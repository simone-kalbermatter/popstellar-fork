package com.github.dedis.popstellar.di;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.github.dedis.popstellar.model.network.method.message.data.DataRegistry;
import com.github.dedis.popstellar.repository.*;
import com.github.dedis.popstellar.repository.database.AppDatabase;
import com.github.dedis.popstellar.utility.handler.data.*;
import com.github.dedis.popstellar.utility.security.KeyManager;

import org.mockito.Mockito;

import javax.inject.Singleton;

/** This class helps in the creation of the DataRegistry */
@Singleton
public class DataRegistryModuleHelper {

  private static final Application application = ApplicationProvider.getApplicationContext();
  private static final AppDatabase appDatabase =
      AppDatabaseModuleHelper.getAppDatabase(application);

  static {
    appDatabase.close();
  }

  public static DataRegistry buildRegistry() {
    return buildRegistry(
        new LAORepository(appDatabase, application), Mockito.mock(KeyManager.class));
  }

  public static DataRegistry buildRegistry(LAORepository laoRepository, KeyManager keyManager) {
    RollCallRepository rollCallRepository = new RollCallRepository(appDatabase, application);
    ElectionRepository electionRepository = new ElectionRepository(appDatabase, application);
    MeetingRepository meetingRepository = new MeetingRepository(appDatabase, application);
    DigitalCashRepository digitalCashRepository =
        new DigitalCashRepository(appDatabase, application);
    return buildRegistry(
        laoRepository,
        new SocialMediaRepository(appDatabase, application),
        electionRepository,
        rollCallRepository,
        meetingRepository,
        digitalCashRepository,
        new WitnessingRepository(
            appDatabase,
            application,
            rollCallRepository,
            electionRepository,
            meetingRepository,
            digitalCashRepository),
        new MessageRepository(appDatabase, application),
        keyManager,
        new ServerRepository());
  }

  public static DataRegistry buildRegistry(
      LAORepository laoRepository,
      WitnessingRepository witnessingRepository,
      KeyManager keyManager) {
    return buildRegistry(
        laoRepository,
        new SocialMediaRepository(appDatabase, application),
        new ElectionRepository(appDatabase, application),
        new RollCallRepository(appDatabase, application),
        new MeetingRepository(appDatabase, application),
        new DigitalCashRepository(appDatabase, application),
        witnessingRepository,
        new MessageRepository(appDatabase, application),
        keyManager,
        new ServerRepository());
  }

  public static DataRegistry buildRegistry(
      LAORepository laoRepository,
      KeyManager keyManager,
      RollCallRepository rollCallRepo,
      WitnessingRepository witnessingRepo) {
    return buildRegistry(
        laoRepository,
        new SocialMediaRepository(appDatabase, application),
        new ElectionRepository(appDatabase, application),
        rollCallRepo,
        new MeetingRepository(appDatabase, application),
        new DigitalCashRepository(appDatabase, application),
        witnessingRepo,
        new MessageRepository(appDatabase, application),
        keyManager,
        new ServerRepository());
  }

  public static DataRegistry buildRegistry(
      LAORepository laoRepository,
      KeyManager keyManager,
      MeetingRepository meetingRepo,
      WitnessingRepository witnessingRepo) {
    return buildRegistry(
        laoRepository,
        new SocialMediaRepository(appDatabase, application),
        new ElectionRepository(appDatabase, application),
        new RollCallRepository(appDatabase, application),
        meetingRepo,
        new DigitalCashRepository(appDatabase, application),
        witnessingRepo,
        new MessageRepository(appDatabase, application),
        keyManager,
        new ServerRepository());
  }

  public static DataRegistry buildRegistry(
      LAORepository laoRepository, ElectionRepository electionRepo, KeyManager keyManager) {
    RollCallRepository rollCallRepository = new RollCallRepository(appDatabase, application);
    MeetingRepository meetingRepository = new MeetingRepository(appDatabase, application);
    DigitalCashRepository digitalCashRepository =
        new DigitalCashRepository(appDatabase, application);
    return buildRegistry(
        laoRepository,
        new SocialMediaRepository(appDatabase, application),
        electionRepo,
        rollCallRepository,
        meetingRepository,
        digitalCashRepository,
        new WitnessingRepository(
            appDatabase,
            application,
            rollCallRepository,
            electionRepo,
            meetingRepository,
            digitalCashRepository),
        new MessageRepository(appDatabase, application),
        keyManager,
        new ServerRepository());
  }

  public static DataRegistry buildRegistry(
      LAORepository laoRepository,
      ElectionRepository electionRepo,
      WitnessingRepository witnessingRepo,
      KeyManager keyManager,
      MessageRepository messageRepo) {
    return buildRegistry(
        laoRepository,
        new SocialMediaRepository(appDatabase, application),
        electionRepo,
        new RollCallRepository(appDatabase, application),
        new MeetingRepository(appDatabase, application),
        new DigitalCashRepository(appDatabase, application),
        witnessingRepo,
        messageRepo,
        keyManager,
        new ServerRepository());
  }

  public static DataRegistry buildRegistry(
      LAORepository laoRepo,
      SocialMediaRepository socialMediaRepo,
      RollCallRepository rollCallRepo,
      KeyManager keyManager) {
    ElectionRepository electionRepository = new ElectionRepository(appDatabase, application);
    MeetingRepository meetingRepository = new MeetingRepository(appDatabase, application);
    DigitalCashRepository digitalCashRepository =
        new DigitalCashRepository(appDatabase, application);
    return buildRegistry(
        laoRepo,
        socialMediaRepo,
        electionRepository,
        rollCallRepo,
        meetingRepository,
        digitalCashRepository,
        new WitnessingRepository(
            appDatabase,
            application,
            rollCallRepo,
            electionRepository,
            meetingRepository,
            digitalCashRepository),
        new MessageRepository(appDatabase, application),
        keyManager,
        new ServerRepository());
  }

  public static DataRegistry buildRegistry(
      LAORepository laoRepo,
      WitnessingRepository witnessingRepo,
      MessageRepository msgRepo,
      KeyManager keyManager,
      ServerRepository serverRepo) {
    return buildRegistry(
        laoRepo,
        new SocialMediaRepository(appDatabase, application),
        new ElectionRepository(appDatabase, application),
        new RollCallRepository(appDatabase, application),
        new MeetingRepository(appDatabase, application),
        new DigitalCashRepository(appDatabase, application),
        witnessingRepo,
        msgRepo,
        keyManager,
        serverRepo);
  }

  public static DataRegistry buildRegistry(
      DigitalCashRepository digitalCashRepo, KeyManager keyManager) {
    RollCallRepository rollCallRepository = new RollCallRepository(appDatabase, application);
    ElectionRepository electionRepository = new ElectionRepository(appDatabase, application);
    MeetingRepository meetingRepository = new MeetingRepository(appDatabase, application);
    return buildRegistry(
        new LAORepository(appDatabase, application),
        new SocialMediaRepository(appDatabase, application),
        electionRepository,
        rollCallRepository,
        meetingRepository,
        digitalCashRepo,
        new WitnessingRepository(
            appDatabase,
            application,
            rollCallRepository,
            electionRepository,
            meetingRepository,
            digitalCashRepo),
        new MessageRepository(appDatabase, application),
        keyManager,
        new ServerRepository());
  }

  public static DataRegistry buildRegistry(
      LAORepository laoRepo,
      SocialMediaRepository socialMediaRepo,
      ElectionRepository electionRepo,
      RollCallRepository rollCallRepo,
      MeetingRepository meetingRepo,
      DigitalCashRepository digitalCashRepo,
      WitnessingRepository witnessingRepo,
      MessageRepository msgRepo,
      KeyManager keyManager,
      ServerRepository serverRepo) {
    LaoHandler laoHandler =
        new LaoHandler(keyManager, msgRepo, laoRepo, serverRepo, witnessingRepo);
    RollCallHandler rollCallHandler =
        new RollCallHandler(laoRepo, rollCallRepo, digitalCashRepo, witnessingRepo);
    MeetingHandler meetingHandler = new MeetingHandler(laoRepo, meetingRepo, witnessingRepo);
    ElectionHandler electionHandler =
        new ElectionHandler(msgRepo, laoRepo, electionRepo, witnessingRepo);
    ConsensusHandler consensusHandler = new ConsensusHandler(laoRepo, witnessingRepo);
    ChirpHandler chirpHandler = new ChirpHandler(laoRepo, socialMediaRepo);
    ReactionHandler reactionHandler = new ReactionHandler(laoRepo, socialMediaRepo);
    TransactionCoinHandler transactionCoinHandler = new TransactionCoinHandler(digitalCashRepo);
    WitnessingHandler witnessingHandler = new WitnessingHandler(laoRepo, witnessingRepo);

    return DataRegistryModule.provideDataRegistry(
        laoHandler,
        rollCallHandler,
        meetingHandler,
        electionHandler,
        consensusHandler,
        chirpHandler,
        reactionHandler,
        transactionCoinHandler,
        witnessingHandler);
  }
}
