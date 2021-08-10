// fake LAOs to show how the app works
import {
  Hash, Lao, PublicKey, Timestamp
} from 'model/objects';

const data: Lao[] = [
  new Lao({
    id: Hash.fromString('31'),
    name: 'LAO 1',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('32'),
    name: 'LAO 2',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('i7NMjdW2tkBj4uMCM15rlckmQCdR0anffOTD+MQg3XQ='),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('33'),
    name: 'LAO 3',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('i7NMjdW2tkBj4uMCM15rlckmQCdR0anffOTD+MQg3XQ='),
      new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('34'),
    name: 'LAO 4',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('35'),
    name: 'LAO 5',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('36'),
    name: 'LAO 6',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('37'),
    name: 'LAO 7',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('38'),
    name: 'LAO 8',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('39'),
    name: 'LAO 9',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('310'),
    name: 'LAO 10',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('311'),
    name: 'LAO 11',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('312'),
    name: 'LAO 12',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('313'),
    name: 'LAO 13',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('314'),
    name: 'LAO 14',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('315'),
    name: 'LAO 15',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('316'),
    name: 'LAO 16',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('317'),
    name: 'LAO 17',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('318'),
    name: 'LAO 18',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('319'),
    name: 'LAO 19',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
  new Lao({
    id: Hash.fromString('320'),
    name: 'LAO 20',
    creation: new Timestamp(1607616483),
    last_modified: new Timestamp(1607616483),
    organizer: new PublicKey('DEADBEEF'),
    witnesses: [new PublicKey('DEADC0DE'), new PublicKey('DEADBEA7')],
    /* modification_id: Hash.fromString('a modification id'),
    modification_signatures: [
      {
        witness: new PublicKey('DEADC0DE'),
        signature: 'witness signature 1',
      }, {
        witness: new PublicKey('DEADBEA7'),
        signature: 'witness signature 2',
      }], */
  }),
];

export default data;
