import {configureStore} from '@reduxjs/toolkit'; import {sessionSlice} from '@operational/shared';
export const store=configureStore({reducer:{session:sessionSlice.reducer}}); export type RootState=ReturnType<typeof store.getState>;
