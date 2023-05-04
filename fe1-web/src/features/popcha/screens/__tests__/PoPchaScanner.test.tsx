import { fireEvent, render, waitFor } from '@testing-library/react-native';
import React from 'react';

import { fireScan } from '__mocks__/expo-camera';
import { mockLaoId } from '__tests__/utils';
import FeatureContext from 'core/contexts/FeatureContext';

import { POPCHA_FEATURE_IDENTIFIER, PoPchaReactContext } from '../../interface';
import PoPchaScanner from '../PoPchaScanner';

const contextValue = {
  [POPCHA_FEATURE_IDENTIFIER]: {
    useCurrentLaoId: () => mockLaoId,
  } as PoPchaReactContext,
};

const mockToastShow = jest.fn();
const mockToastRet = {
  show: mockToastShow,
};
jest.mock('react-native-toast-notifications', () => ({
  useToast: () => mockToastRet,
}));

const mockClientId = 'mockClientId';
const mockRedirectUri = 'mockRedirectUri';
const mockLoginHint = mockLaoId.toString();
const mockNonce = 'mockNonce';
const mockResponseType = 'id_token token';
const mockScope = 'openid profile';

const mockUrl = new URL('https://valid2.server.example:8000');
mockUrl.searchParams.append('client_id', mockClientId);
mockUrl.searchParams.append('redirect_uri', mockRedirectUri);
mockUrl.searchParams.append('login_hint', mockLoginHint);
mockUrl.searchParams.append('nonce', mockNonce);
mockUrl.searchParams.append('response_type', mockResponseType);
mockUrl.searchParams.append('scope', mockScope);

beforeEach(() => {
  jest.clearAllMocks();
});

/**
 * Test that the scanner shows an error message when the url is invalid
 * @param url an invalid url to test
 */
const testInvalidUrl = async (url: string) => {
  const { getByTestId } = render(
    <FeatureContext.Provider value={contextValue}>
      <PoPchaScanner />
    </FeatureContext.Provider>,
  );

  const scannerButton = getByTestId('popcha_scanner_button');
  fireEvent.press(scannerButton);
  fireScan(url);
  await waitFor(() => expect(mockToastShow).toHaveBeenCalledTimes(1));
};

describe('PoPcha scanner', () => {
  describe('scanner renders correctly', () => {
    it('closed scanner renders correctly', () => {
      const component = render(
        <FeatureContext.Provider value={contextValue}>
          <PoPchaScanner />
        </FeatureContext.Provider>,
      ).toJSON();
      expect(component).toMatchSnapshot();
    });

    it('opened scanner renders correctly', () => {
      const { getByTestId, toJSON } = render(
        <FeatureContext.Provider value={contextValue}>
          <PoPchaScanner />
        </FeatureContext.Provider>,
      );
      const button = getByTestId('popcha_scanner_button');
      fireEvent.press(button);
      expect(toJSON()).toMatchSnapshot();
    });
  });
  describe('scanner verifies correctly', () => {
    it('invalid url format shows error message', async () => {
      testInvalidUrl('invalid url');
    });

    it('url without client_id shows error message', async () => {
      const url = new URL(mockUrl.toString());
      url.searchParams.delete('client_id');
      await testInvalidUrl(url.toString());
    });

    it('url without redirect_uri shows error message', async () => {
      const url = new URL(mockUrl.toString());
      url.searchParams.delete('redirect_uri');
      await testInvalidUrl(url.toString());
    });

    it('url without login_hint shows error message', async () => {
      const url = new URL(mockUrl.toString());
      url.searchParams.delete('login_hint');
      await testInvalidUrl(url.toString());
    });

    it('url without nonce shows error message', async () => {
      const url = new URL(mockUrl.toString());
      url.searchParams.delete('nonce');
      await testInvalidUrl(url.toString());
    });

    it('url without response_type shows error message', async () => {
      const url = new URL(mockUrl.toString());
      url.searchParams.delete('response_type');
      await testInvalidUrl(url.toString());
    });

    it('url without scope shows error message', async () => {
      const url = new URL(mockUrl.toString());
      url.searchParams.delete('scope');
      await testInvalidUrl(url.toString());
    });

    it('login_hint does not match current laoId shows error message', async () => {
      const url = new URL(mockUrl.toString());
      url.searchParams.set('login_hint', 'invalid login hint');
      await testInvalidUrl(url.toString());
    });
  });

  describe('valid url sends correct response', () => {
    it('valid url sends correct response', async () => {
      const { getByTestId } = render(
        <FeatureContext.Provider value={contextValue}>
          <PoPchaScanner />
        </FeatureContext.Provider>,
      );

      const scannerButton = getByTestId('popcha_scanner_button');
      fireEvent.press(scannerButton);
      fireScan(mockUrl.toString());
      await waitFor(() => expect(mockToastShow).toHaveBeenCalledTimes(0));
    });
  });
});
