import {createSlice,type PayloadAction} from '@reduxjs/toolkit';
export type Role='SOLICITANTE'|'ANALISTA'|'SUPERVISOR';
export interface SessionDescription {initialized:boolean;authenticated:boolean;subject?:string|undefined;username?:string|undefined;roles:Role[]}
const initialState:SessionDescription={initialized:false,authenticated:false,roles:[]};
export const sessionSlice=createSlice({name:'session',initialState,reducers:{sessionReady:(_s,a:PayloadAction<Omit<SessionDescription,'initialized'>>)=>({initialized:true,...a.payload}),sessionCleared:()=>({initialized:true,authenticated:false,roles:[]})}});
export const {sessionReady,sessionCleared}=sessionSlice.actions;
export function realmRoles(tokenParsed:unknown):Role[]{const value=(tokenParsed as {realm_access?:{roles?:unknown}}|undefined)?.realm_access?.roles;if(!Array.isArray(value))return [];return value.filter((r):r is Role=>r==='SOLICITANTE'||r==='ANALISTA'||r==='SUPERVISOR');}
export interface AuthTokenProvider {isAuthenticated():boolean;getValidAccessToken(minValidity?:number):Promise<string>}
export interface AuthBridge {session:SessionDescription;authTokenProvider:AuthTokenProvider;login():Promise<void>;logout():Promise<void>}

export interface KeycloakTokenSource { authenticated?: boolean | undefined; token?: string | undefined; updateToken(minValidity: number): Promise<boolean> }
export function createKeycloakTokenProvider(source: KeycloakTokenSource, onInvalidSession: () => void = () => {}): AuthTokenProvider {
  return {
    isAuthenticated: () => Boolean(source.authenticated && source.token),
    getValidAccessToken: async (minValidity = 30) => {
      if (!source.authenticated) { onInvalidSession(); throw new Error('SESSION_REQUIRED'); }
      try { await source.updateToken(minValidity); }
      catch { onInvalidSession(); throw new Error('SESSION_REFRESH_FAILED'); }
      if (!source.token) { onInvalidSession(); throw new Error('SESSION_REQUIRED'); }
      return source.token;
    }
  };
}
