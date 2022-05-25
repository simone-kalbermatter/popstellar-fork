import { CompositeScreenProps, useNavigation } from '@react-navigation/core';
import { StackScreenProps } from '@react-navigation/stack';
import PropTypes from 'prop-types';
import React, { useMemo } from 'react';
import { StyleSheet, ViewStyle } from 'react-native';
import { ListItem } from 'react-native-elements';

import { AppParamList } from 'core/navigation/typing/AppParamList';
import { LaoEventsParamList } from 'core/navigation/typing/LaoEventsParamList';
import { LaoParamList } from 'core/navigation/typing/LaoParamList';
import { Spacing } from 'core/styles';
import STRINGS from 'resources/strings';

import { EventHooks } from '../hooks';

const styles = StyleSheet.create({
  listItem: {
    paddingHorizontal: Spacing.horizontalContentSpacing,
  } as ViewStyle,
});

type NavigationProps = CompositeScreenProps<
  StackScreenProps<LaoEventsParamList, typeof STRINGS.navigation_lao_events_home>,
  CompositeScreenProps<
    StackScreenProps<LaoParamList, typeof STRINGS.navigation_lao_events>,
    StackScreenProps<AppParamList, typeof STRINGS.navigation_app_lao>
  >
>;

const EventListItem = (props: IPropTypes) => {
  const { eventId, eventType } = props;

  const navigation = useNavigation<NavigationProps['navigation']>();

  const isOrganizer = EventHooks.useIsLaoOrganizer();
  const eventTypes = EventHooks.useEventTypes();

  const EventType = useMemo(() => {
    return eventTypes.find((c) => c.eventType === eventType);
  }, [eventType, eventTypes]);

  return EventType ? (
    <ListItem
      containerStyle={styles.listItem}
      bottomDivider
      onPress={() =>
        navigation.push(STRINGS.navigation_app_lao, {
          screen: STRINGS.navigation_lao_events,
          params: {
            screen: EventType.navigationNames.screenSingle,
            params: {
              eventId: eventId,
              isOrganizer,
            },
          },
        })
      }>
      <EventType.ListItemComponent eventId={eventId} isOrganizer={isOrganizer} />
    </ListItem>
  ) : (
    <ListItem containerStyle={styles.listItem} bottomDivider>
      <ListItem.Content>
        <ListItem.Title>{`Event type '${eventType}' was not registered!`}</ListItem.Title>
      </ListItem.Content>
    </ListItem>
  );
};

const propTypes = {
  eventId: PropTypes.string.isRequired,
  eventType: PropTypes.string.isRequired,
};
EventListItem.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default EventListItem;
