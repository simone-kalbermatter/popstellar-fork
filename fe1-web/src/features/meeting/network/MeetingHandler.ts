import { ProcessableMessage } from 'core/network/jsonrpc/messages';
import { dispatch, getStore } from 'core/redux';
import { ActionType, ObjectType } from 'core/network/jsonrpc/messages/MessageData';
import { makeCurrentLao } from 'features/lao/reducer';
import { hasWitnessSignatureQuorum } from 'core/network/validation/Checker';
import { addEvent, updateEvent } from 'features/events/reducer';
import { getEventFromId } from 'features/events/network/EventHandlerUtils';

import { CreateMeeting, StateMeeting } from './messages';
import { Meeting } from '../objects';

/**
 * Handles all meeting related messages that are received.
 */

const getCurrentLao = makeCurrentLao();

/**
 * Handles a MeetingCreate message by creating a meeting in the current Lao.
 *
 * @param msg - The extended message for creating a meeting
 */
export function handleMeetingCreateMessage(msg: ProcessableMessage): boolean {
  if (
    msg.messageData.object !== ObjectType.MEETING ||
    msg.messageData.action !== ActionType.CREATE
  ) {
    console.warn('handleMeetingCreateMessage was called to process an unsupported message', msg);
    return false;
  }

  const makeErr = (err: string) => `meeting/create was not processed: ${err}`;

  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }

  const mtgMsg = msg.messageData as CreateMeeting;

  const meeting = new Meeting({
    id: mtgMsg.id,
    name: mtgMsg.name,
    location: mtgMsg.location,
    creation: mtgMsg.creation,
    start: mtgMsg.start,
    end: mtgMsg.end ? mtgMsg.end : undefined,
    extra: mtgMsg.extra ? { ...mtgMsg.extra } : {},
  });

  dispatch(addEvent(lao.id, meeting.toState()));
  return true;
}

/**
 * Handles a MeetingState message by updating the state of the meeting.
 *
 * @param msg - The extended message for getting the meeting's state
 */
export function handleMeetingStateMessage(msg: ProcessableMessage): boolean {
  if (
    msg.messageData.object !== ObjectType.MEETING ||
    msg.messageData.action !== ActionType.STATE
  ) {
    console.warn('handleMeetingStateMessage was called to process an unsupported message', msg);
    return false;
  }

  const makeErr = (err: string) => `meeting/state was not processed: ${err}`;

  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }

  const mtgMsg = msg.messageData as StateMeeting;
  if (!hasWitnessSignatureQuorum(mtgMsg.modification_signatures, lao)) {
    console.warn(makeErr('witness quorum was not reached'));
    return false;
  }

  const oldMeeting = getEventFromId(storeState, mtgMsg.id) as Meeting;
  if (!oldMeeting) {
    console.warn(makeErr("no known meeting matching the 'id' field"));
    return false;
  }

  const meeting = new Meeting({
    ...oldMeeting,
    lastModified: mtgMsg.last_modified,
    location: mtgMsg.location,
    start: mtgMsg.start,
    end: mtgMsg.end,
    extra: {
      ...oldMeeting.extra,
      ...mtgMsg.extra,
    },
  });

  dispatch(updateEvent(lao.id, meeting.toState()));
  return true;
}
