import { Picker } from '@react-native-picker/picker';
import PropTypes from 'prop-types';
import React from 'react';
import { StyleSheet } from 'react-native';

import { RollCallToken } from '../objects/RollCallToken';

const styles = StyleSheet.create({
  pickerStyle: {
    maxHeight: 40,
    width: 'fit-content',
    maxWidth: '90%',
    fontSize: 20,
    textAlign: 'center',
    margin: 'auto',
  },
});
const RollCallTokensDropDown = (props: IPropTypes) => {
  const { onIndexChange, rollCallTokens, selectedTokenIndex } = props;

  const options = rollCallTokens.map((rc, index) => {
    const value = rc?.token.publicKey.valueOf() || '';
    return <Picker.Item value={index} label={value} />;
  });

  const onChange = (index: number) => {
    onIndexChange(index);
  };

  return (
    <Picker
      onValueChange={(value: number) => onChange(value)}
      style={styles.pickerStyle}
      selectedValue={selectedTokenIndex}>
      {options}
    </Picker>
  );
};

const propTypes = {
  onIndexChange: PropTypes.func.isRequired,
  rollCallTokens: PropTypes.arrayOf(PropTypes.instanceOf(RollCallToken)).isRequired,
  selectedTokenIndex: PropTypes.number.isRequired,
};
RollCallTokensDropDown.propTypes = propTypes;
type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default RollCallTokensDropDown;
