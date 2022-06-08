import { Hash } from 'core/objects';
import { getStore } from 'core/redux';

import { getDigitalCashState } from '../../wallet/reducer';
import { TransactionState } from '../objects/transaction';

export namespace DigitalCashStore {
  export function getTransactionsById(laoId: string): Record<string, TransactionState> {
    return getDigitalCashState(getStore().getState()).byLaoId[laoId]?.transactionsByHash || {};
  }
  export function getTransactionsByPublicKey(laoId: string, pk: string): TransactionState[] {
    const laoState = getDigitalCashState(getStore().getState()).byLaoId[laoId];
    if (!laoState) {
      return [];
    }
    const pkHash = Hash.fromPublicKey(pk);
    const transactionsById = getTransactionsById(laoId);
    return laoState.transactionsByPubHash[pkHash.valueOf()]?.map((hash) => transactionsById[hash]);
  }
  export function getBalance(laoId: string, pk: string): number {
    const laoState = getDigitalCashState(getStore().getState()).byLaoId[laoId];
    if (!laoState) {
      return 0;
    }
    const hash = Hash.fromPublicKey(pk);
    const balance = laoState.balances[hash.valueOf()];
    return balance || 0;
  }
}
