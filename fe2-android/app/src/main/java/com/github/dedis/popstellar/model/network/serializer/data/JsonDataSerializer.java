package com.github.dedis.popstellar.model.network.serializer.data;

import com.github.dedis.popstellar.model.network.method.message.data.*;
import com.github.dedis.popstellar.model.network.serializer.JsonUtils;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Optional;

/** Json serializer and deserializer for the data messages */
public class JsonDataSerializer implements JsonSerializer<Data>, JsonDeserializer<Data> {

  private static final String OBJECT = "object";
  private static final String ACTION = "action";

  private final DataRegistry dataRegistry;

  public JsonDataSerializer(DataRegistry dataRegistry) {
    this.dataRegistry = dataRegistry;
  }

  @Override
  public Data deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    JsonUtils.verifyJson(JsonUtils.DATA_SCHEMA, obj.toString());
    Objects object = Objects.find(obj.get(OBJECT).getAsString());
    Action action = Action.find(obj.get(ACTION).getAsString());

    if (object == null) {
      throw new JsonParseException("Unknown object type : " + obj.get(OBJECT).getAsString());
    }
    if (action == null) {
      throw new JsonParseException("Unknown action type : " + obj.get(ACTION).getAsString());
    }

    Optional<Class<? extends Data>> clazz = dataRegistry.getType(object, action);
    if (!clazz.isPresent()) {
      throw new JsonParseException(
          "The pair ("
              + object.getObject()
              + ", "
              + action.getAction()
              + ") does not exists in the protocol");
    }

    return context.deserialize(json, clazz.get());
  }

  @Override
  public JsonElement serialize(Data src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = context.serialize(src).getAsJsonObject();
    obj.addProperty(OBJECT, src.getObject());
    obj.addProperty(ACTION, src.getAction());
    JsonUtils.verifyJson(JsonUtils.DATA_SCHEMA, obj.toString());
    return obj;
  }
}
