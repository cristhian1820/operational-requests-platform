import type {Meta,StoryObj} from '@storybook/react';import {StatusChip,EmptyState,ErrorState} from './components';
const meta={title:'Solicitudes/StatusChip',component:StatusChip} satisfies Meta<typeof StatusChip>;export default meta;type Story=StoryObj<typeof meta>;
export const Registrada:Story={args:{status:'REGISTRADA'}};export const EnAtencion:Story={args:{status:'EN_ATENCION'}};export const Resuelta:Story={args:{status:'RESUELTA'}};export const Cerrada:Story={args:{status:'CERRADA'}};
export const Vacio={render:()=> <EmptyState title="Sin solicitudes"/>};export const ErrorRecuperable={render:()=> <ErrorState message="No fue posible cargar" onRetry={()=>undefined}/>};
