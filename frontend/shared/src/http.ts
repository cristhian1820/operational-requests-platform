import {z} from 'zod';
import type {AuthTokenProvider} from './session';
export class ApiError extends Error {constructor(public readonly status:number,public readonly code:string,detail:string,public readonly correlationId?:string){super(detail)}}
const problemSchema=z.object({status:z.number(),detail:z.string().optional(),errorCode:z.string().optional(),correlationId:z.string().optional()}).passthrough();
export function createHttpClient(provider:AuthTokenProvider){return async function request<T>(url:string,options:RequestInit={},schema?:z.ZodType<T>):Promise<T>{
 let token:string;try{if(!provider.isAuthenticated())throw new Error('SESSION_REQUIRED');token=await provider.getValidAccessToken(30)}catch{throw new ApiError(401,'AUTHENTICATION_REQUIRED','La sesión no está disponible. Inicia sesión nuevamente.')}
 const response=await fetch(url,{...options,headers:{Accept:'application/json',Authorization:`Bearer ${token}`,'X-Correlation-Id':crypto.randomUUID(),...options.headers}});
 if(!response.ok){const raw=await response.json().catch(()=>({status:response.status}));const parsedProblem=problemSchema.safeParse(raw);const problem=parsedProblem.success?parsedProblem.data:{status:response.status};throw new ApiError(response.status,problem.errorCode??`HTTP_${response.status}`,problem.detail??message(response.status),problem.correlationId)}
 if(response.status===204)return undefined as T;const data=await response.json();if(!schema)return data as T;const parsed=schema.safeParse(data);if(!parsed.success)throw new ApiError(500,'INVALID_API_RESPONSE','La respuesta del servicio no cumple el contrato');return parsed.data;
 }}
function message(status:number){return ({400:'La solicitud no es válida',401:'La sesión expiró',403:'No tiene autorización',404:'No se encontró el recurso',409:'La solicitud cambió; actualice e intente nuevamente',422:'La transición no está permitida'} as Record<number,string>)[status]??'Ocurrió un error inesperado'}
