import {z} from 'zod';
/** SQL Server UNIQUEIDENTIFIER/OpenAPI uuid lexical form. SQL seed data may use non-RFC variant bits. */
export const Uuid=z.string().regex(/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/,'UUID inválido');
export const Status=z.enum(['REGISTRADA','EN_ATENCION','RESUELTA','CERRADA']); export const Priority=z.enum(['BAJA','MEDIA','ALTA']); export const RoleSchema=z.enum(['SOLICITANTE','ANALISTA','SUPERVISOR']);
export const Category=z.object({id:Uuid,codigo:z.string().min(1),nombre:z.string().min(1)});
export const Observation=z.object({id:Uuid,texto:z.string(),actorId:Uuid,rolActor:RoleSchema,creadaEn:z.iso.datetime()});
export const History=z.object({id:Uuid,estadoAnterior:Status.nullable(),estadoNuevo:Status,actorId:Uuid,rolActor:RoleSchema,motivo:z.string().nullable(),ocurridoEn:z.iso.datetime()});
export const RequestSummary=z.object({id:Uuid,numero:z.string(),asunto:z.string(),categoriaId:Uuid,prioridad:Priority,estado:Status,solicitanteId:Uuid,analistaAsignadoId:Uuid.nullable(),creadaEn:z.iso.datetime(),actualizadaEn:z.iso.datetime()});
export const RequestDetail=RequestSummary.extend({descripcion:z.string(),categoriaNombre:z.string().nullable(),observaciones:z.array(Observation),historial:z.array(History)});
export const RequestPage=z.object({content:z.array(RequestSummary),page:z.number().int().min(0),size:z.number().int().min(1),totalElements:z.number().int().min(0),totalPages:z.number().int().min(0)});
export const CreateRequest=z.object({asunto:z.string().trim().min(1).max(200),descripcion:z.string().trim().min(1).max(2000),categoriaId:Uuid,prioridad:Priority});
export const ObservationInput=z.object({texto:z.string().trim().min(1).max(2000)}); export const TransitionInput=z.object({estadoDestino:Status,motivo:z.string().trim().max(500).optional(),observacion:z.string().trim().max(2000).optional()});
export const Summary=z.object({porEstado:z.array(z.object({estado:Status,cantidad:z.number()})),porCategoria:z.array(z.object({categoriaId:Uuid,categoria:z.string(),cantidad:z.number()})),total:z.number(),actualizadoEn:z.iso.datetime()});
export const Trend=z.object({puntos:z.array(z.object({fecha:z.iso.date(),cantidadRegistradas:z.number()}))});
export type RequestDetailType=z.infer<typeof RequestDetail>; export type RequestSummaryType=z.infer<typeof RequestSummary>;
