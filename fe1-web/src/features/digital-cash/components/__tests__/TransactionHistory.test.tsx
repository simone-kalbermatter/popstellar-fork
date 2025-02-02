import { render } from '@testing-library/react-native';
import React from 'react';

import MockNavigator from '__tests__/components/MockNavigator';
import { mockLaoId } from '__tests__/utils';
import FeatureContext from 'core/contexts/FeatureContext';
import { DigitalCashHooks } from 'features/digital-cash/hooks';
import { Transaction } from 'features/digital-cash/objects/transaction';

import {
  mockCBHash,
  mockCoinbaseTransactionJSON,
  mockTransactionState,
  mockRollCallToken,
  mockDigitalCashContextValue,
} from '../../__tests__/utils';
import TransactionHistory from '../TransactionHistory';

jest.mock('features/digital-cash/hooks');

const mockCoinbase = Transaction.fromJSON(mockCoinbaseTransactionJSON, mockCBHash);

(DigitalCashHooks.useTransactionsByRollCallTokens as jest.Mock).mockReturnValue([
  mockCoinbase,
  Transaction.fromState(mockTransactionState),
]);

describe('TransactionHistory', () => {
  it('renders correctly', () => {
    const Screen = () => (
      <TransactionHistory laoId={mockLaoId} rollCallTokens={[mockRollCallToken]} />
    );

    const { toJSON } = render(
      <FeatureContext.Provider value={mockDigitalCashContextValue(true)}>
        <MockNavigator component={Screen} />
      </FeatureContext.Provider>,
    );
    expect(toJSON()).toMatchSnapshot();
  });
});
