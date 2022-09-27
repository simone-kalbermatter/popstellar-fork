package com.github.dedis.popstellar.model.qrcode;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.github.dedis.popstellar.model.objects.security.PublicKey;
import com.github.dedis.popstellar.testutils.Base64DataUtils;
import com.google.gson.Gson;

public class PopTokenDataTest {

  private final Gson gson = new Gson();
  private final PublicKey pk = Base64DataUtils.generatePublicKey();

  @Test
  public void constructorAndGetterAreCoherent() {
    PopTokenData data = new PopTokenData(pk);
    assertEquals(pk, data.getPopToken());
  }

  @Test
  public void extractDataTest() {
    String jsonFormat = gson.toJson(new PopTokenData(pk));
    assertEquals(pk, PopTokenData.extractFrom(gson, jsonFormat).getPopToken());
  }
}
