package com.github.dedis.popstellar.repository.remote;

import com.github.dedis.popstellar.model.objects.Channel;
import com.github.dedis.popstellar.utility.handler.MessageHandler;
import com.github.dedis.popstellar.utility.scheduler.TestSchedulerProvider;
import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GlobalNetworkManagerTest {

  @Mock MessageHandler handler;
  @Mock Gson gson;

  @Test
  public void initializationProducesAValidConnection() {
    TestSchedulerProvider schedulerProvider = new TestSchedulerProvider();
    TestScheduler testScheduler = schedulerProvider.getTestScheduler();
    ConnectionFactory factory = mock(ConnectionFactory.class);

    MultiConnection firstConnection = mock(MultiConnection.class);
    when(firstConnection.observeMessage()).thenReturn(BehaviorSubject.create());
    when(firstConnection.observeConnectionEvents()).thenReturn(BehaviorSubject.create());

    when(factory.createMultiConnection(anyString())).thenReturn(firstConnection);

    GlobalNetworkManager networkManager =
        new GlobalNetworkManager(handler, factory, gson, schedulerProvider);
    verify(factory).createMultiConnection(anyString());

    Completable sendMessage = networkManager.getMessageSender().unsubscribe(Channel.ROOT);
    verify(firstConnection, never()).sendMessage(any());

    Disposable disposable = sendMessage.subscribe();
    testScheduler.advanceTimeBy(5, TimeUnit.SECONDS);
    disposable.dispose();

    verify(firstConnection).sendMessage(any());
  }

  @Test
  public void connectingToANewConnectionClosesTheLast() {
    ConnectionFactory factory = mock(ConnectionFactory.class);

    MultiConnection firstConnection = mock(MultiConnection.class);
    when(firstConnection.observeMessage()).thenReturn(BehaviorSubject.create());
    when(firstConnection.observeConnectionEvents()).thenReturn(BehaviorSubject.create());

    when(factory.createMultiConnection(anyString())).thenReturn(firstConnection);

    GlobalNetworkManager networkManager =
        new GlobalNetworkManager(handler, factory, gson, new TestSchedulerProvider());
    verify(factory).createMultiConnection(anyString());

    MultiConnection secondConnection = mock(MultiConnection.class);
    when(secondConnection.observeMessage()).thenReturn(BehaviorSubject.create());
    when(secondConnection.observeConnectionEvents()).thenReturn(BehaviorSubject.create());

    when(factory.createMultiConnection(anyString())).thenReturn(secondConnection);
    networkManager.connect("new url");

    verify(firstConnection).close();
  }
}
