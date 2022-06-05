import PropTypes from 'prop-types';
import React, { useEffect, useState } from 'react';
import { View } from 'react-native';
import { ListItem } from 'react-native-elements';

import { PoPIcon } from 'core/components';
import { Hash } from 'core/objects';
import { Color, Icon, List, Typography } from 'core/styles';
import STRINGS from 'resources/strings';

import { WalletHooks } from '../hooks';
import { WalletFeature } from '../interface';
import { recoverWalletRollCallTokens } from '../objects';
import { RollCallToken } from '../objects/RollCallToken';
import RollCallWalletItem from './RollCallWalletItem';

const RollCallWalletItems = ({ laoId }: IPropTypes) => {
  const rollCallsById = WalletHooks.useRollCallsByLaoId(laoId.valueOf());

  const [tokens, setTokens] = useState<RollCallToken[]>([]);

  useEffect(() => {
    let updateWasCanceled = false;

    recoverWalletRollCallTokens(Object.values(rollCallsById), laoId).then((value) => {
      // then update the state if no new update was triggered
      if (!updateWasCanceled) {
        setTokens(value);
      }
    });

    return () => {
      // cancel update if the hook is called again
      updateWasCanceled = true;
    };
  }, [rollCallsById, laoId]);

  if (tokens.length > 0) {
    return (
      <>
        {tokens.map((token, idx) => (
          // isFirstItem and isLastItem have to be refactored in the future
          // since it is not known what other items other features add
          <RollCallWalletItem
            key={token.rollCallId.valueOf()}
            rollCallToken={token}
            isFirstItem={false}
            isLastItem={idx === tokens.length - 1}
          />
        ))}
      </>
    );
  }

  const listStyles = List.getListItemStyles(false, true);

  return (
    <ListItem containerStyle={listStyles} style={listStyles}>
      <View style={List.icon}>
        <PoPIcon name="qrCode" color={Color.primary} size={Icon.size} />
      </View>
      <ListItem.Content>
        <ListItem.Title style={Typography.base}>
          {STRINGS.wallet_home_rollcall_no_pop_tokens}
        </ListItem.Title>
        <ListItem.Subtitle style={Typography.small}>
          {STRINGS.wallet_home_rollcall_no_pop_tokens_description}
        </ListItem.Subtitle>
      </ListItem.Content>
    </ListItem>
  );
};

const propTypes = {
  laoId: PropTypes.instanceOf(Hash).isRequired,
};

RollCallWalletItems.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default RollCallWalletItems;

export const rollCallWalletItemGenerator: WalletFeature.WalletItemGenerator = {
  ListItems: RollCallWalletItems as React.ComponentType<{ laoId: Hash }>,
  order: 1000,
};
