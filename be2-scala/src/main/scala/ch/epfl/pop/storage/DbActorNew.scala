package ch.epfl.pop.storage

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.ObjectType
import ch.epfl.pop.model.objects._
import ch.epfl.pop.pubsub.PubSubMediator
import ch.epfl.pop.pubsub.graph.ErrorCodes
import ch.epfl.pop.storage.DbActorNew._

import scala.util.{Failure, Success, Try}

case class DbActorNew(
                       private val mediatorRef: ActorRef,
                       private val storage: Storage = new DiskStorage()
                     ) extends Actor with ActorLogging {

  /**
   * Response for a negative db request
   *
   * @note [[DbActorNAck]] is used to manage the internal state of the actor,
   * but should not be exposed (e.g. sent back)
   */
  private[this] case class DbActorNAck() extends DbActorMessage

  override def postStop(): Unit = {
    storage.close()
    super.postStop()
  }



  /* --------------- Functions handling messages DbActor may receive --------------- */

  private def write(channel: Channel, message: Message): DbActorMessage = {
    // create channel if missing. If already present => createChannel does nothing
    val _object = message.decodedData match {
      case Some(data) => data._object
      case _ => ObjectType.LAO
    }
    createChannel(channel, _object)

    val channelData: ChannelData = readChannelData(channel).channelData
    storage.write(
      (channel.toString, channelData.addMessage(message.message_id).toJsonString),
      (s"$channel:${message.message_id}", message.toJsonString)
    )

    DbActorAck()
  }

  private def read(channel: Channel, messageId: Hash): DbActorMessage = ???

  private def readChannelData(channel: Channel): DbActorReadChannelDataAck = ???

  private def readLaoData(channel: Channel): DbActorMessage = ???

  private def catchupChannel(channel: Channel): DbActorMessage = ???

  private def writeAndPropagate(channel: Channel, message: Message): DbActorMessage = {
    val answer: DbActorMessage = write(channel, message)
    mediatorRef ! PubSubMediator.Propagate(channel, message)
    answer
  }

  private def createChannel(channel: Channel, objectType: ObjectType.ObjectType): DbActorAck = {
    checkChannelExistence(channel) match {
      case DbActorAck() => DbActorAck() // do nothing if the channel already exists
      case _ =>
        Try(storage.write(channel.toString -> ChannelData(objectType, List.empty).toJsonString)) match {
          case Success(_) => DbActorAck()
          case Failure(ex) => throw DbActorNAckException(ErrorCodes.SERVER_ERROR.id, ex.getMessage)
        }
    }
  }

  private def createChannels(list: List[(Channel, ObjectType.ObjectType)]): DbActorAck = {

    @scala.annotation.tailrec
    def filterExistingChannels(
                                list: List[(Channel, ObjectType.ObjectType)],
                                acc: List[(Channel, ObjectType.ObjectType)]
                              ): List[(Channel, ObjectType.ObjectType)] = {
      list match {
        case Nil => acc
        case head :: tail => checkChannelExistence(head._1) match {
          case DbActorAck() => filterExistingChannels(tail, acc) // already exists in db
          case _ => filterExistingChannels(tail, head :: acc) // does not exist in db
        }
      }
    }

    // removing channels already present in the db from the list
    val filtered: List[(Channel, ObjectType.ObjectType)] = filterExistingChannels(list, Nil)
    // creating ChannelData from the filtered input
    val mapped: List[(String, String)] = filtered.map { case (c, o) => (c.toString, ChannelData(o, List.empty).toJsonString) }

    Try(storage.write(mapped : _*)) match {
      case Success(_) => DbActorAck()
      case Failure(ex) => throw DbActorNAckException(ErrorCodes.SERVER_ERROR.id, ex.getMessage)
    }
  }

  private def checkChannelExistence(channel: Channel): DbActorMessage = {
    try {
      storage.read(channel.toString) match {
        case Some(_) => DbActorAck()
        case _ => DbActorNAck()
      }
    } catch {
      case _: Throwable => DbActorNAck() // TODO REFACTORING make the receive throw on DbActorNAck return (waiting for the full actor to be finished)
    }
  }

  private def addWitnessSignature(messageId: Hash, signature: Signature): DbActorMessage = {
    throw DbActorNAckException(
      ErrorCodes.SERVER_ERROR.id,
      s"NOT IMPLEMENTED: database actor cannot handle AddWitnessSignature requests yet"
    )
  }




  override def receive: Receive = LoggingReceive {
    case DbActorNew.Write(channel, message) =>
      log.info(s"Actor $self (db) received a WRITE request on channel '$channel'")
      sender() ! write(channel, message)

    case Read(channel, messageId) =>
      log.info(s"Actor $self (db) received a READ request for message_id '$messageId' from channel '$channel'")
      sender() ! read(channel, messageId)

    case ReadChannelData(channel) =>
      log.info(s"Actor $self (db) received a ReadChannelData request from channel '$channel'")
      sender() ! readChannelData(channel)

    case ReadLaoData(channel) =>
      log.info(s"Actor $self (db) received a ReadLaoData request")
      sender() ! readLaoData(channel)

    case Catchup(channel) =>
      log.info(s"Actor $self (db) received a CATCHUP request for channel '$channel'")
      sender() ! catchupChannel(channel)

    case WriteAndPropagate(channel, message) =>
      log.info(s"Actor $self (db) received a WriteAndPropagate request on channel '$channel'")
      sender() ! writeAndPropagate(channel, message)

    case CreateChannel(channel, objectType) =>
      log.info(s"Actor $self (db) received an CreateChannel request for channel '$channel' of type '$objectType'")
      sender() ! createChannel(channel, objectType)

    case CreateChannelsFromList(list) =>
      log.info(s"Actor $self (db) received a CreateChannelsFromList request for list $list")
      sender() ! createChannels(list)

    case ChannelExists(channel) =>
      log.info(s"Actor $self (db) received an ChannelExists request for channel '$channel'")
      sender() ! checkChannelExistence(channel)

    case AddWitnessSignature(messageId, signature) =>
      log.info(s"Actor $self (db) received an AddWitnessSignature request for message_id '$messageId'")
      sender() ! addWitnessSignature(messageId, signature)

    case m@_ =>
      log.info(s"Actor $self (db) received an unknown message")
      throw DbActorNAckException(
        ErrorCodes.SERVER_ERROR.id,
        s"database actor received a message '$m' that it could not recognize"
      )
  }
}

object DbActorNew {
  // DbActor Events correspond to messages the actor may receive
  sealed trait Event

  /**
   * Request to write a message in the database
   *
   * @param channel the channel where the message should be published
   * @param message the message to write in the database
   */
  final case class Write(channel: Channel, message: Message) extends Event


  /**
   * Request to read a specific message with id <messageId> from <channel>
   *
   * @param channel the channel where the message was published
   * @param messageId the id of the message (message_id) we want to read
   */
  final case class Read(channel: Channel, messageId: Hash) extends Event

  /**
   * Request to read the channelData from <channel>, with key laoId/channel
   *
   * @param channel
   * the channel we need the data for
   */
  final case class ReadChannelData(channel: Channel) extends Event

  /**
   * Request to read the laoData of the LAO, with key laoId
   *
   * @param channel
   * the channel we need the LAO's data for
   */
  final case class ReadLaoData(channel: Channel) extends Event

  /**
   * Request to read all messages from a specific <channel>
   *
   * @param channel the channel where the messages should be fetched
   */
  final case class Catchup(channel: Channel) extends Event

  /**
   * Request to write a <message> in the database and propagate said message
   * to clients subscribed to the <channel>
   *
   * @param channel the channel where the message should be published
   * @param message the message to write in db and propagate to clients
   * @note DbActor will answer with a [[DbActorWriteAck]] if successful since the propagation cannot fail
   */
  final case class WriteAndPropagate(channel: Channel, message: Message) extends Event

  /**
   * Request to create channel <channel> in the db with a type
   *
   * @param channel    channel to create
   * @param objectType channel type
   */
  final case class CreateChannel(channel: Channel, objectType: ObjectType.ObjectType) extends Event

  /**
   * Request to create List of channels in the db with given types
   *
   * @param list list from which channels are created
   */
  final case class CreateChannelsFromList(list: List[(Channel, ObjectType.ObjectType)]) extends Event

  /** Request to check if channel <channel> exists in the db
   *
   * @param channel targeted channel
   * @note db answers with a simple boolean
   */
  final case class ChannelExists(channel: Channel) extends Event

  /**
   * Request to append witness <signature> to a stored message with message_id
   * <messageId>
   *
   * @param messageId message_id of the targeted message
   * @param signature signature to append to the witness signature list of the message
   */
  final case class AddWitnessSignature(messageId: Hash, signature: Signature) extends Event

  // DbActor DbActorMessage correspond to messages the actor may emit
  sealed trait DbActorMessage

  /**
   * Response for a [[Write]] db request Receiving [[DbActorWriteAck]] works as
   * an acknowledgement that the write request was successful
   */
  final case class DbActorWriteAck() extends DbActorMessage

  /**
   * Response for a [[Read]] db request Receiving [[DbActorReadAck]] works as
   * an acknowledgement that the read request was successful
   *
   * @param message requested message
   */
  final case class DbActorReadAck(message: Option[Message]) extends DbActorMessage

  /**
   * Response for a [[ReadChannelData]] db request Receiving [[DbActorReadChannelDataAck]] works as
   * an acknowledgement that the request was successful
   *
   * @param channelData requested channel data
   */
  final case class DbActorReadChannelDataAck(channelData: ChannelData) extends DbActorMessage

  /**
   * Response for a [[ReadLaoData]] db request Receiving [[DbActorReadLaoDataAck]] works as
   * an acknowledgement that the request was successful
   *
   * @param laoData requested lao data
   */
  final case class DbActorReadLaoDataAck(laoData: LaoData) extends DbActorMessage

  /**
   * Response for a [[Catchup]] db request Receiving [[DbActorCatchupAck]]
   * works as an acknowledgement that the catchup request was successful
   *
   * @param messages requested messages
   */
  final case class DbActorCatchupAck(messages: List[Message]) extends DbActorMessage

  /**
   * Response for a general db actor ACK
   */
  final case class DbActorAck() extends DbActorMessage

}
