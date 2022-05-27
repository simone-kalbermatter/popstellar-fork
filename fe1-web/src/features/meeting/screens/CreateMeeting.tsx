import { CompositeScreenProps } from '@react-navigation/core';
import { useNavigation } from '@react-navigation/native';
import { StackScreenProps } from '@react-navigation/stack';
import React, { useState } from 'react';
import { Platform, StyleSheet, Text, View } from 'react-native';
import { useToast } from 'react-native-toast-notifications';

import { ConfirmModal, DatePicker, DismissModal, Button, Input } from 'core/components';
import { onChangeEndTime, onChangeStartTime } from 'core/components/DatePicker';
import ScreenWrapper from 'core/components/ScreenWrapper';
import { onConfirmEventCreation } from 'core/functions/UI';
import { AppParamList } from 'core/navigation/typing/AppParamList';
import { LaoEventsParamList } from 'core/navigation/typing/LaoEventsParamList';
import { LaoParamList } from 'core/navigation/typing/LaoParamList';
import { Timestamp } from 'core/objects';
import { Spacing, Typography } from 'core/styles';
import { FOUR_SECONDS } from 'resources/const';
import STRINGS from 'resources/strings';

import { MeetingHooks } from '../hooks';
import { MeetingFeature } from '../interface';
import { requestCreateMeeting } from '../network/MeetingMessageApi';

const DEFAULT_MEETING_DURATION = 3600;

const styles = StyleSheet.create({
  buttons: {
    marginTop: Spacing.x1,
    zIndex: 0,
  },
});

type NavigationProps = CompositeScreenProps<
  StackScreenProps<LaoEventsParamList, typeof STRINGS.navigation_lao_events_create_meeting>,
  CompositeScreenProps<
    StackScreenProps<LaoParamList, typeof STRINGS.navigation_lao_events>,
    StackScreenProps<AppParamList, typeof STRINGS.navigation_app_lao>
  >
>;

/**
 * Screen to create a meeting event: a name text input, a start time text and its buttons,
 * a finish time text and its buttons, a location text input, a confirm button and a cancel button
 */
const CreateMeeting = () => {
  const navigation = useNavigation<NavigationProps['navigation']>();
  const toast = useToast();
  const laoId = MeetingHooks.useCurrentLaoId();

  const [meetingName, setMeetingName] = useState('');
  const [startTime, setStartTime] = useState(Timestamp.EpochNow());
  const [endTime, setEndTime] = useState(Timestamp.EpochNow().addSeconds(DEFAULT_MEETING_DURATION));
  const [modalEndIsVisible, setModalEndIsVisible] = useState(false);
  const [modalStartIsVisible, setModalStartIsVisible] = useState(false);

  const [location, setLocation] = useState('');

  const confirmButtonVisibility: boolean = meetingName !== '';

  const createMeeting = () => {
    requestCreateMeeting(laoId, meetingName, startTime, location, endTime)
      .then(() => {
        navigation.navigate(STRINGS.navigation_lao_events_home);
      })
      .catch((err) => {
        console.error('Could not create meeting, error:', err);
        toast.show(`Could not create meeting, error: ${err}`, {
          type: 'danger',
          placement: 'top',
          duration: FOUR_SECONDS,
        });
      });
  };

  const buildDatePickerWeb = () => {
    const startDate = startTime.toDate();
    const endDate = endTime.toDate();

    return (
      <>
        <Text style={[Typography.paragraph, Typography.important]}>
          {STRINGS.meeting_create_start_time}
        </Text>

        <DatePicker
          selected={startDate}
          onChange={(date: Date) =>
            onChangeStartTime(date, setStartTime, setEndTime, DEFAULT_MEETING_DURATION)
          }
        />
        <Text style={[Typography.paragraph, Typography.important]}>
          {STRINGS.meeting_create_finish_time}
        </Text>

        <DatePicker
          selected={endDate}
          onChange={(date: Date) => onChangeEndTime(date, startTime, setEndTime)}
        />
      </>
    );
  };

  return (
    <ScreenWrapper>
      <Text style={[Typography.paragraph, Typography.important]}>
        {STRINGS.meeting_create_name}
      </Text>
      <Input
        value={meetingName}
        onChange={setMeetingName}
        placeholder={STRINGS.meeting_create_name_placeholder}
      />

      {/* see archive branches for date picker used for native apps */}
      {Platform.OS === 'web' && buildDatePickerWeb()}

      <Text style={[Typography.paragraph, Typography.important]}>
        {STRINGS.meeting_create_location}
      </Text>
      <Input
        value={location}
        onChange={setLocation}
        placeholder={STRINGS.meeting_create_location_placeholder}
      />

      <View style={styles.buttons}>
        <Button
          onPress={() =>
            onConfirmEventCreation(
              startTime,
              endTime,
              createMeeting,
              setModalStartIsVisible,
              setModalEndIsVisible,
            )
          }
          disabled={!confirmButtonVisibility}>
          <Text style={[Typography.base, Typography.centered, Typography.negative]}>
            {STRINGS.meeting_create_meeting}
          </Text>
        </Button>
      </View>

      <DismissModal
        visibility={modalEndIsVisible}
        setVisibility={setModalEndIsVisible}
        title={STRINGS.modal_event_creation_failed}
        description={STRINGS.modal_event_ends_in_past}
      />
      <ConfirmModal
        visibility={modalStartIsVisible}
        setVisibility={setModalStartIsVisible}
        title={STRINGS.modal_event_creation_failed}
        description={STRINGS.modal_event_starts_in_past}
        onConfirmPress={() => createMeeting()}
        buttonConfirmText={STRINGS.modal_button_start_now}
      />
    </ScreenWrapper>
  );
};

export default CreateMeeting;

export const CreateMeetingScreen: MeetingFeature.LaoEventScreen = {
  id: STRINGS.navigation_lao_events_create_meeting,
  Component: CreateMeeting,
};
