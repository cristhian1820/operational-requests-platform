import {useEffect,useState} from 'react';
import {BrowserRouter,Route,Routes} from 'react-router-dom';
import {Typography} from '@mui/material';
import Keycloak from 'keycloak-js';
import {createKeycloakTokenProvider,runtimeConfig,realmRoles,type AuthBridge,type SessionDescription} from '@operational/shared';
import RemoteRoutes from './RequestRoutes';

const keycloak=new Keycloak({url:runtimeConfig.keycloakUrl,realm:runtimeConfig.keycloakRealm,clientId:runtimeConfig.keycloakClientId});

export function StandaloneContent(){
  const [session,setSession]=useState<SessionDescription>({initialized:false,authenticated:false,roles:[]});
  useEffect(()=>{void keycloak.init({onLoad:'check-sso',pkceMethod:'S256'}).then(authenticated=>setSession({initialized:true,authenticated,subject:keycloak.subject,username:keycloak.tokenParsed?.preferred_username as string|undefined,roles:realmRoles(keycloak.tokenParsed)})).catch(()=>setSession({initialized:true,authenticated:false,roles:[]}));},[]);
  if(!session.initialized)return <p role="status">Recuperando sesión…</p>;
  if(!session.authenticated)return <button onClick={()=>void keycloak.login({redirectUri:location.href})}>Iniciar sesión</button>;
  const auth:AuthBridge={session,authTokenProvider:createKeycloakTokenProvider(keycloak,()=>setSession({...session,authenticated:false})),login:()=>keycloak.login(),logout:()=>keycloak.logout({redirectUri:location.origin})};
  return <Routes><Route path="/solicitudes/*" element={<RemoteRoutes auth={auth}/>}/><Route path="*" element={<Typography role="alert">Ruta no encontrada</Typography>}/></Routes>;
}

/** El BrowserRouter existe solo en el entrypoint standalone. */
export function StandaloneRoot(){return <BrowserRouter><StandaloneContent/></BrowserRouter>}
