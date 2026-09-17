import { describe, expect, it } from 'vitest';
import { Category, History, Observation, RequestDetail, RequestPage, Summary, Trend } from '@operational/shared';

const id = '11111111-1111-4111-8111-111111111111';
const actor = '22222222-2222-4222-8222-222222222222';
const timestamps = { creadaEn: '2026-09-16T12:00:00Z', actualizadaEn: '2026-09-16T12:00:00Z' };

describe('contratos HTTP reales', () => {
  it('acepta la respuesta real de categorías', () => expect(Category.parse({ id, codigo: 'SOP', nombre: 'Soporte técnico' })).toEqual({ id, codigo: 'SOP', nombre: 'Soporte técnico' }));
  it('acepta los identificadores sembrados por SQL Server', () => expect(Category.parse({ id: '11111111-1111-1111-1111-111111111111', codigo: 'ACCESO', nombre: 'Accesos' }).nombre).toBe('Accesos'));
  it('rechaza una categoría incompleta', () => expect(Category.safeParse({ id, nombre: 'Sin código' }).success).toBe(false));
  it('acepta la respuesta paginada real de solicitudes', () => expect(RequestPage.parse({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }).content).toEqual([]));
  it('rechaza paginación con valores fuera de contrato', () => expect(RequestPage.safeParse({ content: [], page: -1, size: 0, totalElements: -1, totalPages: -1 }).success).toBe(false));
  it('acepta detalle real, observación e historial del backend', () => {
    const detail = { id, numero: 'SOL-2026-000001', asunto: 'Asunto', descripcion: 'Descripción', categoriaId: id, categoriaNombre: 'General', prioridad: 'MEDIA', estado: 'REGISTRADA', solicitanteId: actor, analistaAsignadoId: null, ...timestamps, observaciones: [{ id: actor, actorId: actor, rolActor: 'SOLICITANTE', texto: 'Texto', creadaEn: timestamps.creadaEn }], historial: [{ id: actor, estadoAnterior: null, estadoNuevo: 'REGISTRADA', actorId: actor, rolActor: 'SOLICITANTE', motivo: null, ocurridoEn: timestamps.creadaEn }] };
    expect(Observation.parse(detail.observaciones[0]).texto).toBe('Texto');
    expect(History.parse(detail.historial[0]).ocurridoEn).toBe(timestamps.creadaEn);
    expect(RequestDetail.parse(detail).categoriaNombre).toBe('General');
  });
  it('acepta resumen y tendencia reales de indicadores', () => {
    expect(Summary.parse({ porEstado: [{ estado: 'REGISTRADA', cantidad: 1 }], porCategoria: [{ categoriaId: id, categoria: 'Soporte técnico', cantidad: 1 }], total: 1, actualizadoEn: timestamps.actualizadaEn }).total).toBe(1);
    expect(Trend.parse({ puntos: [{ fecha: '2026-09-16', cantidadRegistradas: 1 }] }).puntos[0]?.cantidadRegistradas).toBe(1);
  });
  it('rechaza respuestas incompatibles', () => expect(RequestPage.safeParse({ content: 'incorrecto' }).success).toBe(false));
});
