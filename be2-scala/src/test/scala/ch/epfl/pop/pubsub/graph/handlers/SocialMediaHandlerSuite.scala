package ch.epfl.pop.pubsub.graph.handlers

import akka.actor.{Actor, ActorSystem, Props, Status}
import akka.pattern.AskableActorRef
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import ch.epfl.pop.model.objects.DbActorNAckException
import ch.epfl.pop.pubsub.graph.PipelineError
import ch.epfl.pop.storage.DbActor
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.{AnyFunSuiteLike => FunSuiteLike}
import org.scalatest.matchers.should.Matchers
import util.examples.LaoDataExample
import util.examples.data.{AddChirpMessages, AddReactionMessages, DeleteChirpMessages, DeleteReactionMessages}

import scala.concurrent.duration.FiniteDuration

class SocialMediaHandlerSuite extends TestKit(ActorSystem("SocialMedia-DB-System")) with FunSuiteLike with ImplicitSender with Matchers with BeforeAndAfterAll {
  // Implicits for system actors
  implicit val duration: FiniteDuration = FiniteDuration(5, "seconds")
  implicit val timeout: Timeout = Timeout(duration)

  override def afterAll(): Unit = {
    // Stops the testKit
    TestKit.shutdownActorSystem(system)
  }

  def mockDbWithNack: AskableActorRef = {
    val dbActorMock = Props(new Actor() {
      override def receive: Receive = {
        // You can modify the following match case to include more args, names...
        case m @ DbActor.WriteAndPropagate(_, _) =>
          system.log.info("Received {}", m)
          system.log.info("Responding with a Nack")

          sender() ! Status.Failure(DbActorNAckException(1, "error"))
        case x =>
          system.log.info(s"Received - error $x")
      }
    })
    system.actorOf(dbActorMock)
  }

  def mockDbWithAck: AskableActorRef = {
    val dbActorMock = Props(new Actor() {
      override def receive: Receive = {
        // You can modify the following match case to include more args, names...
        case m: DbActor.WriteAndPropagate =>
          system.log.info("Received {}", m)
          system.log.info("Responding with a Ack")

          sender() ! DbActor.DbActorAck()

        case m: DbActor.ReadLaoData =>
          system.log.info("Received {}", m)
          system.log.info("Responding with a Ack")

          sender() ! DbActor.DbActorReadLaoDataAck(LaoDataExample.LAODATA)
      }
    })
    system.actorOf(dbActorMock)
  }

  def mockDbWithAckAndNotifyNAck: AskableActorRef = {
    val dbActorMock = Props(new Actor() {
      override def receive: Receive = {
        // You can modify the following match case to include more args, names...
        case DbActor.WriteAndPropagate(channel, message) =>
          if (channel == AddChirpMessages.CHANNEL) {
            system.log.info(s"Received WAP on channel $channel")
            system.log.info("Responding with a Ack")

            sender() ! DbActor.DbActorAck()
          } else {
            system.log.info(s"Received WAP on channel $channel")
            system.log.info("Responding with a NAck")

            sender() ! Status.Failure(DbActorNAckException(1, "error"))
          }

        case m: DbActor.ReadLaoData =>
          system.log.info("Received {}", m)
          system.log.info("Responding with a Ack")

          sender() ! DbActor.DbActorReadLaoDataAck(LaoDataExample.LAODATA)
      }
    })
    system.actorOf(dbActorMock)
  }

  def mockDbWithAckButEmptyAckLaoData: AskableActorRef = {
    val dbActorMock = Props(new Actor() {
      override def receive: Receive = {
        // You can modify the following match case to include more args, names...
        case m: DbActor.WriteAndPropagate =>
          system.log.info("Received {}", m)
          system.log.info("Responding with a Ack")

          sender() ! DbActor.DbActorAck()

        case m: DbActor.ReadLaoData =>
          system.log.info("Received {}", m)
          system.log.info("Responding with a NAck")

          sender() ! Status.Failure(DbActorNAckException(1, "No corresponding lao data (mocked)"))
      }
    })
    system.actorOf(dbActorMock)
  }

  def mockDbWithAckButNAckLaoData: AskableActorRef = {
    val dbActorMock = Props(new Actor() {
      override def receive: Receive = {
        // You can modify the following match case to include more args, names...
        case m: DbActor.WriteAndPropagate =>
          system.log.info("Received {}", m)
          system.log.info("Responding with a Ack")

          sender() ! DbActor.DbActorAck()

        case m: DbActor.ReadLaoData =>
          system.log.info("Received {}", m)
          system.log.info("Responding with a NAck")

          sender() ! Status.Failure(DbActorNAckException(1, "error"))
      }
    })
    system.actorOf(dbActorMock)
  }

  test("AddReaction fails if the database fails storing the message") {
    val mockedDB = mockDbWithNack
    val rc = new SocialMediaHandler(mockedDB)
    val request = AddReactionMessages.addReaction

    rc.handleAddReaction(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("AddReaction succeeds if the database succeeds storing the message") {
    val mockedDB = mockDbWithAck
    val rc = new SocialMediaHandler(mockedDB)
    val request = AddReactionMessages.addReaction

    rc.handleAddReaction(request) should equal(Right(request))

    system.stop(mockedDB.actorRef)
  }

  test("DeleteReaction fails if the database fails storing the message") {
    val mockedDB = mockDbWithNack
    val rc = new SocialMediaHandler(mockedDB)
    val request = DeleteReactionMessages.deleteReaction

    rc.handleDeleteReaction(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("DeleteReaction succeeds if the database succeeds storing the message") {
    val mockedDB = mockDbWithAck
    val rc = new SocialMediaHandler(mockedDB)
    val request = DeleteReactionMessages.deleteReaction

    rc.handleDeleteReaction(request) should equal(Right(request))

    system.stop(mockedDB.actorRef)
  }

  test("AddChirp fails if the database fails storing the message") {
    val mockedDB = mockDbWithNack
    val rc = new SocialMediaHandler(mockedDB)
    val request = AddChirpMessages.addChirp

    rc.handleAddChirp(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("AddChirp fails if the database provides 'None' laoId") {
    val mockedDB = mockDbWithAckButEmptyAckLaoData
    val rc = new SocialMediaHandler(mockedDB)
    val request = AddChirpMessages.addChirp

    rc.handleAddChirp(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("AddChirp fails if the database fails to provide laoId") {
    val mockedDB = mockDbWithAckButNAckLaoData
    val rc = new SocialMediaHandler(mockedDB)
    val request = AddChirpMessages.addChirp

    rc.handleAddChirp(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("AddChirp succeeds if the database succeeds storing the message and managing the notify") {
    val mockedDB = mockDbWithAck
    val rc = new SocialMediaHandler(mockedDB)
    val request = AddChirpMessages.addChirp

    rc.handleAddChirp(request) should equal(Right(request))

    system.stop(mockedDB.actorRef)
  }

  test("AddChirp fails if the database succeeds storing the message and fails the notify") {
    val mockedDB = mockDbWithAckAndNotifyNAck
    val rc = new SocialMediaHandler(mockedDB)
    val request = AddChirpMessages.addChirp

    rc.handleAddChirp(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("DeleteChirp fails if the database fails storing the message") {
    val mockedDB = mockDbWithNack
    val rc = new SocialMediaHandler(mockedDB)
    val request = DeleteChirpMessages.deleteChirp

    rc.handleDeleteChirp(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("DeleteChirp succeeds if the database succeeds storing the message and managing the notify") {
    val mockedDB = mockDbWithAck
    val rc = new SocialMediaHandler(mockedDB)
    val request = DeleteChirpMessages.deleteChirp

    rc.handleDeleteChirp(request) should equal(Right(request))

    system.stop(mockedDB.actorRef)
  }

  test("DeleteChirp fails if the database succeeds storing the message but fails the notify") {
    val mockedDB = mockDbWithAckAndNotifyNAck
    val rc = new SocialMediaHandler(mockedDB)
    val request = DeleteChirpMessages.deleteChirp

    rc.handleDeleteChirp(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("DeleteChirp fails if the database provides 'None' laoId") {
    val mockedDB = mockDbWithAckButEmptyAckLaoData
    val rc = new SocialMediaHandler(mockedDB)
    val request = DeleteChirpMessages.deleteChirp

    rc.handleDeleteChirp(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

  test("DeleteChirp fails if the database fails to provide laoId") {
    val mockedDB = mockDbWithAckButNAckLaoData
    val rc = new SocialMediaHandler(mockedDB)
    val request = DeleteChirpMessages.deleteChirp

    rc.handleDeleteChirp(request) shouldBe an[Left[PipelineError, _]]

    system.stop(mockedDB.actorRef)
  }

}
