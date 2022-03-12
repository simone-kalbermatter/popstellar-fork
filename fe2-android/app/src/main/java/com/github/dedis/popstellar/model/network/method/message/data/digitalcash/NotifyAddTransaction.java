package com.github.dedis.popstellar.model.network.method.message.data.digitalcash;

import com.github.dedis.popstellar.model.network.method.message.data.Action;
import com.github.dedis.popstellar.model.network.method.message.data.Data;
import com.github.dedis.popstellar.model.network.method.message.data.Objects;
import com.github.dedis.popstellar.model.objects.Channel;
import com.github.dedis.popstellar.model.objects.security.MessageID;
import com.google.gson.annotations.SerializedName;

/** Data sent to broadcast AddChirp to the general channel */
public class NotifyAddTransaction extends Data {

  @SerializedName("chirp_id")
  private final MessageID chirpId;

  private final Channel channel;
  private final long timestamp;

  /**
   * @param chirpId message ID of the chirp
   * @param channel channel where the post is located
   * @param timestamp UNIX timestamp in UTC
   */
  public NotifyAddTransaction(MessageID chirpId, Channel channel, long timestamp) {
    this.chirpId = chirpId;
    this.channel = channel;
    this.timestamp = timestamp;
  }

  @Override
  public String getObject() {
    return Objects.CHIRP.getObject();
  }

  @Override
  public String getAction() {
    return Action.NOTIFY_ADD.getAction();
  }

  public MessageID getChirpId() {
    return chirpId;
  }

  public Channel getChannel() {
    return channel;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotifyAddTransaction that = (NotifyAddTransaction) o;
    return java.util.Objects.equals(getChirpId(), that.getChirpId())
        && java.util.Objects.equals(getChannel(), that.getChannel())
        && java.util.Objects.equals(getTimestamp(), that.getTimestamp());
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(getChirpId(), getChannel(), getTimestamp());
  }

  @Override
  public String toString() {
    return "NotifyAddChirp{"
        + "chirpId='"
        + chirpId.getEncoded()
        + '\''
        + ", channel='"
        + channel
        + '\''
        + ", timestamp='"
        + timestamp
        + '\''
        + '}';
  }
}
