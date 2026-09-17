import Keycloak from 'keycloak-js';
import { createKeycloakTokenProvider, runtimeConfig, realmRoles, sessionReady, sessionCleared, type AuthBridge, type AuthTokenProvider } from '@operational/shared';
import { store } from './store';

export const keycloak = new Keycloak({ url: runtimeConfig.keycloakUrl, realm: runtimeConfig.keycloakRealm, clientId: runtimeConfig.keycloakClientId });

export function shellTokenProvider(): AuthTokenProvider {
  return createKeycloakTokenProvider(keycloak, () => store.dispatch(sessionCleared()));
}

export async function initializeAuth() {
  try {
    const authenticated = await keycloak.init({ onLoad: 'check-sso', pkceMethod: 'S256', checkLoginIframe: true, silentCheckSsoRedirectUri: `${location.origin}/silent-check-sso.html` });
    const parsed = keycloak.tokenParsed;
    store.dispatch(sessionReady({ authenticated, subject: parsed?.sub, username: typeof parsed?.preferred_username === 'string' ? parsed.preferred_username : undefined, roles: realmRoles(parsed) }));
    keycloak.onTokenExpired = () => { void shellTokenProvider().getValidAccessToken(30).catch(() => store.dispatch(sessionCleared())); };
  } catch {
    store.dispatch(sessionReady({ authenticated: false, roles: [] }));
  }
}

export function authBridge(session: AuthBridge['session']): AuthBridge {
  return { session, authTokenProvider: shellTokenProvider(), login: () => keycloak.login({ redirectUri: location.href }), logout: () => keycloak.logout({ redirectUri: location.origin }) };
}
