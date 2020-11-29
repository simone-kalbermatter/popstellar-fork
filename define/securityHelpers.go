package define

import (
	"bytes"
	"crypto/sha256"
	ed "crypto/ed25519"
	"github.com/rogpeppe/godef/go/ast"
	"strconv"
	"time"
	"fmt"
)

const MaxTimeBetweenLAOCreationAndPublish = 600

// TODO if we use the json Schema, don't need to check structure correctness
func LAOCreatedIsValid(data DataCreateLAO, message Message) error {
	//the last modified timestamp is equal to the creation timestamp,
	if data.Creation != data.Last_modified {
		fmt.Printf("%v, %v", data, data.Last_modified)
		fmt.Printf("sec1")
		return ErrInvalidResource
	}
	//the timestamp is reasonably recent with respect to the server’s clock,
	if data.Creation > time.Now().Unix() || data.Creation-time.Now().Unix() > MaxTimeBetweenLAOCreationAndPublish {
		fmt.Printf("sec2")
		return ErrInvalidResource
	}
	//the attestation is valid,
	str := []byte(data.Organizer)
	str = append(str, []byte(strconv.FormatInt(data.Creation, 10))...)
	str = append(str, []byte(data.Name)...)
	hash := sha256.Sum256(str)
	

	if !bytes.Equal([]byte(message.Message_id), hash[:]) {
		fmt.Printf("sec3 \n")
		fmt.Printf("%v, %v", string(hash[:]), message.Message_id)
		// TODO Reactivate return error once hash is solved
		// the real issue is probably that my hash is incorrect because wasn't cast to string... but string(hash) is gross. Maybe the solution is the shift to  base64 encoded hash (and everything else)
		//return ErrInvalidResource
	}

	return nil
}

func MeetingCreatedIsValid(data DataCreateMeeting, message Message) error {
	//the last modified timestamp is equal to the creation timestamp,
	if data.Creation != data.Last_modified {
		return ErrInvalidResource
	}
	//the timestamp is reasonably recent with respect to the server’s clock,
	if data.Creation > time.Now().Unix() || data.Creation-time.Now().Unix() > MaxTimeBetweenLAOCreationAndPublish {
		return ErrInvalidResource
	}

	//we start after the creation and we end after the start
	if data.Start < data.Creation || data.End < data.Start {
		return ErrInvalidResource
	}
	//need to meet some	where
	if data.Location == "" {
		return ErrInvalidResource
	}
	return nil
}

func PollCreatedIsValid(data DataCreatePoll, message Message) error {
	return nil
}

func RollCallCreatedIsValid(data DataCreateRollCall, message Message) error {
	return nil
}

func MessageIsValid(msg Message) error {
	return nil
}
/*
	we check that Sign(sender||data) is the given signature
*/
func VerifySignature(publicKey string, data string,signature string ) error{
	//check the size of the key as it will panic if we plug it in Verify
	if len(publicKey) != ed.PublicKeySize{
		return ErrRequestDataInvalid
	}
	//check the validity of the signature
	//TODO method is defined supposing args are encrypted
	//the key is in base64 so we need to decrypt it before using it
	keyInClear,err := Decode(publicKey)
	if err!=nil{
		return ErrEncodingFault
	}
	//data is also in base64 so we need to decrypt it before using it
	dataInClear,err := Decode(data)
	if err!=nil{
		return ErrEncodingFault
	}
	if ed.Verify(keyInClear, dataInClear, []byte(signature)){
		return nil
	}
	//invalid signature
	return ErrRequestDataInvalid
}

//TODO be careful about the size and the order !
/*Maybe have a fixed size byte ?
To handle checks while the slice is in construction, the slice must have full space
from the beginning. We should check how to create fixed length arrays in go. And
instead of appending in witness_message, put them in the slot which matches the slot
of the witness id in witness[]

	Witness[1,2,3...]
	witnessSignature[_,_,_./.]
*/
func VerifyWitnessSignatures(publicKeys []byte, signatures []byte,data string,sender string,signature string ) error {
	senderInClear,err := Decode(sender)
	if err!=nil{
		return ErrEncodingFault
	}
	dataInClear,err := Decode(data)
	if err!=nil{
		return ErrEncodingFault
	}
	toCheck := string(senderInClear) + string(dataInClear)
	for i := 0; i < len(signatures); i++ {
		err := VerifySignature(string (publicKeys[i]), toCheck ,string (signatures[i]))
		if err!= nil{
			return err
		}
	}
	return nil
}