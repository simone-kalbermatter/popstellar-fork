import { Hash } from 'core/objects';

import { DigitalCashMessage, DigitalCashTransaction, TxIn, TxOut } from './DigitalCashTransaction';

/**
 * Hash a transaction to get its id
 * @param transaction to hash
 */
export const hashTransaction = (transaction: DigitalCashTransaction): Hash => {
  // Recursively concatenating fields by lexicographic order of their names
  const dataTxIns = transaction.TxIn.flatMap((txIn) => {
    if (txIn.Script) {
      return [
        txIn.Script.Pubkey.valueOf(),
        txIn.Script.Sig.valueOf(),
        txIn.Script.Type,
        txIn.TxOutHash.valueOf(),
        txIn.TxOutHash.toString(),
      ];
    }
    return [txIn.TxOutHash.valueOf(), txIn.TxOutIndex.toString()];
  });
  const dataTxOuts = transaction.TxOut.flatMap((txOut) => {
    return [txOut.Script.PubkeyHash.valueOf(), txOut.Script.Type, txOut.Value.toString()];
  });
  const data = [transaction.LockTime.toString()]
    .concat(dataTxIns)
    .concat(dataTxOuts)
    .concat([transaction.Version.toString()]);

  // Hash will take care of concatenating each fields length
  return Hash.fromStringArray(...data);
};
/**
 * Get the total value out that corresponds to this public key hash from an array of transactions
 * @param pkHash the public key hash
 * @param transactionMessages the transaction messages from which the amount out
 * @return the total value out
 */
export const getTotalValue = (
  pkHash: string | Hash,
  transactionMessages: DigitalCashMessage[],
): number => {
  const txOuts = transactionMessages.flatMap((tr) =>
    tr.transaction.TxOut.filter((txOut) => txOut.Script.PubkeyHash.valueOf() === pkHash.valueOf()),
  );
  return txOuts.reduce((total, current) => total + current.Value, 0);
};

export const getTxsInToSign = (
  pk: string,
  transactionMessages: DigitalCashMessage[],
): Omit<TxIn, 'Script'>[] => {
  return transactionMessages.flatMap((tr) =>
    tr.transaction.TxOut.filter(
      (txOut) => txOut.Script.PubkeyHash.valueOf() === Hash.fromString(pk).valueOf(),
    ).map((txOut, index) => {
      return {
        TxOutHash: tr.transactionId,
        TxOutIndex: index,
      };
    }),
  );
};

export const concatenateTxData = (txsInt: Omit<TxIn, 'Script'>[], txOuts: TxOut[]) => {
  const txsInDataString = txsInt.reduce(
    (dataString, txIn) => dataString + txIn.TxOutHash.valueOf() + txIn.TxOutHash.toString(),
    '',
  );
  return txOuts.reduce(
    (dataString, txOut) =>
      dataString + txOut.Value.toString() + txOut.Script.Type + txOut.Script.PubkeyHash.valueOf(),
    txsInDataString,
  );
};
