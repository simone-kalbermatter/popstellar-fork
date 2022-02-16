import { ExtendedMessage } from 'model/network/method/message';
import { ActionType, MessageRegistry, ObjectType } from 'model/network/method/message/data';
import { dispatch, getStore, makeCurrentLao } from 'store';
import { hasWitnessSignatureQuorum } from 'ingestion/handlers/Utils';
import { addEvent, updateEvent } from 'features/events/reducer/EventsReducer';
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
function handleMeetingCreateMessage(msg: ExtendedMessage): boolean {
  if (msg.messageData.object !== ObjectType.MEETING
    || msg.messageData.action !== ActionType.CREATE) {
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
    extra: (mtgMsg.extra) ? { ...mtgMsg.extra } : {},
  });

  dispatch(addEvent(lao.id, meeting.toState()));
  return true;
}

/**
 * Handles a MeetingState message by updating the state of the meeting.
 *
 * @param msg - The extended message for getting the meeting's state
 */
function handleMeetingStateMessage(msg: ExtendedMessage): boolean {
  if (msg.messageData.object !== ObjectType.MEETING
    || msg.messageData.action !== ActionType.STATE) {
    console.warn('handleMeetingStateMessage was called to process an unsupported message', msg);
    return false;
  }

  const makeErr = (err: string) => `meeting/state was not processed: ${err}`;

  const mtgMsg = msg.messageData as StateMeeting;
  if (!hasWitnessSignatureQuorum(mtgMsg.modification_signatures)) {
    console.warn(makeErr('witness quorum was not reached'));
    return false;
  }

  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
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

/**
 * Configures the MeetingHandler in a MessageRegistry.
 *
 * @param registry - The MessageRegistry where we want to add the mappings
 */
export function configure(registry: MessageRegistry) {
  registry.addHandler(ObjectType.MEETING, ActionType.CREATE, handleMeetingCreateMessage);
  registry.addHandler(ObjectType.MEETING, ActionType.STATE, handleMeetingStateMessage);
}
