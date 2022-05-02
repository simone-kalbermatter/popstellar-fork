package com.github.dedis.popstellar.model.objects;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

/** Represents an peer address */
public class PeerAddress {

  // Purpose of this class is that, in the future, the content of the peers field will contain
  // additionnal fields
  // in addition to the String address field like in the exemple
  // peers: "type": "array", "items": {"type": "object", [...] }
  // ex: peers: [{address: "x", type:"organizer"}, {address: "x", type:"witness"}]
  @SerializedName("address")
  private final String address;

  public PeerAddress(@NonNull String address) {
    this.address = address;
  }

  @NonNull
  public String getAddress() {
    return address;
  }

  @NonNull
  @Override
  public String toString() {
    return "{address:='" + getAddress() + "'}";
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(address);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      System.out.println(1);
      return false;
    }

    PeerAddress that = (PeerAddress) o;
    return that.getAddress().equals(getAddress());
  }
}
