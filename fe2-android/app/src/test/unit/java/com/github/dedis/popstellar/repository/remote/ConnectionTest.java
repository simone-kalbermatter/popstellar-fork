package com.github.dedis.popstellar.repository.remote;

import com.github.dedis.popstellar.model.network.GenericMessage;
import com.github.dedis.popstellar.model.network.answer.Result;
import com.github.dedis.popstellar.model.network.method.Message;
import com.github.dedis.popstellar.model.network.method.Subscribe;
import com.github.dedis.popstellar.model.objects.Channel;
import com.github.dedis.popstellar.model.objects.PeerAddress;
import com.tinder.scarlet.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ConnectionTest {

  public static final String URL = "url";

  @Test
  public void sendMessageDelegatesToService() {
    LAOService service = mock(LAOService.class);
    BehaviorSubject<GenericMessage> messages = BehaviorSubject.create();
    when(service.observeMessage()).thenReturn(messages);
    when(service.observeWebsocket()).thenReturn(BehaviorSubject.create());

    BehaviorSubject<Lifecycle.State> manualState = BehaviorSubject.create();

    Connection connection = new Connection("url", service, manualState);
    Message msg = new Subscribe(Channel.ROOT, 12);

    connection.sendMessage(msg);

    verify(service).sendMessage(msg);
    verify(service).observeMessage();
    verify(service, atLeastOnce()).observeWebsocket();
    verifyNoMoreInteractions(service);
  }

  @Test
  public void observeMessageDelegatesToService() {
    LAOService service = mock(LAOService.class);
    BehaviorSubject<GenericMessage> messages = BehaviorSubject.create();
    when(service.observeMessage()).thenReturn(messages);
    when(service.observeWebsocket()).thenReturn(BehaviorSubject.create());

    BehaviorSubject<Lifecycle.State> manualState = BehaviorSubject.create();

    // Create connection and retrieve events
    Connection connection = new Connection(URL, service, manualState);
    Observable<GenericMessage> connectionMessages = connection.observeMessage();
    // Publish message to the pipeline
    GenericMessage message = new Result(5);
    messages.onNext(message);

    // Make sure the event was receive
    connectionMessages.test().assertValueCount(1).assertValue(message);

    verify(service).observeMessage();
    verify(service, atLeastOnce()).observeWebsocket();
    verifyNoMoreInteractions(service);
  }

  @Test
  public void observeWebsocketDelegatesToService() {
    LAOService service = mock(LAOService.class);
    BehaviorSubject<GenericMessage> messages = BehaviorSubject.create();
    BehaviorSubject<WebSocket.Event> events = BehaviorSubject.create();
    when(service.observeMessage()).thenReturn(messages);
    when(service.observeWebsocket()).thenReturn(events);

    BehaviorSubject<Lifecycle.State> manualState = BehaviorSubject.create();

    // Create connection and retrieve events
    Connection connection = new Connection("url", service, manualState);
    Observable<WebSocket.Event> connectionEvents = connection.observeConnectionEvents();
    // Publish event to the pipeline
    WebSocket.Event event = new WebSocket.Event.OnConnectionOpened<>("Fake WebSocket");
    events.onNext(event);

    // Make sure the event was receive
    connectionEvents.test().assertValueCount(1).assertValue(event);

    verify(service).observeMessage();
    verify(service, atLeastOnce()).observeWebsocket();
    verifyNoMoreInteractions(service);
  }

  @Test
  public void connectionClosesGracefully() {
    LAOService service = mock(LAOService.class);
    BehaviorSubject<GenericMessage> messages = BehaviorSubject.create();
    when(service.observeMessage()).thenReturn(messages);
    when(service.observeWebsocket()).thenReturn(BehaviorSubject.create());

    BehaviorSubject<Lifecycle.State> manualState =
        BehaviorSubject.createDefault(Lifecycle.State.Started.INSTANCE);

    Connection connection = new Connection("url", service, manualState);
    connection.close();

    assertEquals(
        new Lifecycle.State.Stopped.WithReason(ShutdownReason.GRACEFUL), manualState.getValue());

    verify(service).observeMessage();
    verify(service, atLeastOnce()).observeWebsocket();
    verifyNoMoreInteractions(service);
  }

  @Test
  public void testMultiConnection() {
    LAOService service = mock(LAOService.class);
    BehaviorSubject<GenericMessage> messages = BehaviorSubject.create();
    when(service.observeMessage()).thenReturn(messages);
    when(service.observeWebsocket()).thenReturn(BehaviorSubject.create());

    BehaviorSubject<Lifecycle.State> manualState =
        BehaviorSubject.createDefault(Lifecycle.State.Started.INSTANCE);

    Function<String, Connection> provider = url -> new Connection(url, service, manualState);
    MultiConnection multiConnection = new MultiConnection(provider, "url");

    // Extend the connections with a new peer
    List<PeerAddress> peers = new ArrayList<>();
    peers.add(new PeerAddress("url2"));

    multiConnection.connectToPeers(peers);

    Message msg = new Subscribe(Channel.ROOT, 12);
    multiConnection.sendMessage(msg);

    verify(service, times(2)).sendMessage(msg);
    verify(service, times(2)).observeMessage();
    verify(service, times(2)).observeWebsocket();
    verifyNoMoreInteractions(service);
  }
}
