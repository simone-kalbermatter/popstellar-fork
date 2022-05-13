import React from 'react';
import { AnyAction, Reducer } from 'redux';

import { MessageRegistry } from 'core/network/jsonrpc/messages';
import { Hash } from 'core/objects';
import FeatureInterface from 'core/objects/FeatureInterface';

import { ElectionReducerState, ELECTION_REDUCER_PATH } from '../reducer/ElectionReducer';
import { EvotingFeature } from './Feature';

export const EVOTING_FEATURE_IDENTIFIER = 'evoting';

export interface EvotingConfiguration {
  // objects
  messageRegistry: MessageRegistry;

  /* LAO related functions */

  /**
   * Returns the currently active lao. Should be used outside react components
   * @returns The current lao
   */
  getCurrentLao: () => EvotingFeature.Lao;

  /**
   * Returns the currently active lao. Should be used inside react components
   * @returns The current lao
   */
  useCurrentLao: () => EvotingFeature.Lao;

  /**
   * Returns the currently active lao id. Should be used inside react components
   * @returns The current lao id
   */
  useCurrentLaoId: () => Hash | undefined;

  /* Event related functions */

  /**
   * Creates a redux action for adding an event to the event store
   * @param laoId - The lao id where to add the event
   * @param eventType - The type of the event
   * @param id - The id of the event
   * @param idAlias - An optional alias id of the event
   * @returns A redux action causing the state change
   */
  addEvent: (
    laoId: Hash | string,
    eventType: string,
    id: Hash | string,
    idAlias?: Hash | string | undefined,
  ) => AnyAction;

  /**
   * Creates a redux action for update the stored event state
   * @param laoId - The lao id where to add the event
   * @param eventType - The type of the event
   * @param id - The id of the event
   * @param idAlias - An optional alias id of the event
   * @returns A redux action causing the state change
   */
  updateEvent: (
    laoId: Hash | string,
    eventType: string,
    id: Hash | string,
    idAlias?: Hash | string | undefined,
  ) => AnyAction;

  /**
   * Given the redux state and an event id, this function looks in the active
   * lao for an event with a matching id, creates an instance of the corresponding type
   * and returns it
   * @param id - The id of the event
   * @returns The event or undefined if none was found
   */
  getEventById: (id: Hash) => EvotingFeature.EventState | undefined;
}

/**
 * The type of the context that is provided to react evoting components
 */
export type EvotingReactContext = Pick<
  EvotingConfiguration,
  /* lao */
  | 'useCurrentLao'
  | 'useCurrentLaoId'
  /* events */
  | 'getEventById'
  | 'addEvent'
  | 'updateEvent'
>;

/**
 * The interface the evoting feature exposes
 */
export interface EvotingInterface extends FeatureInterface {
  screens: {
    CreateElection: React.ComponentType<any>;
  };

  eventTypeComponents: {
    isOfType: (event: unknown) => boolean;
    Component: React.ComponentType<{ event: unknown; isOrganizer: boolean | null | undefined }>;
  }[];

  context: EvotingReactContext;

  reducers: {
    [ELECTION_REDUCER_PATH]: Reducer<ElectionReducerState>;
  };
}
