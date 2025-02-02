package com.github.dedis.popstellar.model.network;

import com.github.dedis.popstellar.di.DataRegistryModuleHelper;
import com.github.dedis.popstellar.di.JsonModule;
import com.github.dedis.popstellar.model.network.method.message.data.Data;
import com.github.dedis.popstellar.model.network.serializer.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.*;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class JsonTestUtils {

  public static final Gson GSON = JsonModule.provideGson(DataRegistryModuleHelper.buildRegistry());

  public static String loadFile(String path) {
    InputStream is =
        Objects.requireNonNull(JsonTestUtils.class.getClassLoader()).getResourceAsStream(path);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    return reader.lines().collect(Collectors.joining("\n"));
  }

  /**
   * Convert the given data to a json String, convert it back to a Data and compare them.
   *
   * @param data the Data to test
   * @throws JsonParseException if it failed to parse
   * @throws AssertionError if the converted data is not equals to data
   */
  public static void testData(Data data) throws JsonParseException {
    String json = GSON.toJson(data, Data.class);
    JsonUtils.verifyJson(JsonUtils.DATA_SCHEMA, json);
    assertEquals(data, GSON.fromJson(json, Data.class));
  }

  public static Data parse(String json) throws JsonParseException {
    return GSON.fromJson(json, Data.class);
  }
}
