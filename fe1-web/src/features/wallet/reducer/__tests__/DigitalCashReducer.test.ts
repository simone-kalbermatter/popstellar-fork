import 'jest-extended';

import { AnyAction } from 'redux';

import { mockCBHash, mockLaoId, mockValidCoinbaseJSON } from '__tests__/utils';
import { Hash } from 'core/objects';
import { Transaction } from 'features/wallet/objects/transaction';

import {
  digitalCashReduce,
  addTransaction,
  makeBalanceSelector,
  DIGITAL_CASH_REDUCER_PATH,
} from '../DigitalCashReducer';

const mockTransaction = Transaction.fromJSON(mockValidCoinbaseJSON, mockCBHash).toState();

const emptyState = {
  byLaoId: {},
};

const filledState = {
  byLaoId: {
    [mockLaoId.valueOf()]: {
      balances: {
        [mockValidCoinbaseJSON.outputs[0].script.pubkey_hash]:
          mockValidCoinbaseJSON.outputs[0].value,
      },
      transactions: [mockTransaction],
      transactionsByHash: {
        [mockTransaction.transactionId!]: mockTransaction,
      },
      transactionsByPubHash: {
        [mockValidCoinbaseJSON.outputs[0].script.pubkey_hash]: [mockTransaction],
      },
    },
  },
};

describe('digital cash reducer', () => {
  it('should return the initial state', () => {
    expect(digitalCashReduce(undefined, {} as AnyAction)).toEqual(emptyState);
  });

  it('should handle a transaction being added from empty state', () => {
    expect(
      digitalCashReduce(
        emptyState,
        addTransaction({ laoId: mockLaoId, transactionMessage: mockTransaction }),
      ),
    ).toEqual(filledState);
  });
});
describe('make balance selector', () => {
  it('should be able to recover a balance', () => {
    expect(
      makeBalanceSelector(
        new Hash(mockLaoId),
        mockValidCoinbaseJSON.inputs[0].script.pubkey,
      )({ [DIGITAL_CASH_REDUCER_PATH]: filledState }),
    ).toEqual(100);
  });
  it('should return 0 when public key is not found', () => {
    expect(
      makeBalanceSelector(
        new Hash(mockLaoId),
        'pubkey',
      )({ [DIGITAL_CASH_REDUCER_PATH]: filledState }),
    ).toEqual(0);
  });
});
