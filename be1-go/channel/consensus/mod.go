package consensus

import (
	"encoding/base64"
	"encoding/json"
	"popstellar/channel"
	"popstellar/crypto"
	"popstellar/inbox"
	jsonrpc "popstellar/message"
	"popstellar/message/answer"
	"popstellar/message/messagedata"
	"popstellar/message/query"
	"popstellar/message/query/method"
	"popstellar/message/query/method/message"
	"popstellar/network/socket"
	"popstellar/validation"
	"strconv"
	"sync"
	"time"

	"github.com/rs/zerolog"
	"go.dedis.ch/kyber/v3"
	"golang.org/x/xerrors"
)

const (
	msgID                    = "msg id"
	messageIDNonExistant     = "message doesn't correspond to any previously received message"
	messageNotInCorrectPhase = "consensus corresponding to the message hasn't entered"
)

// Channel defines a consensus channel
type Channel struct {
	sockets channel.Sockets

	inbox *inbox.Inbox

	// /root/<lao_id>/consensus
	channelID string

	hub channel.HubFunctionalities

	attendees map[string]struct{}

	log zerolog.Logger

	consensusInstances map[string]*ConsensusInstance
	messageStates      map[string]*MessageState
}

// Save the state of a consensus instance
type ConsensusInstance struct {
	sync.RWMutex
	id string

	proposed_try int64
	promised_try int64
	accepted_try int64

	accepted_value bool

	decided        bool
	decision       bool
	proposed_value bool

	promises []messagedata.ConsensusPromise
	accepts  []messagedata.ConsensusAccept
}

// State of a consensus by messageID, used when two messages on the same object
// happens
type MessageState struct {
	sync.Mutex

	currentPhase      Phase
	proposer          kyber.Point
	electAcceptNumber int
}

type Phase int

const (
	ElectAcceptPhase Phase = 1
	PromisePhase     Phase = 2
	AcceptPhase      Phase = 3
)

// NewChannel returns a new initialized consensus channel
func NewChannel(channelID string, hub channel.HubFunctionalities, log zerolog.Logger) channel.Channel {
	inbox := inbox.NewInbox(channelID)

	log = log.With().Str("channel", "consensus").Logger()

	return &Channel{
		sockets:            channel.NewSockets(),
		inbox:              inbox,
		channelID:          channelID,
		hub:                hub,
		attendees:          make(map[string]struct{}),
		log:                log,
		consensusInstances: make(map[string]*ConsensusInstance),
		messageStates:      make(map[string]*MessageState),
	}
}

// Subscribe is used to handle a subscribe message from the client
func (c *Channel) Subscribe(sock socket.Socket, msg method.Subscribe) error {
	c.log.Info().Str(msgID, strconv.Itoa(msg.ID)).Msg("received a subscribe")

	c.sockets.Upsert(sock)

	return nil
}

// Unsubscribe is used to handle an unsubscribe message.
func (c *Channel) Unsubscribe(socketID string, msg method.Unsubscribe) error {
	c.log.Info().Str(msgID, strconv.Itoa(msg.ID)).Msg("received an unsubscribe")

	ok := c.sockets.Delete(socketID)
	if !ok {
		return answer.NewError(-2, "client is not subscribed to this channel")
	}

	return nil
}

// Catchup is used to handle a catchup message.
func (c *Channel) Catchup(catchup method.Catchup) []message.Message {
	c.log.Info().Str(msgID, strconv.Itoa(catchup.ID)).Msg("received a catchup")

	return c.inbox.GetSortedMessages()
}

// Broadcast is used to handle a broadcast message.
func (c *Channel) Broadcast(msg method.Broadcast) error {
	err := xerrors.Errorf("a consensus channel shouldn't need to broadcast a message")
	c.log.Err(err)
	return err
}

// broadcastToAllWitnesses is a helper message to broadcast a message to all
// witnesses.
func (c *Channel) broadcastToAllClients(msg message.Message) error {
	c.log.Info().Str(msgID, msg.MessageID).Msg("broadcasting message to all witnesses")

	rpcMessage := method.Broadcast{
		Base: query.Base{
			JSONRPCBase: jsonrpc.JSONRPCBase{
				JSONRPC: "2.0",
			},
			Method: "broadcast",
		},
		Params: struct {
			Channel string          `json:"channel"`
			Message message.Message `json:"message"`
		}{
			c.channelID,
			msg,
		},
	}

	buf, err := json.Marshal(&rpcMessage)
	if err != nil {
		return xerrors.Errorf("failed to marshal broadcast query: %v", err)
	}

	c.sockets.SendToAll(buf)
	return nil
}

// Publish handles publish messages for the consensus channel
func (c *Channel) Publish(publish method.Publish, _ socket.Socket) error {
	err := c.VerifyPublishMessage(publish)
	if err != nil {
		return xerrors.Errorf("failed to verify publish message: %w", err)
	}

	msg := publish.Params.Message

	data := msg.Data

	jsonData, err := base64.URLEncoding.DecodeString(data)
	if err != nil {
		return xerrors.Errorf("failed to decode message data: %v", err)
	}

	object, action, err := messagedata.GetObjectAndAction(jsonData)
	if err != nil {
		return xerrors.Errorf("failed to get object or action: %v", err)
	}

	switch object {
	case messagedata.ConsensusObject:
		err = c.processConsensusObject(action, msg)
	default:
		return answer.NewInvalidObjectError(object)
	}

	if err != nil {
		return xerrors.Errorf("failed to process %q object: %w", object, err)
	}

	err = c.broadcastToAllClients(msg)
	if err != nil {
		return xerrors.Errorf("failed to broadcast message: %v", err)
	}
	return nil
}

// VerifyPublishMessage checks if a Publish message is valid
func (c *Channel) VerifyPublishMessage(publish method.Publish) error {
	c.log.Info().
		Str(msgID, strconv.Itoa(publish.ID)).
		Msg("received a publish")

	// Check if the structure of the message is correct
	msg := publish.Params.Message

	jsonData, err := base64.URLEncoding.DecodeString(msg.Data)
	if err != nil {
		return xerrors.Errorf("failed to decode message data: %v", err)
	}

	// Verify the data
	err = c.hub.GetSchemaValidator().VerifyJSON(jsonData, validation.Data)
	if err != nil {
		return xerrors.Errorf("failed to verify json schema: %w", err)
	}

	// Check if the message already exists
	if _, ok := c.inbox.GetMessage(msg.MessageID); ok {
		return answer.NewError(-3, "message already exists")
	}

	return nil
}

// ProcessConsensusObject processes a Consensus Object.
func (c *Channel) processConsensusObject(action string, msg message.Message) error {

	senderBuf, err := base64.URLEncoding.DecodeString(msg.Sender)
	if err != nil {
		return xerrors.Errorf("failed to decode sender key: %v", err)
	}

	// Unmarshal sender of the message, used to know who is the propose
	senderPoint := crypto.Suite.Point()
	err = senderPoint.UnmarshalBinary(senderBuf)
	if err != nil {
		return answer.NewErrorf(-4, "failed to unmarshal public key of the sender: %v", err)
	}

	switch action {
	case messagedata.ConsensusActionElect:
		var consensusElect messagedata.ConsensusElect

		err = msg.UnmarshalData(&consensusElect)
		if err != nil {
			return xerrors.Errorf("failed to unmarshal consensus#elect: %v", err)
		}

		err = c.processConsensusElect(senderPoint, msg.MessageID, consensusElect)
	case messagedata.ConsensusActionElectAccept:
		var consensusElectAccept messagedata.ConsensusElectAccept

		err = msg.UnmarshalData(&consensusElectAccept)
		if err != nil {
			return xerrors.Errorf("failed to unmarshal consensus#elect-accept: %v", err)
		}

		err = c.processConsensusElectAccept(senderPoint, consensusElectAccept)
	case messagedata.ConsensusActionPrepare:
		var consensusPrepare messagedata.ConsensusPrepare

		err = msg.UnmarshalData(&consensusPrepare)
		if err != nil {
			return xerrors.Errorf("failed to unmarshal consensus#prepare: %v", err)
		}

		err = c.processConsensusPrepare(consensusPrepare)
	case messagedata.ConsensusActionPromise:
		var consensusPromise messagedata.ConsensusPromise

		err = msg.UnmarshalData(&consensusPromise)
		if err != nil {
			return xerrors.Errorf("failed to unmarshal consensus#promise: %v", err)
		}

		err = c.processConsensusPromise(senderPoint, consensusPromise)
	case messagedata.ConsensusActionPropose:
		var consensusPropose messagedata.ConsensusPropose

		err = msg.UnmarshalData(&consensusPropose)
		if err != nil {
			return xerrors.Errorf("failed to unmarshal consensus#propose: %v", err)
		}

		err = c.processConsensusPropose(consensusPropose)
	case messagedata.ConsensusActionAccept:
		var consensusAccept messagedata.ConsensusAccept

		err = msg.UnmarshalData(&consensusAccept)
		if err != nil {
			return xerrors.Errorf("failed to unmarshal consensus#accept: %v", err)
		}

		err = c.processConsensusAccept(consensusAccept)
	case messagedata.ConsensuisActionLearn:
		var consensusLearn messagedata.ConsensusLearn

		err = msg.UnmarshalData(&consensusLearn)
		if err != nil {
			return xerrors.Errorf("failed to unmarshal consensus#learn: %v", err)
		}

		err = c.processConsensusLearn(consensusLearn)
	default:
		return answer.NewInvalidActionError(action)
	}
	if err != nil {
		return xerrors.Errorf("failed to process %s action: %w", action, err)
	}

	c.inbox.StoreMessage(msg)

	return nil
}

// createConsensusInstance adds a new consensus instance to the
// consensusInstances array
func (c *Channel) createConsensusInstance(instanceID string) {
	c.consensusInstances[instanceID] = &ConsensusInstance{

		id: instanceID,

		proposed_try: 0,
		promised_try: -1,
		accepted_try: -1,

		accepted_value: false,
		decided:        false,
		decision:       false,
		proposed_value: false,

		promises: make([]messagedata.ConsensusPromise, 0),
		accepts:  make([]messagedata.ConsensusAccept, 0),
	}
}

// createMessageInstance creates a new message instance to the messageStates
// array
func (c *Channel) createMessageInstance(messageID string, proposer kyber.Point) {
	newMessageState := MessageState{
		currentPhase:      ElectAcceptPhase,
		proposer:          proposer,
		electAcceptNumber: 0,
	}

	c.messageStates[messageID] = &newMessageState
}

// ProcessConsensusElect processes an elect action.
func (c *Channel) processConsensusElect(sender kyber.Point, messageID string, data messagedata.ConsensusElect) error {

	err := data.Verify()
	if err != nil {
		return xerrors.Errorf("invalid consensus#elect message: %v", err)
	}

	// Creates a consensus instance if there is none on the object
	if c.consensusInstances[data.InstanceID] == nil {
		c.createConsensusInstance(data.InstanceID)
	}

	c.createMessageInstance(messageID, sender)

	return nil
}

// ProcessConsensusElectAccept processes an elect accept action.
func (c *Channel) processConsensusElectAccept(sender kyber.Point, data messagedata.ConsensusElectAccept) error {

	err := data.Verify()
	if err != nil {
		return xerrors.Errorf("invalid consensus#elect_accept message: %v", err)
	}

	// check wether a message with the correct ID was received previously
	_, valid := c.inbox.GetMessage(data.MessageID)

	if !valid {
		return xerrors.Errorf(messageIDNonExistant)
	}

	messageState := c.messageStates[data.MessageID]
	messageState.Lock()
	defer messageState.Unlock()
	if data.Accept {
		messageState.electAcceptNumber += 1
	}

	// Once all Elect_Accept have been received, proposer creates new prepare
	// message
	if messageState.electAcceptNumber < c.hub.GetServerNumber() {
		return nil
	}
	if messageState.currentPhase != ElectAcceptPhase ||
		!messageState.proposer.Equal(c.hub.GetPubKeyOrg()) {
		return nil
	}

	consensusInstance := c.consensusInstances[data.InstanceID]
	consensusInstance.Lock()
	defer consensusInstance.Unlock()

	if consensusInstance.proposed_try >= consensusInstance.promised_try {
		consensusInstance.proposed_try += 1
	} else {
		consensusInstance.proposed_try = consensusInstance.promised_try + 1
	}

	// For now the consensus always accept a true if it complete
	consensusInstance.proposed_value = true

	newData := messagedata.ConsensusPrepare{
		Object:     "consensus",
		Action:     "prepare",
		InstanceID: data.InstanceID,
		MessageID:  data.MessageID,

		CreatedAt: time.Now().Unix(),

		Value: messagedata.ValuePrepare{
			ProposedTry: consensusInstance.proposed_try,
		},
	}

	byteMsg, err := json.Marshal(newData)
	if err != nil {
		return xerrors.Errorf("failed to marshal new consensus#prepare message: %v", err)
	}

	err = c.publishNewMessage(byteMsg)
	if err != nil {
		return xerrors.Errorf("failed to send new consensus#prepare message: %v", err)
	}

	return nil
}

// ProcessConsensusPrepare processes a prepare action.
func (c *Channel) processConsensusPrepare(data messagedata.ConsensusPrepare) error {

	err := data.Verify()
	if err != nil {
		return xerrors.Errorf("invalid consensus#prepare message: %v", err)
	}

	// check wether a message with the correct ID was received previously
	_, valid := c.inbox.GetMessage(data.MessageID)
	if !valid {
		return xerrors.Errorf(messageIDNonExistant)
	}

	messageState := c.messageStates[data.MessageID]
	messageState.Lock()
	defer messageState.Unlock()

	messageState.currentPhase = PromisePhase

	consensusInstance := c.consensusInstances[data.InstanceID]
	consensusInstance.Lock()
	defer consensusInstance.Unlock()

	if consensusInstance.promised_try >= data.Value.ProposedTry {
		return nil
	}

	consensusInstance.promised_try = data.Value.ProposedTry

	newData := messagedata.ConsensusPromise{
		Object:     "consensus",
		Action:     "promise",
		InstanceID: data.InstanceID,
		MessageID:  data.MessageID,

		CreatedAt: time.Now().Unix(),

		Value: messagedata.ValuePromise{
			AcceptedTry:   consensusInstance.accepted_try,
			AcceptedValue: consensusInstance.accepted_value,
			PromisedTry:   consensusInstance.promised_try,
		},
	}

	byteMsg, err := json.Marshal(newData)
	if err != nil {
		return xerrors.Errorf("failed to marshal new consensus#promise message: %v", err)
	}

	err = c.publishNewMessage(byteMsg)
	if err != nil {
		return err
	}

	return nil
}

// ProcessConsensusPromise processes a promise action.
func (c *Channel) processConsensusPromise(sender kyber.Point, data messagedata.ConsensusPromise) error {

	err := data.Verify()
	if err != nil {
		return xerrors.Errorf("invalid consensus#promise message: %v", err)
	}

	// check wether a message with the correct ID was received previously
	_, valid := c.inbox.GetMessage(data.MessageID)
	if !valid {
		return xerrors.Errorf(messageIDNonExistant)
	}

	messageState := c.messageStates[data.MessageID]

	if messageState.currentPhase < PromisePhase {
		return xerrors.Errorf(messageNotInCorrectPhase + " the promise phase")
	}

	consensusInstance := c.consensusInstances[data.InstanceID]
	consensusInstance.Lock()
	defer consensusInstance.Unlock()

	consensusInstance.promises = append(consensusInstance.promises, data)

	// if enough Promise messages are received, the proposer send a Propose message
	if len(consensusInstance.promises) < c.hub.GetServerNumber()/2+1 {
		return nil
	}
	if messageState.currentPhase != PromisePhase ||
		!messageState.proposer.Equal(c.hub.GetPubKeyOrg()) {
		return nil
	}

	highestAccepted := int64(-1)
	highestAcceptedValue := true
	for _, promise := range consensusInstance.promises {
		if promise.Value.AcceptedTry > highestAccepted {
			highestAccepted = promise.Value.AcceptedTry
			highestAcceptedValue = promise.Value.AcceptedValue
		}
	}

	newData := messagedata.ConsensusPropose{
		Object:     "consensus",
		Action:     "propose",
		InstanceID: data.InstanceID,
		MessageID:  data.MessageID,

		CreatedAt: time.Now().Unix(),

		Value: messagedata.ValuePropose{
			ProposedValue: highestAcceptedValue,
		},

		AcceptorSignatures: make([]string, 0),
	}

	if highestAccepted == -1 {
		newData.Value.ProposedTry = consensusInstance.proposed_try
	} else {
		newData.Value.ProposedTry = highestAccepted
	}

	byteMsg, err := json.Marshal(newData)
	if err != nil {
		return xerrors.Errorf("failed to marshal new consensus#propose message: %v", err)
	}

	err = c.publishNewMessage(byteMsg)
	if err != nil {
		return err
	}

	return nil
}

// ProcessConsensusPropose processes a propose action.
func (c *Channel) processConsensusPropose(data messagedata.ConsensusPropose) error {

	err := data.Verify()
	if err != nil {
		return xerrors.Errorf("invalid consensus#propose message: %v", err)
	}

	// check wether a message with the correct ID was received previously
	_, valid := c.inbox.GetMessage(data.MessageID)

	if !valid {
		return xerrors.Errorf(messageIDNonExistant)
	}

	messageState := c.messageStates[data.MessageID]

	if messageState.currentPhase < PromisePhase {
		return xerrors.Errorf(messageNotInCorrectPhase + " the promise phase")
	}

	messageState.currentPhase = AcceptPhase

	consensusInstance := c.consensusInstances[data.InstanceID]
	consensusInstance.Lock()
	defer consensusInstance.Unlock()

	// If the server has no client subscribed to the consensus channel, it
	// doesn't take part in it
	if c.sockets.Number() == 0 {
		return nil
	}

	if consensusInstance.promised_try > data.Value.ProposedTry {
		return nil
	}

	consensusInstance.accepted_try = data.Value.ProposedTry
	consensusInstance.accepted_value = data.Value.ProposedValue

	newData := messagedata.ConsensusAccept{
		Object:     "consensus",
		Action:     "accept",
		InstanceID: data.InstanceID,
		MessageID:  data.MessageID,

		CreatedAt: time.Now().Unix(),

		Value: messagedata.ValueAccept{
			AcceptedTry:   consensusInstance.accepted_try,
			AcceptedValue: consensusInstance.accepted_value,
		},
	}
	byteMsg, err := json.Marshal(newData)
	if err != nil {
		return xerrors.Errorf("failed to marshal new consensus#accept message: %v", err)
	}

	err = c.publishNewMessage(byteMsg)
	if err != nil {
		return err
	}

	return nil
}

// ProcessConsensusAccept proccesses an accept action.
func (c *Channel) processConsensusAccept(data messagedata.ConsensusAccept) error {

	err := data.Verify()
	if err != nil {
		return xerrors.Errorf("invalid consensus#accept message: %v", err)
	}

	// check wether a message with the correct ID was received previously
	_, valid := c.inbox.GetMessage(data.MessageID)

	if !valid {
		return xerrors.Errorf(messageIDNonExistant)
	}

	messageState := c.messageStates[data.MessageID]

	if messageState.currentPhase < AcceptPhase {
		return xerrors.Errorf(messageNotInCorrectPhase + " the accept phase")
	}

	consensusInstance := c.consensusInstances[data.InstanceID]
	consensusInstance.Lock()
	defer consensusInstance.Unlock()

	if data.Value.AcceptedTry == consensusInstance.proposed_try &&
		data.Value.AcceptedValue == consensusInstance.proposed_value {
		consensusInstance.accepts = append(consensusInstance.accepts, data)
	}

	if len(consensusInstance.accepts) < c.hub.GetServerNumber()/2+1 {
		return nil
	}
	if !messageState.proposer.Equal(c.hub.GetPubKeyOrg()) {
		return nil
	}

	if consensusInstance.decided {
		return nil
	}

	consensusInstance.decided = true
	consensusInstance.decision = consensusInstance.proposed_value

	newData := messagedata.ConsensusLearn{
		Object:     "consensus",
		Action:     "learn",
		InstanceID: data.InstanceID,
		MessageID:  data.MessageID,

		CreatedAt: time.Now().Unix(),

		Value: messagedata.ValueLearn{
			Decision: consensusInstance.decision,
		},

		AcceptorSignatures: make([]string, 0),
	}
	byteMsg, err := json.Marshal(newData)
	if err != nil {
		return xerrors.Errorf("failed to marshal new consensus#promise message: %v", err)
	}

	err = c.publishNewMessage(byteMsg)
	if err != nil {
		return err
	}

	return nil
}

// ProcessConsensusLearn processes a learn action.
func (c *Channel) processConsensusLearn(data messagedata.ConsensusLearn) error {

	err := data.Verify()
	if err != nil {
		return xerrors.Errorf("invalid consensus#learn message: %v", err)
	}

	// check wether a message with the correct ID was received previously
	_, valid := c.inbox.GetMessage(data.MessageID)

	if !valid {
		return xerrors.Errorf("message doesn't correspond to any received message")
	}

	consensusInstance := c.consensusInstances[data.InstanceID]
	if !consensusInstance.decided {
		consensusInstance.decided = true
		consensusInstance.decision = data.Value.Decision
	}

	return nil
}

// publishNewMessage send a publish message on the current channel
func (c *Channel) publishNewMessage(byteMsg []byte) error {

	encryptedMsg := base64.URLEncoding.EncodeToString(byteMsg)

	publicKey := c.hub.GetPubKeyServ()
	pkBuf, err := publicKey.MarshalBinary()
	if err != nil {
		return xerrors.Errorf("failed to marshal the public key: %v", err)
	}

	encryptedKey := base64.URLEncoding.EncodeToString(pkBuf)

	signatureBuf, err := c.hub.Sign(byteMsg)
	if err != nil {
		return xerrors.Errorf("failed to sign the data: %v", err)
	}

	signature := base64.URLEncoding.EncodeToString(signatureBuf)

	messageID := messagedata.Hash(encryptedMsg, encryptedKey)

	msg := message.Message{
		Data:              encryptedMsg,
		Sender:            encryptedKey,
		Signature:         signature,
		MessageID:         messageID,
		WitnessSignatures: make([]message.WitnessSignature, 0),
	}

	publish := method.Publish{
		Base: query.Base{
			JSONRPCBase: jsonrpc.JSONRPCBase{
				JSONRPC: "2.0",
			},
			Method: "publish",
		},

		Params: struct {
			Channel string          "json:\"channel\""
			Message message.Message "json:\"message\""
		}{
			Channel: c.channelID,
			Message: msg,
		},
	}

	c.hub.SetMessageID(&publish)

	err = c.hub.SendAndHandleMessage(publish)
	if err != nil {
		return xerrors.Errorf("failed to send new message: %v", err)
	}

	return nil
}
