import React from 'react';
import { AnyAction, Reducer } from 'redux';

import { MessageRegistry } from 'core/network/jsonrpc/messages';
import { Channel, Hash, PublicKey } from 'core/objects';
import FeatureInterface from 'core/objects/FeatureInterface';

import { Lao } from '../objects';
import { LaoReducerState, LAO_REDUCER_PATH } from '../reducer';
import { LaoFeature } from './Feature';

export const LAO_FEATURE_IDENTIFIER = 'lao';

export interface LaoConfiguration {
  /* other */
  registry: MessageRegistry;
}

export interface LaoCompositionConfiguration {
  /* events */

  EventList: React.ComponentType<any>;

  /* connect */

  /**
   * Given the lao server address and the lao id, this computes the data
   * that is encoded in a QR code that can be used to connect to a LAO
   */
  encodeLaoConnectionForQRCode: (server: string, laoId: string) => string;

  /* other */

  /**
   * The screens that should additionally be included in the lao navigation
   */
  laoNavigationScreens: LaoFeature.Screen[];

  /**
   * The screens that should additionally be included in the lao navigation
   */
  organizerNavigationScreens: LaoFeature.Screen[];
}

/**
 * The interface the lao feature exposes
 */
export interface LaoConfigurationInterface extends FeatureInterface {
  /* components */
  components: {
    LaoList: React.ComponentType<unknown>;
  };

  /* action creators */
  actionCreators: {
    addLaoServerAddress: (laoId: Hash | string, serverAddress: string) => AnyAction;
  };

  /* hooks */
  hooks: {
    /**
     * Gets the list of LAOs
     * @returns The list of LAOs
     */
    useLaoList: () => Lao[];

    /**
     * Checks whether the current user is the organizer of the current lao
     * @returns Whether the current user is the organizer of the current lao
     */
    useIsLaoOrganizer: () => boolean;

    /**
     * Gets the map of lao ids to the respective laos
     * @returns The map of ids to laos
     */
    useLaoMap: () => Record<string, Lao>;

    /**
     * Gets the current lao
     * @returns The current lao
     */
    useCurrentLao: () => Lao;

    /**
     * Gets the current lao id
     * @returns The current lao id or undefined if there is none
     */
    useCurrentLaoId: () => Hash | undefined;
  };

  /* functions */
  functions: {
    /**
     * Gets the current lao
     * @returns The current lao
     */
    getCurrentLao: () => Lao;

    /**
     * Gets the current lao id
     * @returns The current lao id or undefined if there is none
     */
    getCurrentLaoId: () => Hash | undefined;

    /**
     * Gets the organizer backend's public key for a given lao
     * @param laoId The lao id
     * @returns The organizer's backend public key for the given lao or undefined if it is not known
     */
    getLaoOrganizerBackendPublicKey: (laoId: string) => PublicKey | undefined;

    /**
     * Sends a network request to create a new lao and returns
     * the corresponding channel
     */
    requestCreateLao: (laoName: string) => Promise<Channel>;

    /**
     * Opens a lao test connection
     */
    openLaoTestConnection: () => void;

    /**
     * Get a LAOs channel by its id
     * @param laoId The id of the lao whose channel should be returned
     * @returns The channel related to the passed lao id
     */
    getLaoChannel: (laoId: string) => Channel | undefined;
  };

  /* reducers */
  reducers: {
    [LAO_REDUCER_PATH]: Reducer<LaoReducerState>;
  };
}

/**
 * The type of the context that is provided to react lao components
 */
export type LaoReactContext = Pick<
  LaoCompositionConfiguration,
  /* events */
  | 'EventList'
  /* connect */
  | 'encodeLaoConnectionForQRCode'
  /* navigation screens */
  | 'laoNavigationScreens'
  | 'organizerNavigationScreens'
>;

export interface LaoCompositionInterface extends FeatureInterface {
  /* navigation */
  navigation: {
    LaoNavigation: React.ComponentType<unknown>;
  };
  /* react context */
  context: LaoReactContext;
}
