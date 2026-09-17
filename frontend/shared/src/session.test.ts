import { describe, expect, it, vi } from 'vitest';
import { createKeycloakTokenProvider } from './session';

describe('AuthTokenProvider', () => {
  it('renueva sobre la instancia recibida y no sobre un singleton', async () => {
    const source = { authenticated: true, token: 'shell-token', updateToken: vi.fn().mockResolvedValue(true) };
    const provider = createKeycloakTokenProvider(source);
    await expect(provider.getValidAccessToken(30)).resolves.toBe('shell-token');
    expect(source.updateToken).toHaveBeenCalledWith(30);
  });
  it('trata una sesión sin token de renovación como sesión no disponible', async () => {
    const source = { authenticated: true, token: undefined, updateToken: vi.fn().mockRejectedValue(new Error('refresh unavailable')) };
    const onInvalid = vi.fn();
    await expect(createKeycloakTokenProvider(source, onInvalid).getValidAccessToken()).rejects.toThrow('SESSION_REFRESH_FAILED');
    expect(onInvalid).toHaveBeenCalledOnce();
  });
});
