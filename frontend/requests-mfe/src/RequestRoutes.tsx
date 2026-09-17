import {Navigate,Route,Routes} from 'react-router-dom';
import {Typography} from '@mui/material';
import type {AuthBridge,Role} from '@operational/shared';
import {CreatePage,DetailPage,InboxPage} from './pages';

function RoleRoute({auth,role,children}:{auth:AuthBridge;role:Role;children:React.ReactNode}){
  return auth.session.roles.includes(role)?children:<Navigate to="/sin-autorizacion" replace/>;
}

/** Rutas relativas al mount point /solicitudes/*. No crea un Router propio. */
export default function RemoteRoutes({auth}:{auth:AuthBridge}){
  return <Routes>
    <Route index element={<InboxPage auth={auth}/>}/>
    <Route path="nueva" element={<RoleRoute auth={auth} role="SOLICITANTE"><CreatePage auth={auth}/></RoleRoute>}/>
    <Route path=":id" element={<DetailPage auth={auth}/>}/>
    <Route path="*" element={<Typography role="alert" variant="h5">Ruta de solicitudes no encontrada</Typography>}/>
  </Routes>;
}
