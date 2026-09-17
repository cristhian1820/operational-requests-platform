import { describe, expect, it, vi, beforeEach } from 'vitest';
import { requestsApi } from './api';

beforeEach(() => vi.unstubAllGlobals());
describe('requests API authentication boundary', () => {
  it('uses the injected provider for categories and sends Bearer', async () => {
    const provider = { isAuthenticated: () => true, getValidAccessToken: vi.fn().mockResolvedValue('federated-token') };
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([{ id: '11111111-1111-4111-8111-111111111111', codigo: 'SOP', nombre: 'Soporte técnico' }]), { status: 200, headers: { 'Content-Type': 'application/json' } }));
    vi.stubGlobal('fetch', fetchMock);
    await requestsApi(provider).categories();
    expect(provider.getValidAccessToken).toHaveBeenCalledWith(30);
    expect(fetchMock.mock.calls[0]?.[1]?.headers).toMatchObject({ Authorization: 'Bearer federated-token' });
  });
});
