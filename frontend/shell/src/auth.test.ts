// @vitest-environment jsdom
import { describe, expect, it, vi } from 'vitest';
import { realmRoles } from '@operational/shared';

const keycloakMocks = vi.hoisted(() => ({
  logout: vi.fn(),
  login: vi.fn(),
  updateToken: vi.fn().mockResolvedValue(true)
}));

vi.mock('keycloak-js', () => ({
  default: class {
    authenticated = true;
    token = 'access-token';
    logout = keycloakMocks.logout;
    login = keycloakMocks.login;
    updateToken = keycloakMocks.updateToken;
  }
}));

import { authBridge } from './auth';

describe('reconstruccion de sesion', () => {
  it('recupera roles del token tras una recarga', () => {
    expect(realmRoles({ realm_access: { roles: ['SOLICITANTE'] } })).toEqual(['SOLICITANTE']);
  });

  it('cierra sesion en la raiz autorizada del shell', async () => {
    keycloakMocks.logout.mockResolvedValueOnce(undefined);
    await authBridge({ initialized: true, authenticated: true, roles: ['SOLICITANTE'] }).logout();
    expect(keycloakMocks.logout).toHaveBeenCalledWith({ redirectUri: `${window.location.origin}/` });
  });
});
