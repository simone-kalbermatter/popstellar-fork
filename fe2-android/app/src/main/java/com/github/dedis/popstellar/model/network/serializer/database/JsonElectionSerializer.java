package com.github.dedis.popstellar.model.network.serializer.database;

import com.github.dedis.popstellar.model.network.method.message.data.election.*;
import com.github.dedis.popstellar.model.objects.Channel;
import com.github.dedis.popstellar.model.objects.Election;
import com.github.dedis.popstellar.model.objects.event.EventState;
import com.github.dedis.popstellar.model.objects.security.MessageID;
import com.github.dedis.popstellar.model.objects.security.PublicKey;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

public class JsonElectionSerializer
    implements JsonSerializer<Election>, JsonDeserializer<Election> {

  @Override
  public Election deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();

    // Deserialize the nested Channel object
    Channel channel = context.deserialize(jsonObject.get("channel"), Channel.class);

    // Deserialize the primitive values
    String id = jsonObject.get("id").getAsString();
    String name = jsonObject.get("name").getAsString();
    long creation = jsonObject.get("creation").getAsLong();
    long start = jsonObject.get("start").getAsLong();
    long end = jsonObject.get("end").getAsLong();
    JsonElement electionKeyElement = jsonObject.get("electionKey");
    String electionKey = electionKeyElement == null ? null : electionKeyElement.getAsString();

    // Deserialize the enums
    ElectionVersion electionVersion =
        ElectionVersion.valueOf(jsonObject.get("electionVersion").getAsString());
    String stateString = jsonObject.get("state").getAsString();
    EventState state = stateString.isEmpty() ? null : EventState.valueOf(stateString);

    // Deserialize the list electionQuestions
    JsonArray electionQuestionsJsonArray = jsonObject.get("electionQuestions").getAsJsonArray();
    List<ElectionQuestion> electionQuestions = new ArrayList<>();
    for (JsonElement electionQuestionJsonElement : electionQuestionsJsonArray) {
      electionQuestions.add(
          context.deserialize(electionQuestionJsonElement, ElectionQuestion.class));
    }

    // Deserialize the map votesBySender
    Map<PublicKey, List<Vote>> votesBySender = new HashMap<>();
    JsonObject votesBySenderObject = jsonObject.get("votesBySender").getAsJsonObject();
    for (Map.Entry<String, JsonElement> entry : votesBySenderObject.entrySet()) {
      PublicKey senderPublicKey = new PublicKey(entry.getKey());
      JsonArray votesBySenderJsonArray = entry.getValue().getAsJsonArray();
      // Deserialize the value of the map (list of votes)
      List<Vote> votesBySenderPk = new ArrayList<>();
      for (JsonElement element : votesBySenderJsonArray) {
        votesBySenderPk.add(context.deserialize(element, Vote.class));
      }
      votesBySender.put(senderPublicKey, votesBySenderPk);
    }

    // Deserialize the map messageMap
    Map<PublicKey, MessageID> messageMap = new HashMap<>();
    JsonObject messageMapObject = jsonObject.get("messageMap").getAsJsonObject();
    for (Map.Entry<String, JsonElement> entry : messageMapObject.entrySet()) {
      PublicKey senderPublicKey = new PublicKey(entry.getKey());
      MessageID messageId = new MessageID(entry.getValue().getAsString());
      messageMap.put(senderPublicKey, messageId);
    }

    // Deserialize the map results
    Map<String, Set<QuestionResult>> results = new HashMap<>();
    JsonObject resultsObject = jsonObject.get("results").getAsJsonObject();
    for (Map.Entry<String, JsonElement> entry : resultsObject.entrySet()) {
      String questionId = entry.getKey();
      // Deserialize the value of the map (set of question results)
      JsonArray resultsJsonArray = entry.getValue().getAsJsonArray();
      Set<QuestionResult> resultsByQuestion = new HashSet<>();
      for (JsonElement element : resultsJsonArray) {
        resultsByQuestion.add(context.deserialize(element, QuestionResult.class));
      }
      results.put(questionId, resultsByQuestion);
    }

    return new Election(
        id,
        name,
        creation,
        channel,
        start,
        end,
        electionQuestions,
        electionKey,
        electionVersion,
        votesBySender,
        messageMap,
        state,
        results);
  }

  @Override
  public JsonElement serialize(
      Election election, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();

    // Serialize the channel object
    jsonObject.add("channel", context.serialize(election.getChannel(), Channel.class));

    // Serialize the primitive values
    jsonObject.addProperty("id", election.getId());
    jsonObject.addProperty("name", election.getName());
    jsonObject.addProperty("creation", election.getCreation());
    jsonObject.addProperty("start", election.getStartTimestamp());
    jsonObject.addProperty("end", election.getEndTimestamp());
    jsonObject.addProperty("electionKey", election.getElectionKey());

    // Serialize the enum
    EventState state = election.getState();
    jsonObject.addProperty("state", state == null ? "" : state.name());
    jsonObject.addProperty("electionVersion", election.getElectionVersion().name());

    // Serialize the list of election questions into a JsonArray
    JsonArray electionQuestionsJsonArray = new JsonArray();
    for (ElectionQuestion electionQuestion : election.getElectionQuestions()) {
      electionQuestionsJsonArray.add(context.serialize(electionQuestion, ElectionQuestion.class));
    }
    jsonObject.add("electionQuestions", electionQuestionsJsonArray);

    // Serialize the map of votesBySender into a JsonObject
    JsonObject votesBySenderJsonObject = new JsonObject();
    for (Map.Entry<PublicKey, List<Vote>> entry : election.getVotesBySender().entrySet()) {
      String pk = entry.getKey().getEncoded();
      // Serialize the list of votes into a JsonArray
      JsonArray votesBySenderJsonArray = new JsonArray();
      for (Vote vote : entry.getValue()) {
        votesBySenderJsonArray.add(context.serialize(vote, Vote.class));
      }
      votesBySenderJsonObject.add(pk, votesBySenderJsonArray);
    }
    jsonObject.add("votesBySender", votesBySenderJsonObject);

    // Serialize the messageMap into a JsonObject
    JsonObject messageMapJsonObject = new JsonObject();
    for (Map.Entry<PublicKey, MessageID> entry : election.getMessageMap().entrySet()) {
      String pk = entry.getKey().getEncoded();
      String messageID = entry.getValue().getEncoded();
      JsonElement jsonElement = new JsonPrimitive(messageID);
      messageMapJsonObject.add(pk, jsonElement);
    }
    jsonObject.add("messageMap", messageMapJsonObject);

    // Serialize the results map into a JsonObject
    JsonObject resultsJsonObject = new JsonObject();
    for (Map.Entry<String, Set<QuestionResult>> entry : election.getResults().entrySet()) {
      // Serialize the set of question results into a JsonArray
      JsonArray resultsJsonArray = new JsonArray();
      for (QuestionResult vote : entry.getValue()) {
        resultsJsonArray.add(context.serialize(vote, QuestionResult.class));
      }
      resultsJsonObject.add(entry.getKey(), resultsJsonArray);
    }
    jsonObject.add("results", resultsJsonObject);

    return jsonObject;
  }
}
