// @vitest-environment jsdom
import {render,screen,waitFor} from '@testing-library/react';
import {MemoryRouter,Route,Routes} from 'react-router-dom';
import {beforeEach,describe,expect,it,vi} from 'vitest';
import type {AuthBridge,Role} from '@operational/shared';
import RemoteRoutes from './RequestRoutes';
import {requestsApi} from './api';

vi.mock('./api',()=>({requestsApi:vi.fn()}));

const category={id:'11111111-1111-4111-8111-111111111111',codigo:'SOP',nombre:'Soporte técnico'};
const emptyPage={content:[],page:0,size:20,totalElements:0,totalPages:0};
const api={categories:vi.fn(),list:vi.fn(),detail:vi.fn(),create:vi.fn(),assign:vi.fn(),observe:vi.fn(),transition:vi.fn()};
const auth=(roles:Role[]):AuthBridge=>({session:{initialized:true,authenticated:true,subject:'22222222-2222-2222-2222-222222222222',username:'local',roles},authTokenProvider:{isAuthenticated:()=>true,getValidAccessToken:vi.fn().mockResolvedValue('test-token')},login:vi.fn(),logout:vi.fn()});

function renderFederated(path:string,roles:Role[]){return render(<MemoryRouter initialEntries={[path]}><Routes><Route path="/solicitudes/*" element={<RemoteRoutes auth={auth(roles)}/>}/><Route path="/sin-autorizacion" element={<p>Sin autorización</p>}/></Routes></MemoryRouter>)}

beforeEach(()=>{vi.clearAllMocks();api.categories.mockResolvedValue([category]);api.list.mockResolvedValue(emptyPage);api.detail.mockImplementation(()=>new Promise(()=>undefined));vi.mocked(requestsApi).mockReturnValue(api)});

describe('routing federado',()=>{
  it('renderiza la bandeja en la ubicación global /solicitudes e inicia ambas consultas',async()=>{renderFederated('/solicitudes',['SOLICITANTE']);expect(screen.getByRole('heading',{name:'Bandeja de solicitudes'})).toBeInTheDocument();expect(screen.getByRole('status')).toHaveTextContent('Cargando solicitudes');await waitFor(()=>expect(api.categories).toHaveBeenCalledOnce());expect(api.list).toHaveBeenCalledOnce();expect(await screen.findByText('No hay resultados')).toBeInTheDocument()});
  it('muestra Nueva solicitud solamente a SOLICITANTE',async()=>{const view=renderFederated('/solicitudes',['SOLICITANTE']);expect(screen.getByRole('link',{name:'Nueva solicitud'})).toBeInTheDocument();view.unmount();renderFederated('/solicitudes',['ANALISTA']);expect(screen.queryByRole('link',{name:'Nueva solicitud'})).not.toBeInTheDocument()});
  it('renderiza creación en /solicitudes/nueva para SOLICITANTE',async()=>{renderFederated('/solicitudes/nueva',['SOLICITANTE']);expect(screen.getByRole('heading',{name:'Nueva solicitud'})).toBeInTheDocument();await waitFor(()=>expect(api.categories).toHaveBeenCalledOnce())});
  it('inicia la carga del detalle en /solicitudes/:id',async()=>{renderFederated('/solicitudes/33333333-3333-3333-3333-333333333333',['SOLICITANTE']);expect(screen.getByRole('status')).toHaveTextContent('Recuperando sesión y solicitud');await waitFor(()=>expect(api.detail).toHaveBeenCalledWith('33333333-3333-3333-3333-333333333333',expect.any(AbortSignal)))});
  it('muestra una vista controlada para una ruta desconocida',()=>{renderFederated('/solicitudes/ruta/desconocida',['ANALISTA']);expect(screen.getByRole('alert')).toHaveTextContent('Ruta de solicitudes no encontrada')});
});
