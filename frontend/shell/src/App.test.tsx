// @vitest-environment jsdom
import {render,screen} from '@testing-library/react';import {Provider} from 'react-redux';import {configureStore} from '@reduxjs/toolkit';import {sessionSlice} from '@operational/shared';import {describe,it,expect,vi} from 'vitest';import App from './App';
vi.mock('requestsMfe/Routes',()=>({default:()=> <p>Remoto cargado</p>}));vi.mock('./auth',()=>({keycloak:{login:vi.fn()},authBridge:()=>({logout:vi.fn()})}));
function show(authenticated=false,roles:any[]=[]){const store=configureStore({reducer:{session:sessionSlice.reducer},preloadedState:{session:{initialized:true,authenticated,roles}}});return render(<Provider store={store}><App/></Provider>)}
describe('shell',()=>{it('muestra login sin sesión',()=>{show();expect(screen.getByRole('button',{name:'Iniciar sesión'})).toBeInTheDocument()});it('muestra navegación autorizada',()=>{show(true,['SUPERVISOR']);expect(screen.getByRole('link',{name:'Indicadores'})).toBeInTheDocument()})});
