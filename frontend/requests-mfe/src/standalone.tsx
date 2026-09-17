import React from 'react';
import {createRoot} from 'react-dom/client';
import {CssBaseline,ThemeProvider,createTheme} from '@mui/material';
import {StandaloneRoot} from './standalone-app';

createRoot(document.getElementById('root')!).render(<React.StrictMode><ThemeProvider theme={createTheme()}><CssBaseline/><StandaloneRoot/></ThemeProvider></React.StrictMode>);
