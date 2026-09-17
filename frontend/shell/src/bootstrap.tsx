import React from 'react'; import {createRoot} from 'react-dom/client'; import {Provider} from 'react-redux'; import {initializeAuth} from './auth'; import {store} from './store'; import App from './App'; import './styles.css';
void initializeAuth().finally(()=>createRoot(document.getElementById('root')!).render(<React.StrictMode><Provider store={store}><App/></Provider></React.StrictMode>));
