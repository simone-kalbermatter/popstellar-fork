import STRINGS from 'resources/strings';

import {
  HomeCompositionConfiguration,
  HomeFeature,
  HomeInterface,
  HOME_FEATURE_IDENTIFIER,
} from './interface';
import * as navigation from './navigation';
import * as screens from './screens';

/**
 * Configures the Home feature
 */
export function compose(config: HomeCompositionConfiguration): HomeInterface {
  return {
    identifier: HOME_FEATURE_IDENTIFIER,
    navigation,
    screens,
    context: {
      createLao: config.createLao,
      connectToTestLao: config.connectToTestLao,
      useLaoList: config.useLaoList,
      LaoList: config.LaoList,
      mainNavigationScreens: [
        ...config.mainNavigationScreens,
        // add launch screen to the navigation
        {
          name: STRINGS.navigation_tab_launch,
          Component: screens.Launch,
          order: 1,
        } as HomeFeature.MainNavigationScreen,
      ],
    },
  };
}
