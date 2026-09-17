import { describe, expect, it, vi, beforeEach } from 'vitest';
import { createHttpClient } from './http';
beforeEach(() => vi.unstubAllGlobals());
describe('http client', () => {
  it('renueva con el proveedor y envía Bearer', async () => {
    const provider = { isAuthenticated: () => true, getValidAccessToken: vi.fn().mockResolvedValue('access-token') };
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 })); vi.stubGlobal('fetch', fetchMock);
    await createHttpClient(provider)('http://api');
    expect(provider.getValidAccessToken).toHaveBeenCalledWith(30);
    expect(fetchMock.mock.calls[0]?.[1]?.headers).toMatchObject({ Authorization: 'Bearer access-token' });
  });
  for (const status of [401, 403]) it(`distingue ${status}`, async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ status, errorCode: `E${status}`, detail: 'x' }), { status, headers: { 'Content-Type': 'application/json' } })));
    const provider = { isAuthenticated: () => true, getValidAccessToken: vi.fn().mockResolvedValue('t') };
    await expect(createHttpClient(provider)('http://api')).rejects.toMatchObject({ status, code: `E${status}` });
  });
  it('trata token ausente como sesión requerida', async () => {
    const provider = { isAuthenticated: () => false, getValidAccessToken: vi.fn() };
    await expect(createHttpClient(provider)('http://api')).rejects.toMatchObject({ status: 401, code: 'AUTHENTICATION_REQUIRED' });
    expect(provider.getValidAccessToken).not.toHaveBeenCalled();
  });
  it('preserva ProblemDetail HTTP y no lo convierte en error Zod', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ title: 'Solicitud inválida', status: 400, detail: 'Revise los parámetros y campos enviados', errorCode: 'VALIDATION_ERROR' }), { status: 400, headers: { 'Content-Type': 'application/problem+json' } })));
    const provider = { isAuthenticated: () => true, getValidAccessToken: vi.fn().mockResolvedValue('t') };
    await expect(createHttpClient(provider)('http://api', {}, undefined)).rejects.toMatchObject({ status: 400, code: 'VALIDATION_ERROR', message: 'Revise los parámetros y campos enviados' });
  });
});
