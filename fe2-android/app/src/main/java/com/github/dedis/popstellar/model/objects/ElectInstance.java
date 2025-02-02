package com.github.dedis.popstellar.model.objects;

import androidx.annotation.NonNull;

import com.github.dedis.popstellar.model.Copyable;
import com.github.dedis.popstellar.model.network.method.message.data.consensus.*;
import com.github.dedis.popstellar.model.objects.security.MessageID;
import com.github.dedis.popstellar.model.objects.security.PublicKey;
import com.github.dedis.popstellar.utility.security.Hash;

import java.util.*;

/**
 * Class holding information of a ConsensusElect message and its current states including the
 * key/messageId of every node that have accepted this Elect with an ElectAccept.
 */
public final class ElectInstance implements Copyable<ElectInstance> {

  private final MessageID messageId;
  private final Channel channel;
  private final PublicKey proposer;
  private final ConsensusElect elect;
  private final Set<PublicKey> nodes;
  // map the public key of acceptors to the id of their ElectAccept message
  private final Map<PublicKey, MessageID> acceptorToMessageId;

  private State state;

  public ElectInstance(
      @NonNull MessageID messageId,
      @NonNull Channel channel,
      @NonNull PublicKey proposer,
      @NonNull Set<PublicKey> nodes,
      @NonNull ConsensusElect elect) {
    this.messageId = messageId;
    this.channel = channel;
    this.proposer = proposer;
    this.elect = elect;
    this.nodes = Collections.unmodifiableSet(nodes);
    acceptorToMessageId = new HashMap<>();

    state = State.STARTING;
  }

  public ElectInstance(ElectInstance electInstance) {
    messageId = electInstance.messageId;
    channel = electInstance.channel;
    proposer = electInstance.proposer;
    elect = electInstance.elect;
    nodes = electInstance.nodes;
    acceptorToMessageId = new HashMap<>(electInstance.acceptorToMessageId);
    state = electInstance.state;
  }

  public MessageID getMessageId() {
    return messageId;
  }

  public Channel getChannel() {
    return channel;
  }

  public String getInstanceId() {
    return elect.getInstanceId();
  }

  public ConsensusKey getKey() {
    return elect.getKey();
  }

  public Object getValue() {
    return elect.getValue();
  }

  public long getCreation() {
    return elect.getCreation();
  }

  public State getState() {
    return state;
  }

  public void setState(@NonNull State state) {
    this.state = state;
  }

  public PublicKey getProposer() {
    return proposer;
  }

  public Map<PublicKey, MessageID> getAcceptorsToMessageId() {
    return Collections.unmodifiableMap(acceptorToMessageId);
  }

  public void addElectAccept(
      @NonNull PublicKey publicKey,
      @NonNull MessageID messageId,
      @NonNull ConsensusElectAccept electAccept) {
    if (electAccept.isAccept()) {
      acceptorToMessageId.put(publicKey, messageId);
    }
  }

  public Set<PublicKey> getNodes() {
    return nodes;
  }

  @NonNull
  @Override
  public String toString() {
    return String.format(
        "ElectInstance{messageId='%s', instanceId='%s', channel='%s', proposer='%s', elect=%s, nodes=%s, state=%s}",
        messageId.getEncoded(),
        getInstanceId(),
        channel,
        proposer.getEncoded(),
        elect.toString(),
        nodes.toString(),
        state);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ElectInstance that = (ElectInstance) o;
    return messageId.equals(that.messageId)
        && channel.equals(that.channel)
        && proposer.equals(that.proposer)
        && elect.equals(that.elect)
        && nodes.equals(that.nodes)
        && acceptorToMessageId.equals(that.acceptorToMessageId)
        && state == that.state;
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId, channel, proposer, elect, nodes, acceptorToMessageId, state);
  }

  /**
   * Generate the id for a consensus instance. This instanceId is used to group all Elect that
   * refers to the same object and property and will be used in every Consensus Data message.
   *
   * <p>https://github.com/dedis/popstellar/blob/master/protocol/query/method/message/data/dataElect.json
   *
   * @param type The object type that the consensus refers to
   * @param id The object id that the consensus refers to
   * @param property The property of the object that the value refers to
   * @return the id computed as HashLen('consensus', key:type, key:id, key:property)
   */
  public static String generateConsensusId(
      @NonNull String type, @NonNull String id, @NonNull String property) {
    return Hash.hash("consensus", type, id, property);
  }

  @Override
  public ElectInstance copy() {
    return new ElectInstance(this);
  }

  public enum State {
    FAILED,
    WAITING,
    STARTING,
    ACCEPTED
  }
}
