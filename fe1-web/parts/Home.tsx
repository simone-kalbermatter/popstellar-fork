import React from 'react';
import { StyleSheet, View, FlatList } from 'react-native';
import { useSelector } from 'react-redux';

import { makeLaosList } from 'store';
import { Lao } from 'model/objects';

import { Spacing } from 'styles';
import styleContainer from 'styles/stylesheets/container';
import STRINGS from 'res/strings';

import LAOItem from 'components/LAOItem';
import TextBlock from 'components/TextBlock';
import WideButtonView from '../components/WideButtonView';
import { WalletCryptographyHandler } from '../model/objects/WalletCryptographyHandler';

/**
 * Manage the Home screen component: if the user is not connected to any LAO, a welcome message
 * is displayed, otherwise a list available previously connected LAOs is displayed instead
 *
 * TODO use the list that the user have already connect to, and ask data to
 *  some organizer server if needed
 */
const styles = StyleSheet.create({
  flatList: {
    marginTop: Spacing.s,
  },
});

let cypher: string = ' ';

const onWalletCryptoHandlerButtonPressed = async () => {
  console.log('Creation of wallet object');
  await WalletCryptographyHandler.initWalletStorage();
  console.log('Wallet created');
};

const onEncryptRandomToken = async () => {
  const key: string = 'MHICAQEwBQYDK2VwBCIEINTuctv5E1hK1bbY8fdp+K06/nwoy/HU++CXqI9EdVhCoB8wHQYKKoZIhvcNAQkJFDEPDA1DdXJkbGUgQ2hhaXJzgSEAGb9ECWmEzf6FQbrBZ9w7lshQhqowtrbLDFw4rXAxZuE=';
  cypher = await WalletCryptographyHandler.encrypt(key);
  console.log(cypher.toString());
};

const onDecryptRandomToken = async () => {
  const plain = await WalletCryptographyHandler.decrypt(cypher);
  console.log(plain.toString());
};

// FIXME: define interface + types, requires availableLaosReducer to be migrated first
function getConnectedLaosDisplay(laos: Lao[]) {
  return (
    <View style={styleContainer.centered}>
      <FlatList
        data={laos}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => <LAOItem LAO={item} />}
        style={styles.flatList}
      />
    </View>
  );
}

function getWelcomeMessageDisplay() {
  return (
    <View style={styleContainer.centered}>
      <TextBlock bold text={STRINGS.home_welcome} />
      <TextBlock bold text={STRINGS.home_connect_lao} />
      <TextBlock bold text={STRINGS.home_launch_lao} />
      <TextBlock text={' '} />
      <TextBlock text={' '} />
      <TextBlock text={' '} />
      <WideButtonView
        title={STRINGS.wallet}
        onPress={() => onWalletCryptoHandlerButtonPressed()}
      />
      <WideButtonView
        title={STRINGS.walletEncryptRandomToken}
        onPress={() => onEncryptRandomToken()}
      />
      <WideButtonView
        title={STRINGS.walletDecryptRandomToken}
        onPress={() => onDecryptRandomToken()}
      />
    </View>
  );
}

const Home = () => {
  const laosList = makeLaosList();
  const laos: Lao[] = useSelector(laosList);

  return (laos && !laos.length)
    ? getConnectedLaosDisplay(laos)
    : getWelcomeMessageDisplay();
};

export default Home;
