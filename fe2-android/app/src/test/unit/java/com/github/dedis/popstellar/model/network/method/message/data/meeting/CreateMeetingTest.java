package com.github.dedis.popstellar.model.network.method.message.data.meeting;

import com.github.dedis.popstellar.model.network.method.message.data.Action;
import com.github.dedis.popstellar.model.objects.Lao;
import com.github.dedis.popstellar.model.objects.event.EventType;
import com.github.dedis.popstellar.utility.security.Hash;

import org.junit.Test;

import java.time.Instant;
import java.util.Objects;

import static com.github.dedis.popstellar.testutils.Base64DataUtils.generatePublicKey;
import static org.junit.Assert.*;

public class CreateMeetingTest {
  private static final String LAO_ID =
      Lao.generateLaoId(generatePublicKey(), Instant.now().getEpochSecond(), "Lao name");
  private static final String NAME = "name";
  private static final String LOCATION = "location";
  private static final long CREATION = Instant.now().getEpochSecond();
  private static final long START = CREATION + 1;
  private static final long END = START + 5;
  private static final String ID =
      Hash.hash(EventType.MEETING.getSuffix(), LAO_ID, Long.toString(CREATION), NAME);

  private static final CreateMeeting CREATE_MEETING =
      new CreateMeeting(LAO_ID, ID, NAME, CREATION, LOCATION, START, END);

  @Test
  public void getId() {
    assertEquals(ID, CREATE_MEETING.getId());
  }

  @Test
  public void getName() {
    assertEquals(NAME, CREATE_MEETING.getName());
  }

  @Test
  public void getCreation() {
    assertEquals(CREATION, CREATE_MEETING.getCreation());
  }

  @Test
  public void getLocation() {
    assertEquals(LOCATION, CREATE_MEETING.getLocation().orElse(""));
  }

  @Test
  public void getStart() {
    assertEquals(START, CREATE_MEETING.getStart());
  }

  @Test
  public void getEnd() {
    assertEquals(END, CREATE_MEETING.getEnd());
  }

  @Test
  public void getObject() {
    assertEquals("meeting", CREATE_MEETING.getObject());
  }

  @Test
  public void getAction() {
    assertEquals(Action.CREATE.getAction(), CREATE_MEETING.getAction());
  }

  @Test
  public void testEquals() {
    assertEquals(CREATE_MEETING, CREATE_MEETING);
    assertNotEquals(null, CREATE_MEETING);

    CreateMeeting createMeeting1 = new CreateMeeting(LAO_ID, NAME, CREATION, LOCATION, START, END);
    assertEquals(CREATE_MEETING, createMeeting1);
  }

  @Test
  public void testHashCode() {
    assertEquals(Objects.hash(ID, NAME, CREATION, LOCATION, START, END), CREATE_MEETING.hashCode());
  }

  @Test
  public void testToString() {
    String expected =
        String.format(
            "CreateMeeting{id='%s', name='%s', creation=%d, location='%s', start=%d, end=%d}",
            ID, NAME, CREATION, LOCATION, START, END);
    assertEquals(expected, CREATE_MEETING.toString());
  }

  @Test
  public void nonCoherentIdInConstructorThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new CreateMeeting(LAO_ID, "random id", NAME, CREATION, LOCATION, START, END));
  }
}
