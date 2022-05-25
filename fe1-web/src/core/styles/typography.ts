import { TextStyle } from 'react-native';

import {
  contrast,
  accent as accentColor,
  secondaryAccent as secondaryColor,
  primary as primaryColor,
} from './color';
import { x1 } from './spacing';

export const base: TextStyle = {
  textAlign: 'left',
  color: primaryColor,
  fontSize: 20,
  lineHeight: 26,
};

export const accent: TextStyle = {
  color: accentColor,
};

export const negative: TextStyle = {
  color: contrast,
};

export const secondary: TextStyle = {
  color: secondaryColor,
};

export const paragraph: TextStyle = {
  ...base,
  marginBottom: x1,
};

export const heading: TextStyle = {
  ...base,
  fontSize: 32,
  lineHeight: 41,
  fontWeight: 'bold',
  marginBottom: x1,
};

export const topNavigationHeading: TextStyle = {
  ...base,
  fontWeight: '500',
};

export const pressable: TextStyle = {
  ...base,
  color: accentColor,
  fontWeight: '500',
};

export const centered: TextStyle = {
  textAlign: 'center',
};

export const important: TextStyle = {
  fontWeight: 'bold',
};

export const baseCentered: TextStyle = {
  ...base,
  textAlign: 'center',
  marginHorizontal: 10,
};

export const importantCentered: TextStyle = {
  ...baseCentered,
  fontWeight: 'bold',
};
