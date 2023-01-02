package com.github.dedis.popstellar.model.objects;

import androidx.annotation.NonNull;

import com.github.dedis.popstellar.model.Copyable;
import com.github.dedis.popstellar.model.Immutable;
import com.github.dedis.popstellar.model.network.method.message.data.election.*;
import com.github.dedis.popstellar.model.objects.event.*;
import com.github.dedis.popstellar.model.objects.security.*;
import com.github.dedis.popstellar.model.objects.security.elGamal.ElectionPublicKey;
import com.github.dedis.popstellar.utility.security.Hash;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Immutable
public class Election extends Event {

  private final Channel channel;
  private final String id;
  private final String name;
  private final long creation;
  private final long start;
  private final long end;
  private final List<ElectionQuestion> electionQuestions;
  // Election public key is generated via Kyber and is encoded in Base64
  // decoding it is required before actually starting using it
  private final String electionKey;
  // Either OPEN_BALLOT or SECRET_BALLOT
  private final ElectionVersion electionVersion;

  // Map that associates each sender pk to their open ballot votes
  private final Map<PublicKey, List<ElectionVote>> openVoteByPublicKey;
  // Map that associates each sender pk to their encrypted votes
  private final Map<PublicKey, List<ElectionEncryptedVote>> encryptedVoteByPublicKey;

  // Map that associates each messageId to its sender
  private final Map<MessageID, PublicKey> messageMap;

  private final EventState state;

  // Results of an election (associated to a question id)
  private final Map<String, Set<QuestionResult>> results;

  public Election(
      String id,
      String name,
      long creation,
      Channel channel,
      long start,
      long end,
      List<ElectionQuestion> electionQuestions,
      String electionKey,
      ElectionVersion electionVersion,
      Map<PublicKey, List<ElectionVote>> openVoteByPublicKey,
      Map<PublicKey, List<ElectionEncryptedVote>> encryptedVoteByPublicKey,
      Map<MessageID, PublicKey> messageMap,
      EventState state,
      Map<String, Set<QuestionResult>> results) {
    this.id = id;
    this.name = name;
    this.creation = creation;
    this.channel = channel;
    this.start = start;
    this.end = end;
    this.state = state;
    this.electionKey = electionKey;
    this.electionVersion = electionVersion;
    // Defensive copies
    this.electionQuestions = new ArrayList<>(electionQuestions);
    this.openVoteByPublicKey = Copyable.copyMapOfList(openVoteByPublicKey);
    this.encryptedVoteByPublicKey = Copyable.copyMapOfList(encryptedVoteByPublicKey);
    this.results = Copyable.copyMapOfSet(results);
    // Create message map as a tree map to sort messages correctly
    this.messageMap = new TreeMap<>(Comparator.comparing(MessageID::getEncoded));
    this.messageMap.putAll(messageMap);
  }

  public String getElectionKey() {
    return electionKey;
  }

  public String getName() {
    return name;
  }

  public ElectionVersion getElectionVersion() {
    return electionVersion;
  }

  public long getCreation() {
    return creation;
  }

  public long getCreationInMillis() {
    return getCreation() * 1000;
  }

  public Channel getChannel() {
    return channel;
  }

  public List<ElectionQuestion> getElectionQuestions() {
    return electionQuestions;
  }

  public EventState getState() {
    return state;
  }

  public Map<MessageID, PublicKey> getMessageMap() {
    return messageMap;
  }

  public String getId() {
    return id;
  }

  @Override
  public long getStartTimestamp() {
    return start;
  }

  @Override
  public long getEndTimestamp() {
    return end;
  }

  public Set<QuestionResult> getResultsForQuestionId(String id) {
    return results.get(id);
  }

  @Override
  public EventType getType() {
    return EventType.ELECTION;
  }

  /**
   * Generate the id for dataElectionSetup.
   * https://github.com/dedis/popstellar/blob/master/protocol/query/method/message/data/dataElectionSetup.json
   *
   * @param laoId ID of the LAO
   * @param createdAt creation time of the election
   * @param name name of the election
   * @return the ID of ElectionSetup computed as Hash('Election'||lao_id||created_at||name)
   */
  public static String generateElectionSetupId(String laoId, long createdAt, String name) {
    return Hash.hash(EventType.ELECTION.getSuffix(), laoId, Long.toString(createdAt), name);
  }

  /**
   * Generate the id for a question of dataElectionSetup and dataElectionResult.
   * https://github.com/dedis/popstellar/blob/master/protocol/query/method/message/data/dataElectionSetup.json
   * https://github.com/dedis/popstellar/blob/master/protocol/query/method/message/data/dataElectionResult.json
   *
   * @param electionId ID of the Election
   * @param question question of the Election
   * @return the ID of an election question computed as Hash(“Question”||election_id||question)
   */
  public static String generateElectionQuestionId(String electionId, String question) {
    return Hash.hash("Question", electionId, question);
  }

  /**
   * Generate the id for a vote of dataCastVote.
   * https://github.com/dedis/popstellar/blob/master/protocol/query/method/message/data/dataCastVote.json
   *
   * @param electionId ID of the Election
   * @param questionId ID of the Election question
   * @param voteIndex index(es) of the vote
   * @param writeIn string representing the write in
   * @param writeInEnabled boolean representing if write enabled or not
   * @return the ID of an election question computed as
   *     Hash('Vote'||election_id||question_id||(vote_index(es)|write_in))
   */
  public static String generateElectionVoteId(
      String electionId,
      String questionId,
      Integer voteIndex,
      String writeIn,
      boolean writeInEnabled) {
    // If write_in is enabled the id is formed with the write_in string
    // If write_in is not enabled the id is formed with the vote indexes (formatted as int1, int2,
    // ). The vote are concatenated and brackets are removed from the array toString representation
    return Hash.hash(
        "Vote", electionId, questionId, writeInEnabled ? writeIn : voteIndex.toString());
  }

  /**
   * Generate the id for a vote of dataCastVote.
   * https://github.com/dedis/popstellar/blob/master/protocol/query/method/message/data/dataCastVote.json
   *
   * @param electionId ID of the Election
   * @param questionId ID of the Election question
   * @param voteIndexEncrypted index(es) of the vote
   * @param writeInEncrypted string representing the write in
   * @param writeInEnabled boolean representing if write enabled or not
   * @return the ID of an election question computed as
   *     Hash('Vote'||election_id||question_id||(encrypted_vote_index(es)|encrypted_write_in))
   */
  public static String generateEncryptedElectionVoteId(
      String electionId,
      String questionId,
      String voteIndexEncrypted,
      String writeInEncrypted,
      boolean writeInEnabled) {
    // HashLen('Vote', election_id, question_id, (encrypted_vote_index|encrypted_write_in))),
    // concatenate vote indexes - must sort in alphabetical order and use delimiter ','"
    return Hash.hash(
        "Vote", electionId, questionId, writeInEnabled ? writeInEncrypted : voteIndexEncrypted);
  }

  /**
   * Computes the hash for the registered votes, when terminating an election (sorted by message
   * id's alphabetical order)
   *
   * @return the hash of all registered votes
   */
  public String computerRegisteredVotes() {
    List<String> listOfVoteIds =
        getElectionVersion() == ElectionVersion.OPEN_BALLOT
            ? getListOfVoteIds(openVoteByPublicKey, ElectionVote::getId)
            : getListOfVoteIds(encryptedVoteByPublicKey, ElectionEncryptedVote::getId);

    if (listOfVoteIds.isEmpty()) {
      return "";
    } else {
      return Hash.hash(listOfVoteIds.toArray(new String[0]));
    }
  }

  private <VOTE> List<String> getListOfVoteIds(
      @NonNull Map<PublicKey, List<VOTE>> map, Function<VOTE, String> voteToId) {
    // Since messageMap is a TreeMap, votes will already be sorted in the alphabetical order of
    // messageIds
    return messageMap.values().stream()
        .map(map::get)
        // Merge lists and drop nulls
        .flatMap(electionVotes -> electionVotes != null ? electionVotes.stream() : Stream.empty())
        .map(voteToId)
        .collect(Collectors.toList());
  }

  /**
   * Encrypts the content of the votes using El-GamaL scheme
   *
   * @param votes list of votes to encrypt
   * @return encrypted votes
   */
  public List<ElectionEncryptedVote> encrypt(List<ElectionVote> votes) {
    // We need to iterate over all election votes to encrypt them
    List<ElectionEncryptedVote> encryptedVotes = new ArrayList<>();
    for (ElectionVote vote : votes) {
      // We are sure that each vote is unique per question following new specification
      int voteIndice = vote.getVote();

      // Get the two lsb byte from the indice
      byte[] voteIndiceInBytes = {(byte) (voteIndice >> 8), (byte) voteIndice};

      // Create a public key and encrypt the indice
      Base64URLData electionKeyToBase64 = new Base64URLData(getElectionKey());
      ElectionPublicKey key = new ElectionPublicKey(electionKeyToBase64);
      // Encrypt the indice
      String encryptedVotesIndice = key.encrypt(voteIndiceInBytes);
      ElectionEncryptedVote encryptedVote =
          new ElectionEncryptedVote(vote.getQuestionId(), encryptedVotesIndice, false, null, id);
      encryptedVotes.add(encryptedVote);
    }
    return encryptedVotes;
  }

  @NonNull
  @Override
  public String toString() {
    return "Election{"
        + "channel='"
        + channel
        + '\''
        + ", id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", creation="
        + creation
        + ", start="
        + start
        + ", end="
        + end
        + ", electionQuestions="
        + Arrays.toString(electionQuestions.toArray())
        + ", voteMap="
        + openVoteByPublicKey
        + ", messageMap="
        + messageMap
        + ", state="
        + state
        + ", results="
        + results
        + '}';
  }

  public ElectionBuilder builder() {
    return new ElectionBuilder(this);
  }

  public static class ElectionBuilder {

    private final String id;
    private String name;
    private long creation;
    private Channel channel;
    private long start;
    private long end;
    private List<ElectionQuestion> electionQuestions;
    private String electionKey;
    private ElectionVersion electionVersion;
    private final Map<PublicKey, List<ElectionVote>> openVoteByPublicKey;
    private final Map<PublicKey, List<ElectionEncryptedVote>> encryptedVoteByPublicKey;
    private final Map<MessageID, PublicKey> messageMap;
    private EventState state;
    private Map<String, Set<QuestionResult>> results;

    /**
     * This is a special builder that can be used to generate the default values of an election
     * being created for the first time
     *
     * @param laoId id of the LAO
     * @param creation time
     * @param name of the election
     */
    public ElectionBuilder(String laoId, long creation, String name) {
      this.id = generateElectionSetupId(laoId, creation, name);
      this.name = name;
      this.creation = creation;
      this.channel = Channel.getLaoChannel(laoId).subChannel(id);

      this.results = new HashMap<>();
      this.electionQuestions = new ArrayList<>();
      this.openVoteByPublicKey = new HashMap<>();
      this.encryptedVoteByPublicKey = new HashMap<>();
      this.messageMap = new HashMap<>();
    }

    public ElectionBuilder(Election election) {
      this.channel = election.channel;
      this.id = election.id;
      this.name = election.name;
      this.creation = election.creation;
      this.start = election.start;
      this.end = election.end;
      this.electionKey = election.electionKey;
      this.electionQuestions = election.electionQuestions;
      this.electionVersion = election.electionVersion;
      // We might modify the maps, for safety reason, we need to create a copy
      this.openVoteByPublicKey = new HashMap<>(election.openVoteByPublicKey);
      this.encryptedVoteByPublicKey = new HashMap<>(election.encryptedVoteByPublicKey);
      this.messageMap = new HashMap<>(election.messageMap);
      this.state = election.state;
      this.results = election.results;
    }

    public ElectionBuilder setName(@NonNull String name) {
      this.name = name;
      return this;
    }

    public ElectionBuilder setStart(long start) {
      this.start = start;
      return this;
    }

    public ElectionBuilder setEnd(long end) {
      this.end = end;
      return this;
    }

    public ElectionBuilder setElectionQuestions(@NonNull List<ElectionQuestion> electionQuestions) {
      this.electionQuestions = electionQuestions;
      return this;
    }

    public ElectionBuilder setElectionKey(@NonNull String electionKey) {
      this.electionKey = electionKey;
      return this;
    }

    public ElectionBuilder setElectionVersion(@NonNull ElectionVersion electionVersion) {
      this.electionVersion = electionVersion;
      return this;
    }

    public ElectionBuilder updateOpenBallotVotesBySender(
        @NonNull PublicKey senderPk, @NonNull List<ElectionVote> votes) {
      this.openVoteByPublicKey.put(senderPk, votes);
      return this;
    }

    public ElectionBuilder updateEncryptedVotesBySender(
        @NonNull PublicKey senderPk, @NonNull List<ElectionEncryptedVote> votes) {
      this.encryptedVoteByPublicKey.put(senderPk, votes);
      return this;
    }

    public ElectionBuilder updateMessageMap(
        @NonNull PublicKey senderPk, @NonNull MessageID messageID) {
      this.messageMap.put(messageID, senderPk);
      return this;
    }

    public ElectionBuilder setState(@NonNull EventState state) {
      this.state = state;
      return this;
    }

    public ElectionBuilder setResults(@NonNull Map<String, Set<QuestionResult>> results) {
      this.results = results;
      return this;
    }

    public Election build() {
      return new Election(
          this.id,
          this.name,
          this.creation,
          this.channel,
          this.start,
          this.end,
          this.electionQuestions,
          this.electionKey,
          this.electionVersion,
          this.openVoteByPublicKey,
          this.encryptedVoteByPublicKey,
          this.messageMap,
          this.state,
          this.results);
    }
  }
}
