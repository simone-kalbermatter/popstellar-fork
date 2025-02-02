package com.github.dedis.popstellar.repository.database;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.dedis.popstellar.di.AppDatabaseModuleHelper;
import com.github.dedis.popstellar.model.objects.Lao;
import com.github.dedis.popstellar.model.objects.Meeting;
import com.github.dedis.popstellar.repository.database.event.meeting.MeetingDao;
import com.github.dedis.popstellar.repository.database.event.meeting.MeetingEntity;

import org.junit.*;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.TestObserver;

import static com.github.dedis.popstellar.testutils.Base64DataUtils.generatePublicKey;

@RunWith(AndroidJUnit4.class)
public class MeetingDatabaseTest {
  private static AppDatabase appDatabase;
  private static MeetingDao meetingDao;

  private static final long CREATION = Instant.now().getEpochSecond();

  private static final String LAO_ID = Lao.generateLaoId(generatePublicKey(), CREATION, "Lao");
  private static final String MEETING_ID =
      Meeting.generateCreateMeetingId(LAO_ID, CREATION, "name");
  private static final Meeting MEETING =
      new Meeting(
          MEETING_ID,
          "name",
          CREATION,
          CREATION + 10,
          CREATION + 20,
          "loc",
          CREATION,
          "modId",
          new ArrayList<>());

  private static final MeetingEntity MEETING_ENTITY = new MeetingEntity(LAO_ID, MEETING);

  @Before
  public void before() {
    appDatabase =
        AppDatabaseModuleHelper.getAppDatabase(ApplicationProvider.getApplicationContext());
    meetingDao = appDatabase.meetingDao();
  }

  @After
  public void close() {
    appDatabase.close();
  }

  @Test
  public void insertTest() {
    TestObserver<Void> testObserver = meetingDao.insert(MEETING_ENTITY).test();

    testObserver.awaitTerminalEvent();
    testObserver.assertComplete();
  }

  @Test
  public void insertAndGetTest() {
    TestObserver<Void> testObserver = meetingDao.insert(MEETING_ENTITY).test();

    testObserver.awaitTerminalEvent();
    testObserver.assertComplete();

    TestObserver<List<Meeting>> testObserver2 =
        meetingDao
            .getMeetingsByLaoId(LAO_ID)
            .test()
            .assertValue(meetings -> meetings.size() == 1 && meetings.get(0).equals(MEETING));

    testObserver2.awaitTerminalEvent();
    testObserver2.assertComplete();
  }
}
