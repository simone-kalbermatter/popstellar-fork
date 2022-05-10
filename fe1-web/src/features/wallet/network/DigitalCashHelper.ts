import { Base64UrlData, Hash } from 'core/objects';

import { DigitalCashMessage, DigitalCashTransaction, TxIn, TxOut } from './DigitalCashTransaction';

/**
 * Hash a transaction to get its id
 * @param transaction to hash
 */
export const hashTransaction = (transaction: DigitalCashTransaction): Hash => {
  // Recursively concatenating fields by lexicographic order of their names
  const dataTxIns = transaction.txsIn.flatMap((txIn) => {
    if (txIn.script.publicKey) {
      return [
        txIn.script.publicKey.valueOf(),
        txIn.script.signature.valueOf(),
        txIn.script.type,
        txIn.txOutHash.valueOf(),
        txIn.txOutIndex.toString(),
      ];
    }
    return [txIn.txOutHash.valueOf(), txIn.txOutIndex.toString()];
  });
  const dataTxOuts = transaction.txsOut.flatMap((txOut) => {
    return [txOut.script.publicKeyHash.valueOf(), txOut.script.type, txOut.value.toString()];
  });
  const data = [transaction.lockTime.toString()]
    .concat(dataTxIns)
    .concat(dataTxOuts)
    .concat([transaction.version.toString()]);

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
    tr.transaction.txsOut.filter(
      (txOut) => txOut.script.publicKeyHash.valueOf() === pkHash.valueOf(),
    ),
  );
  return txOuts.reduce((total, current) => total + current.value, 0);
};

export const getTxsInToSign = (
  pk: string,
  transactionMessages: DigitalCashMessage[],
): Omit<TxIn, 'script'>[] => {
  return transactionMessages.flatMap((tr) =>
    tr.transaction.txsOut
      .filter((txOut) => txOut.script.publicKeyHash.valueOf() === Hash.fromString(pk).valueOf())
      .map((txOut, index) => {
        return {
          txOutHash: tr.transactionId,
          txOutIndex: index,
        };
      }),
  );
};

export const concatenateTxData = (txsInt: Omit<TxIn, 'script'>[], txOuts: TxOut[]) => {
  const txsInDataString = txsInt.reduce(
    (dataString, txIn) => dataString + txIn.txOutHash.valueOf() + txIn.txOutIndex.toString(),
    '',
  );
  return txOuts.reduce(
    (dataString, txOut) =>
      dataString +
      txOut.value.toString() +
      txOut.script.type +
      txOut.script.publicKeyHash.valueOf(),
    txsInDataString,
  );
};
