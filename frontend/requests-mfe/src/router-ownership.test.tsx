// @vitest-environment jsdom
import {render,screen} from '@testing-library/react';
import {MemoryRouter,Route,Routes} from 'react-router-dom';
import {describe,expect,it,vi} from 'vitest';
import type {AuthBridge} from '@operational/shared';
import RemoteRoutes from './RequestRoutes';
import {StandaloneRoot} from './standalone-app';

vi.mock('./pages',()=>({InboxPage:()=> <p>Bandeja remota</p>,CreatePage:()=> <p>Formulario remoto</p>,DetailPage:()=> <p>Detalle remoto</p>}));
vi.mock('keycloak-js',()=>({default:class {init(){return new Promise(()=>undefined)}}}));
const auth:AuthBridge={session:{initialized:true,authenticated:true,roles:['ANALISTA']},authTokenProvider:{isAuthenticated:()=>true,getValidAccessToken:vi.fn().mockResolvedValue('test-token')},login:vi.fn(),logout:vi.fn()};

describe('ownership del router',()=>{
  it('el módulo expuesto usa el router anfitrión sin crear BrowserRouter anidado',()=>{render(<MemoryRouter initialEntries={['/solicitudes']}><Routes><Route path="/solicitudes/*" element={<RemoteRoutes auth={auth}/>}/></Routes></MemoryRouter>);expect(screen.getByText('Bandeja remota')).toBeInTheDocument()});
  it('el bootstrap standalone proporciona BrowserRouter',()=>{window.history.pushState({},'', '/solicitudes');render(<StandaloneRoot/>);expect(screen.getByRole('status')).toHaveTextContent('Recuperando sesión')});
});
