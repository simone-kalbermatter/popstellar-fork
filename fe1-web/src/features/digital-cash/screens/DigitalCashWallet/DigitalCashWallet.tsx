import { CompositeScreenProps, useNavigation, useRoute } from '@react-navigation/core';
import { StackScreenProps } from '@react-navigation/stack';
import React, { useEffect, useMemo, useReducer } from 'react';
import { Modal, Text, View } from 'react-native';
import { ListItem } from 'react-native-elements';
import { ScrollView, TouchableWithoutFeedback } from 'react-native-gesture-handler';

import { CollapsibleContainer, Input, PoPTextButton, QRCode } from 'core/components';
import ModalHeader from 'core/components/ModalHeader';
import ScannerInput from 'core/components/ScannerInput';
import ScreenWrapper from 'core/components/ScreenWrapper';
import { AppParamList } from 'core/navigation/typing/AppParamList';
import { WalletParamList } from 'core/navigation/typing/WalletParamList';
import { List, ModalStyles, Typography } from 'core/styles';
import STRINGS from 'resources/strings';

import TransactionHistory from '../../components/TransactionHistory';
import { DigitalCashHooks } from '../../hooks';
import { DigitalCashFeature } from '../../interface';
import { RollCallAccount } from '../../objects/Account';
import {
  DigitalCashWalletActionType,
  digitalCashWalletStateReducer,
} from './DigitalCashWalletState';

type NavigationProps = CompositeScreenProps<
  StackScreenProps<WalletParamList, typeof STRINGS.navigation_wallet_digital_cash_wallet>,
  StackScreenProps<AppParamList, typeof STRINGS.navigation_app_lao>
>;

const rollCallAccounts: RollCallAccount[] = [
  {
    rollCallId: 'l1d1c5VwRmz2oiRRjEJh78eEhOnEf8QJ4W5PrmZfxcE=',
    rollCallName: 'a roll call',
    popToken: '-uac6_xEos4Dz8ESBpoAnqLD4vsd3viScjIEcPEQilo=',
    balance: 21.3,
  },
  {
    rollCallId: 'THFll04mCvZxOhCL9DYygnbTBSR2fjQAYGkfTzPf-zc=',
    rollCallName: 'another roll call',
    popToken: '-uac6_xEos4Dz8ESBpoAnqLD4vsd3viScjIEcPEQilo=',
    balance: 20.9,
  },
];

const DigitalCashWallet = () => {
  const navigation = useNavigation<NavigationProps['navigation']>();
  const route = useRoute<NavigationProps['route']>();

  const { laoId, scannedPoPTokenRollCallId, scannedPoPToken, scannedPoPTokenBeneficiaryIndex } =
    route.params;

  const isOrganizer = DigitalCashHooks.useIsLaoOrganizer(laoId);

  /**
   * The component state. This is a react reducer, similar to redux reducers and allows
   * us to hide the complex state update logic from the UI code
   */
  const [{ showModal, selectedAccount, beneficiaries, error }, dispatch] = useReducer(
    digitalCashWalletStateReducer,
    {
      showModal: false,
      selectedAccount: null,
      beneficiaries: [{ amount: '', popToken: '' }],
      error: null,
    },
  );

  const balance = rollCallAccounts.reduce((sum, account) => sum + (account.balance || 0), 0);

  /**
   * Add coin issuance account for organizers
   */
  const accounts: RollCallAccount[] = useMemo(() => {
    if (isOrganizer) {
      return [
        {
          rollCallName: STRINGS.digital_cash_coin_issuance,
          rollCallId: STRINGS.digital_cash_coin_issuance_description,
          balance: null,
        } as RollCallAccount,
        ...rollCallAccounts,
      ];
    }
    return rollCallAccounts;
  }, [isOrganizer]);

  const totalAmount = beneficiaries.reduce((sum, target) => sum + parseFloat(target.amount), 0);

  const onClose = () => dispatch({ type: DigitalCashWalletActionType.CLOSE_MODAL });

  const onSendTransaction = () => {
    if (!selectedAccount) {
      throw new Error('It should not be possible to send money without selecting an account first');
    }

    if (Number.isNaN(totalAmount)) {
      dispatch({
        type: DigitalCashWalletActionType.SET_ERROR,
        error: STRINGS.digital_cash_wallet_amount_must_be_number,
      });
      return;
    }

    if (selectedAccount.balance && totalAmount > selectedAccount.balance) {
      dispatch({
        type: DigitalCashWalletActionType.SET_ERROR,
        error: STRINGS.digital_cash_wallet_amount_too_high,
      });
      return;
    }

    dispatch({
      type: DigitalCashWalletActionType.CLEAR_ERROR,
    });

    if (selectedAccount.balance) {
      // TODO: transaction
    } else {
      // TODO: coin issuance
    }
  };

  const cannotSendTransaction =
    Number.isNaN(totalAmount) ||
    (!!selectedAccount?.balance && selectedAccount && totalAmount > selectedAccount?.balance);

  useEffect(() => {
    if (
      scannedPoPTokenRollCallId &&
      scannedPoPTokenBeneficiaryIndex !== undefined &&
      scannedPoPTokenBeneficiaryIndex < beneficiaries.length
    ) {
      const account = accounts.find((acc) => acc.rollCallId === scannedPoPTokenRollCallId);

      if (account && scannedPoPToken) {
        dispatch({
          type: DigitalCashWalletActionType.INSERT_SCANNED_POP_TOKEN,
          account,
          beneficiaryIndex: scannedPoPTokenBeneficiaryIndex,
          beneficiaryPopToken: scannedPoPToken,
        });
      }
    }
    // should only be re-executed of the navigation parameters change
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scannedPoPTokenRollCallId, scannedPoPToken, scannedPoPTokenBeneficiaryIndex]);

  return (
    <ScreenWrapper>
      <Text style={[Typography.paragraph, Typography.important]}>
        {STRINGS.digital_cash_wallet_balance}: ${balance}
      </Text>
      <Text style={Typography.paragraph}>{STRINGS.digital_cash_wallet_description}</Text>

      <View style={List.container}>
        {accounts.map((account, idx) => {
          const listStyle = List.getListItemStyles(idx === 0, idx === accounts.length - 1);

          return (
            <ListItem
              key={account.rollCallId}
              containerStyle={listStyle}
              style={listStyle}
              bottomDivider
              onPress={() => {
                dispatch({ type: DigitalCashWalletActionType.OPEN_MODAL, account });
              }}>
              <ListItem.Content>
                <ListItem.Title style={Typography.base}>{account.rollCallName}</ListItem.Title>
                <ListItem.Subtitle
                  style={Typography.small}
                  numberOfLines={account.balance ? 1 : undefined}>
                  {account.rollCallId}
                </ListItem.Subtitle>
              </ListItem.Content>
              <ListItem.Title style={Typography.base}>${account.balance || '∞'}</ListItem.Title>
              <ListItem.Chevron />
            </ListItem>
          );
        })}
      </View>

      <Modal transparent visible={showModal} onRequestClose={onClose}>
        <TouchableWithoutFeedback containerStyle={ModalStyles.modalBackground} onPress={onClose} />
        <ScrollView style={ModalStyles.modalContainer}>
          <ModalHeader onClose={onClose}>{STRINGS.digital_cash_wallet_your_account}</ModalHeader>

          <Text style={[Typography.paragraph, Typography.important]}>
            {STRINGS.digital_cash_wallet_balance}: ${selectedAccount?.balance || '∞'}
          </Text>

          {selectedAccount?.balance && (
            <CollapsibleContainer title={STRINGS.digital_cash_wallet_your_account_receive}>
              <Text style={Typography.paragraph}>
                {STRINGS.digital_cash_wallet_your_account_receive_description}
              </Text>

              <QRCode visibility value={selectedAccount.popToken} />
            </CollapsibleContainer>
          )}

          <CollapsibleContainer
            title={STRINGS.digital_cash_wallet_your_account_send}
            isInitiallyOpen={!selectedAccount?.balance || !!scannedPoPToken}>
            <Text style={Typography.paragraph}>
              {STRINGS.digital_cash_wallet_transaction_description}
            </Text>

            {beneficiaries.map(({ amount, popToken }, index) => (
              <View key={index.toString()}>
                <Text style={[Typography.paragraph, Typography.important]}>
                  {STRINGS.digital_cash_wallet_beneficiary}
                </Text>
                <ScannerInput
                  value={popToken}
                  onChange={(newPopToken) =>
                    dispatch({
                      type: DigitalCashWalletActionType.UPDATE_BENEFICIARY,
                      beneficiaryIndex: index,
                      popToken: newPopToken,
                    })
                  }
                  onPress={() => {
                    if (!selectedAccount) {
                      throw new Error(
                        'It should not be possible to get here without selecting an account first',
                      );
                    }

                    // we hide the modal here and automatically open it when navigating back
                    dispatch({ type: DigitalCashWalletActionType.HIDE_MODAL });

                    navigation.navigate(STRINGS.navigation_wallet_digital_cash_wallet_scanner, {
                      laoId: laoId.valueOf(),
                      rollCallId: selectedAccount.rollCallId,
                      beneficiaryIndex: index,
                    });
                  }}
                  placeholder={STRINGS.digital_cash_wallet_beneficiary_placeholder}
                />

                <Text style={[Typography.paragraph, Typography.important]}>
                  {STRINGS.digital_cash_wallet_amount}
                </Text>
                <Input
                  value={amount}
                  onChange={(newAmount) =>
                    dispatch({
                      type: DigitalCashWalletActionType.UPDATE_BENEFICIARY,
                      beneficiaryIndex: index,
                      amount: newAmount,
                    })
                  }
                  placeholder={STRINGS.digital_cash_wallet_amount_placeholder}
                />
              </View>
            ))}

            {error && <Text style={[Typography.paragraph, Typography.error]}>{error}</Text>}
            <PoPTextButton
              onPress={() => dispatch({ type: DigitalCashWalletActionType.ADD_BENEFICIARY })}>
              {STRINGS.digital_cash_wallet_add_beneficiary}
            </PoPTextButton>

            <PoPTextButton disabled={cannotSendTransaction} onPress={onSendTransaction}>
              {STRINGS.digital_cash_wallet_send_transaction}
            </PoPTextButton>
          </CollapsibleContainer>
        </ScrollView>
      </Modal>

      <TransactionHistory />
    </ScreenWrapper>
  );
};

export default DigitalCashWallet;

export const DigitalCashWalletScreen: DigitalCashFeature.WalletScreen = {
  id: STRINGS.navigation_wallet_digital_cash_wallet,
  title: STRINGS.digital_cash_wallet_screen_title,
  Component: DigitalCashWallet,
};
