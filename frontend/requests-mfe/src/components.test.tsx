// @vitest-environment jsdom
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { EmptyState, RequestTimeline, StatusChip } from './components';
describe('componentes', () => {
  it('muestra estado y vacío', () => { render(<><StatusChip status="REGISTRADA" /><EmptyState /></>); expect(screen.getByText('REGISTRADA')).toBeInTheDocument(); expect(screen.getByText('No hay resultados')).toBeInTheDocument(); });
  it('ordena cronológicamente la línea de tiempo', () => { const common = { actorId: crypto.randomUUID(), rolActor: 'ANALISTA' as const, motivo: null }; render(<RequestTimeline items={[{ ...common, id: crypto.randomUUID(), estadoAnterior: 'EN_ATENCION', estadoNuevo: 'RESUELTA', ocurridoEn: '2026-02-02T00:00:00Z' }, { ...common, id: crypto.randomUUID(), estadoAnterior: null, estadoNuevo: 'REGISTRADA', ocurridoEn: '2026-01-01T00:00:00Z' }]} />); expect(screen.getAllByRole('listitem')[0]).toHaveTextContent('REGISTRADA'); });
});
