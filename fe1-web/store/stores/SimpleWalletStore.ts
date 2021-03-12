import { Hash } from 'model/objects/Hash';
import { KeyPair } from 'model/objects/KeyPair';

/**
 * Each wallet should have a personal WalletStore object which job is to keep
 * all the tokens of the attended roll calls of the wallet's owner using a
 * SECURE storage.
 *
 * note - at the moment the storage is not secure at all - it is a simple private
 * map locally stored, this will change in the next implementation once an adeguate
 * and SECURE storage library is found for this task.
 *
 * note - all the add/get/remove methods for the database throw exceptions if
 * the operation is not possible, should they instead return true/false?
 */
export class SimpleWalletStore {
  private database: Map<string, KeyPair>;

  private walletId: Hash;

  /**
   * @param walletId the id of the wallet object associated with this wallet storage
   */
  constructor(walletId: Hash) {
    if (walletId === null) {
      throw new Error('Error encountered while creating wallet storage : undefined/null walletId');
    }
    this.database = new Map<string, KeyPair>();
    this.walletId = walletId;
  }

  /**
   * adds a key/value pair to the wallet
   * @param key the key to the storage: it is the concatenation of the LAOid and RollCallId
   * @param keyPair the KeyPair generated by the wallet object
   */
  public addKeyPairToWallet(key: string, keyPair: KeyPair) {
    if (key === null || keyPair === null) {
      throw new Error('Error encountered while adding roll call token to Wallet : undefined/null parameters');
    }
    if (this.database.has(key)) {
      throw new Error('Error encountered while adding roll call token to Wallet : a token for this roll call already exists');
    }
    this.database.set(key, keyPair);
  }

  /**
   * returns the given value for the given key from the wallet
   * @param key the key to the storage: it is the concatenation of the LAOid and RollCallId
   */
  public getKeyPairFromWallet(key: string) {
    if (key === null) {
      throw new Error('Error encountered while retreving roll call token from Wallet : undefined/null parameters');
    }
    this.checkPresenceInWallet(key);
    return this.database.get(key);
  }

  /**
   * removes the given key/value pair from the wallet
   * @param key the key to the storage: it is the concatenation of the LAOid and RollCallId
   */
  public removeKeyFromWallet(key: string) {
    if (key === null) {
      throw new Error('Error encountered while removing roll call token from Wallet : undefined/null parameters');
    }
    this.checkPresenceInWallet(key);
    this.database.delete(key);
  }

  public getWalletId() {
    return this.walletId;
  }

  /**
   * returns true if the key/value pair is present in the wallet, otherwise throws an exception
   * @param key the key to the storage: it is the concatenation of the LAOid and RollCallId
   */
  private checkPresenceInWallet(key: string): boolean {
    if (!this.database.has(key)) {
      throw new Error('Error encountered while removing roll call token from Wallet : the token is not in wallet');
    }
    return true;
  }
}
