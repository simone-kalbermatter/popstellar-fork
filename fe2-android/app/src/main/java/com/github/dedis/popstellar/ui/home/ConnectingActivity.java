package com.github.dedis.popstellar.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.dedis.popstellar.R;
import com.github.dedis.popstellar.databinding.ConnectingActivityBinding;
import com.github.dedis.popstellar.model.network.method.message.data.lao.CreateLao;
import com.github.dedis.popstellar.model.objects.Channel;
import com.github.dedis.popstellar.model.objects.Lao;
import com.github.dedis.popstellar.model.objects.security.PublicKey;
import com.github.dedis.popstellar.repository.remote.GlobalNetworkManager;
import com.github.dedis.popstellar.ui.lao.LaoActivity;
import com.github.dedis.popstellar.utility.Constants;
import com.github.dedis.popstellar.utility.error.ErrorUtils;
import com.github.dedis.popstellar.utility.security.KeyManager;
import com.tinder.scarlet.WebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observables.ConnectableObservable;
import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

@AndroidEntryPoint
public class ConnectingActivity extends AppCompatActivity {
  public static final String TAG = ConnectingActivity.class.getSimpleName();

  private final CompositeDisposable disposables = new CompositeDisposable();
  private ConnectingActivityBinding binding;

  @Inject GlobalNetworkManager networkManager;
  @Inject KeyManager keyManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ConnectingActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupCancelButton();
    setupConnectingText();
    handleOpenConnection();
  }

  @Override
  public void onPause() {
    super.onPause();
    disposables.dispose();
  }

  private void setupConnectingText() {
    if (getIntent().getStringExtra(Constants.ACTIVITY_TO_OPEN_EXTRA).equals(Constants.HOME_EXTRA)) {
      binding.connectingLao.setText(R.string.stored_channels);
    } else if (getIntent()
        .getStringExtra(Constants.CONNECTION_PURPOSE_EXTRA)
        .equals(Constants.JOINING_EXTRA)) {
      binding.connectingLao.setText(getIntent().getStringExtra(Constants.LAO_ID_EXTRA));
    } else if (getIntent()
        .getStringExtra(Constants.CONNECTION_PURPOSE_EXTRA)
        .equals(Constants.CREATING_EXTRA)) {
      binding.connectingText.setText(R.string.creating_new_lao);
      binding.connectingLao.setText(getIntent().getStringExtra(Constants.LAO_NAME));
    }
  }

  /**
   * Waits in background for the connection to the new server address to be opened And then starts
   * call for the subscribe and catchup process
   */
  private void handleOpenConnection() {
    String toOpen = getIntent().getStringExtra(Constants.ACTIVITY_TO_OPEN_EXTRA);
    boolean isDestinationHome = toOpen.equals(Constants.HOME_EXTRA);

    // This object will allow us to get all the event of the underlying observable, even if they
    // were emitted prior to the subscribe
    ConnectableObservable<WebSocket.Event> replay =
        networkManager.getMessageSender().getConnectEvents().replay();
    disposables.add(
        replay.subscribe(
            v -> {
              Timber.tag(TAG).d("connect message is %s", v);
              if (v instanceof WebSocket.Event.OnConnectionOpened) {
                Timber.tag(TAG).d("connection opened with new server address");
                handleConnecting(isDestinationHome);
              } else if (v instanceof WebSocket.Event.OnConnectionFailed) {
                startActivity(HomeActivity.newIntent(this));
                disposables.dispose();
                finish();
              }
            },
            e -> Timber.tag(TAG).e(e, "error on subscription to connection events")));

    // Now that we observe the events we let the observable know it can begin to emit events
    replay.connect();
  }

  /** Handles the subscription and catchup process to an LAO */
  private void handleConnecting(boolean isDestinationHome) {
    if (isDestinationHome) {
      // This is when we reconnect to laos, therefore returning home
      Timber.tag(TAG).d("Opening Home activity");
      startActivity(HomeActivity.newIntent(this));
      finish();
      return;
    }
    // We are either joining an LAO or creating a new one

    boolean isCreation =
        getIntent()
            .getExtras()
            .getString(Constants.CONNECTION_PURPOSE_EXTRA)
            .equals(Constants.CREATING_EXTRA);

    if (isCreation) {
      String laoName = getIntent().getExtras().getString(Constants.LAO_NAME);
      List<String> witnessesList = getIntent().getStringArrayListExtra(Constants.WITNESSES);
      List<PublicKey> witnesses =
          witnessesList.stream().map(PublicKey::new).collect(Collectors.toList());

      // Add the organizer to the list of witnesses
      witnesses.add(keyManager.getMainPublicKey());

      CreateLao createLao = new CreateLao(laoName, keyManager.getMainPublicKey(), witnesses);
      Lao lao = new Lao(createLao.getId());
      Timber.tag(TAG).d("Creating Lao %s", lao.getId());
      disposables.add(
          networkManager
              .getMessageSender()
              .publish(keyManager.getMainKeyPair(), Channel.ROOT, createLao)
              .subscribe(
                  () -> {
                    Timber.tag(TAG).d("got success result for create lao with id %s", lao.getId());
                    subscribeToLao(lao); // Subscribe to the newly created Lao
                  }));
    } else { // Joining an existing lao
      String laoId = getIntent().getExtras().getString(Constants.LAO_ID_EXTRA);
      Lao lao = new Lao(laoId);
      subscribeToLao(lao); // Subscribe to the existing lao
    }
  }

  private void subscribeToLao(Lao lao) {
    Timber.tag(TAG).d("connecting to lao %s", lao.getChannel());
    disposables.add(
        networkManager
            .getMessageSender()
            .subscribe(lao.getChannel())
            .subscribe(
                () -> {
                  Timber.tag(TAG).d("subscribing to LAO with id %s", lao.getId());
                  startActivity(LaoActivity.newIntentForLao(this, lao.getId()));
                  finish();
                },
                error -> {
                  // In case of error, log it and go to home activity
                  ErrorUtils.logAndShow(getApplication(), TAG, error, R.string.error_subscribe_lao);
                  startActivity(HomeActivity.newIntent(this));
                  finish();
                }));
  }

  private void setupCancelButton() {
    Button cancelButton = findViewById(R.id.button_cancel_connecting);
    cancelButton.setOnClickListener(
        v -> {
          startActivity(HomeActivity.newIntent(this));
          finish();
        });
  }

  public static Intent newIntentForJoiningDetail(Context ctx, String laoId) {
    Intent intent = new Intent(ctx, ConnectingActivity.class);
    intent.putExtra(Constants.LAO_ID_EXTRA, laoId);
    intent.putExtra(Constants.CONNECTION_PURPOSE_EXTRA, Constants.JOINING_EXTRA);
    intent.putExtra(Constants.ACTIVITY_TO_OPEN_EXTRA, Constants.LAO_DETAIL_EXTRA);
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
    return intent;
  }

  public static Intent newIntentForCreatingDetail(
      Context ctx, String laoName, List<PublicKey> witnesses) {
    Intent intent = new Intent(ctx, ConnectingActivity.class);
    intent.putExtra(Constants.LAO_NAME, laoName);
    intent.putStringArrayListExtra(
        Constants.WITNESSES,
        new ArrayList<>(
            witnesses.stream().map(PublicKey::getEncoded).collect(Collectors.toList())));
    intent.putExtra(Constants.CONNECTION_PURPOSE_EXTRA, Constants.CREATING_EXTRA);
    intent.putExtra(Constants.ACTIVITY_TO_OPEN_EXTRA, Constants.LAO_DETAIL_EXTRA);
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
    return intent;
  }
}
