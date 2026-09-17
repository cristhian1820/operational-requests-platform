// @vitest-environment jsdom
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AuthBridge } from '@operational/shared';
import RemoteRoutes from './RequestRoutes';
import { requestsApi } from './api';

vi.mock('./api', () => ({ requestsApi: vi.fn() }));

const uuid = '33333333-3333-3333-3333-333333333333';
const categoryId = '11111111-1111-1111-1111-111111111111';
const auth: AuthBridge = { session: { initialized: true, authenticated: true, subject: '22222222-2222-2222-2222-222222222222', username: 'solicitante', roles: ['SOLICITANTE'] }, authTokenProvider: { isAuthenticated: () => true, getValidAccessToken: vi.fn().mockResolvedValue('token') }, login: vi.fn(), logout: vi.fn() };
const detail = (categoriaNombre: string | null) => ({ id: uuid, numero: 'SOL-2026-000001', asunto: 'Acceso', descripcion: 'Necesito acceso', categoriaId: categoryId, categoriaNombre, prioridad: 'MEDIA' as const, estado: 'REGISTRADA' as const, solicitanteId: auth.session.subject, analistaAsignadoId: null, creadaEn: '2026-09-16T12:00:00Z', actualizadaEn: '2026-09-16T12:00:00Z', observaciones: [], historial: [] });

describe('nombre de categoria en detalle', () => {
  beforeEach(() => vi.clearAllMocks());
  it('muestra el nombre y nunca el UUID', async () => {
    vi.mocked(requestsApi).mockReturnValue({ detail: vi.fn().mockResolvedValue(detail('General')) } as never);
    render(<MemoryRouter initialEntries={[`/solicitudes/${uuid}`]}><Routes><Route path="/solicitudes/*" element={<RemoteRoutes auth={auth} />} /></Routes></MemoryRouter>);
    expect(await screen.findByText('Categoría: General')).toBeInTheDocument();
    expect(screen.queryByText(new RegExp(categoryId))).not.toBeInTheDocument();
  });
  it('muestra un fallback controlado si el nombre no está disponible', async () => {
    vi.mocked(requestsApi).mockReturnValue({ detail: vi.fn().mockResolvedValue(detail(null)) } as never);
    render(<MemoryRouter initialEntries={[`/solicitudes/${uuid}`]}><Routes><Route path="/solicitudes/*" element={<RemoteRoutes auth={auth} />} /></Routes></MemoryRouter>);
    await screen.findByText('SOL-2026-000001');
    expect(document.body.textContent).toContain('Categoría: Categoría no disponible');
    expect(screen.queryByText(new RegExp(categoryId))).not.toBeInTheDocument();
  });
});
