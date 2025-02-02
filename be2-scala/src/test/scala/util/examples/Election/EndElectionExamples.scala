package util.examples.Election

import ch.epfl.pop.json.MessageDataProtocol._
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.election.EndElection
import ch.epfl.pop.model.objects._
import spray.json._
import util.examples.Election.CastVoteElectionExamples.{NOT_STALE_CREATED_BEFORE_OPENING_THE_ELECTION, VOTE_ID}

object EndElectionExamples {

  final val SENDER: PublicKey = SetupElectionExamples.SENDER_SETUPELECTION
  final val VOTING_METHOD: String = SetupElectionExamples.VOTING_METHOD
  final val SIGNATURE: Signature = SetupElectionExamples.SIGNATURE

  final val ID: Hash = Hash(Base64Data.encode("election"))
  final val LAO_ID: Hash = Hash(Base64Data.encode("laoId"))
  final val NOT_STALE_CREATED_AT = Timestamp(1649089855L)
  final val NOT_STALE_CREATED_BEFORE_SETUP = Timestamp(1649089850L)
  final val REGISTERED_VOTES: Hash = Hash.fromStrings(VOTE_ID.toString)

  val invalidTimestamp: Timestamp = Timestamp(0)
  val invalidId: Hash = Hash(Base64Data.encode("wrong"))
  val invalidSender: PublicKey = PublicKey(Base64Data.encode("wrong"))

  val workingEndElection: EndElection = EndElection(LAO_ID, ID, NOT_STALE_CREATED_AT, REGISTERED_VOTES)
  final val DATA_END_ELECTION_MESSAGE: Hash = Hash(Base64Data.encode(workingEndElection.toJson.toString))
  final val MESSAGE_END_ELECTION_WORKING: Message = new Message(
    DATA_END_ELECTION_MESSAGE.base64Data,
    SENDER,
    SIGNATURE,
    Hash(Base64Data("")),
    List.empty,
    Some(workingEndElection)
  )

  val beforeSetupEndElection: EndElection = EndElection(LAO_ID, ID, NOT_STALE_CREATED_BEFORE_SETUP, REGISTERED_VOTES)
  final val DATA_END_ELECTION_BEFORE_SETUP_MESSAGE: Hash = Hash(Base64Data.encode(beforeSetupEndElection.toJson.toString))
  final val MESSAGE_END_ELECTION_BEFORE_SETUP: Message = new Message(
    DATA_END_ELECTION_BEFORE_SETUP_MESSAGE.base64Data,
    SENDER,
    SIGNATURE,
    Hash(Base64Data("")),
    List.empty,
    Some(beforeSetupEndElection)
  )
  val wrongTimestampEndElection: EndElection = EndElection(LAO_ID, ID, invalidTimestamp, REGISTERED_VOTES)
  final val MESSAGE_END_ELECTION_WRONG_TIMESTAMP: Message = new Message(
    Base64Data.encode(wrongTimestampEndElection.toJson.toString),
    SENDER,
    SIGNATURE,
    Hash(Base64Data("")),
    List.empty,
    Some(wrongTimestampEndElection)
  )

  val wrongIdEndElection: EndElection = EndElection(LAO_ID, invalidId, NOT_STALE_CREATED_AT, REGISTERED_VOTES)
  final val MESSAGE_END_ELECTION_WRONG_ID: Message = new Message(
    Base64Data.encode(wrongIdEndElection.toJson.toString),
    SENDER,
    SIGNATURE,
    Hash(Base64Data("")),
    List.empty,
    Some(wrongIdEndElection)
  )

  val wrongLaoIdEndElection: EndElection = EndElection(invalidId, ID, NOT_STALE_CREATED_AT, REGISTERED_VOTES)
  final val MESSAGE_END_ELECTION_WRONG_LAO_ID: Message = new Message(
    Base64Data.encode(wrongLaoIdEndElection.toJson.toString),
    SENDER,
    SIGNATURE,
    Hash(Base64Data("")),
    List.empty,
    Some(wrongIdEndElection)
  )

  final val MESSAGE_END_ELECTION_WRONG_OWNER: Message = new Message(
    Base64Data.encode(workingEndElection.toJson.toString),
    invalidSender,
    SIGNATURE,
    Hash(Base64Data("")),
    List.empty,
    Some(workingEndElection)
  )
}
