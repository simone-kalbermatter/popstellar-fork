import { useNavigation } from '@react-navigation/native';
import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, ViewStyle, Text, TextStyle } from 'react-native';
import { useToast } from 'react-native-toast-notifications';
import { useSelector } from 'react-redux';

import { LogoutRoundButton, QRCode, SendRoundButton } from 'core/components';
import { KeyPairStore } from 'core/keypair';
import { PublicKey } from 'core/objects';
import { Typography } from 'core/styles';
import containerStyles from 'core/styles/stylesheets/containerStyles';
import STRINGS from 'resources/strings';

import { RollCallTokensDropDown, SendModal } from '../components';
import { WalletHooks } from '../hooks';
import { requestCoinbaseTransaction, requestSendTransaction } from '../network';
import * as Wallet from '../objects';
import { RollCallToken } from '../objects/RollCallToken';
import { makeBalanceSelector } from '../reducer';

const styles = StyleSheet.create({
  homeContainer: {
    ...containerStyles.centeredXY,
    padding: 30,
  } as ViewStyle,
  smallPadding: {
    padding: '1rem',
  } as ViewStyle,
  tokenSelectContainer: {
    flexDirection: 'row',
    marginTop: 30,
    marginBottom: 20,
  } as ViewStyle,
  mainButtonsContainer: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-around',
    minWidth: '50%',
    margin: 'auto',
    marginTop: 20,
  } as ViewStyle,
  textBase: Typography.baseCentered as TextStyle,
  textImportant: Typography.important as TextStyle,
  topBar: {
    display: 'flex',
    flexDirection: 'row',
    width: '100%',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingLeft: 20,
    paddingRight: 20,
  } as ViewStyle,
});

/**
 * Wallet UI once the wallet is synced
 */
const WalletHome = () => {
  const [sendModalVisible, setSendModalVisible] = useState(false);
  const [tokens, setTokens] = useState<RollCallToken[]>();
  const [selectedTokenIndex, setSelectedTokenIndex] = useState(-1);

  const toast = useToast();

  const rollCalls = WalletHooks.useRollCallsByLaoId();

  const laoId = WalletHooks.useCurrentLaoId();

  const balanceSelector = useMemo(() => {
    if (!laoId || selectedTokenIndex === -1 || !tokens) {
      return () => 0;
    }
    return makeBalanceSelector(laoId, tokens[selectedTokenIndex].token.publicKey.valueOf());
  }, [tokens, laoId, selectedTokenIndex]);
  const balance = useSelector(balanceSelector);

  // FIXME: Navigation should use a defined type here (instead of any)
  const navigation = useNavigation<any>();

  useEffect(() => {
    if (!laoId || !rollCalls || !rollCalls[laoId.valueOf()]) {
      // Clear tokens screen state
      setSelectedTokenIndex(-1);
      setTokens(undefined);
      return;
    }

    Wallet.recoverWalletRollCallTokens(rollCalls, laoId)
      .then((rct) => {
        if (rct.length > 0) {
          setTokens(rct);
          setSelectedTokenIndex(0);
        }
      })
      .catch((e) => {
        console.debug(e);
      });
  }, [rollCalls, laoId]);

  const tokenInfos = () => {
    if (selectedTokenIndex !== -1 && tokens) {
      const selectedToken = tokens[selectedTokenIndex];
      const rollCallName = `Roll Call name: ${selectedToken.rollCallName.valueOf()}`;
      return (
        <View style={containerStyles.centeredXY}>
          <Text style={styles.textBase}>{rollCallName}</Text>
          <Text style={styles.textBase}>{STRINGS.wallet_balance + balance.valueOf()}</Text>
          <QRCode value={tokens[selectedTokenIndex].token.publicKey.valueOf()} visibility />
        </View>
      );
    }
    return <Text style={styles.textBase}>{STRINGS.no_tokens_in_wallet}</Text>;
  };

  return (
    <View style={styles.homeContainer}>
      <View style={styles.topBar}>
        <Text style={styles.textImportant}>{STRINGS.wallet_welcome}</Text>
        <LogoutRoundButton
          onClick={() => {
            Wallet.forget();
            navigation.reset({
              index: 0,
              routes: [{ name: STRINGS.navigation_wallet_setup_tab }],
            });
          }}
        />
      </View>
      <View style={styles.tokenSelectContainer}>
        {tokens && (
          <RollCallTokensDropDown
            rollCallTokens={tokens}
            onIndexChange={setSelectedTokenIndex}
            selectedTokenIndex={selectedTokenIndex}
          />
        )}
      </View>
      {tokenInfos()}
      {tokens && (
        <View style={styles.mainButtonsContainer}>
          <SendRoundButton
            onClick={() => {
              setSendModalVisible(true);
            }}
          />
        </View>
      )}
      <SendModal
        modalVisible={sendModalVisible}
        setModalVisible={setSendModalVisible}
        send={(receiver: string, amount: number, isCoinbase: boolean) => {
          if (isCoinbase) {
            requestCoinbaseTransaction(KeyPairStore.get(), new PublicKey(receiver), amount, laoId!)
              .then(() => toast.show('Sent coinbase transaction'))
              .catch((err) => {
                console.error('Failed sending the transaction : ', err);
              });
          } else {
            requestSendTransaction(
              tokens![selectedTokenIndex].token,
              new PublicKey(receiver),
              amount,
              laoId!,
            )
              .then(() => toast.show('Sent transaction'))
              .catch((err) => {
                console.error('Failed sending the transaction : ', err);
              });
          }
          setSendModalVisible(false);
        }}
      />
      <View style={styles.smallPadding} />
    </View>
  );
};

export default WalletHome;
