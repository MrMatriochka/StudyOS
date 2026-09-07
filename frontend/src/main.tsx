import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom';
import { App } from './App';
import { Accueil } from './pages/Accueil';
import { Semaine } from './pages/Semaine';
import { Qualification } from './pages/Qualification';
import { Matieres } from './pages/Matieres';
import { Matiere } from './pages/Matiere';
import { Recherche } from './pages/Recherche';
import './index.css';

const queryClient = new QueryClient();

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <Navigate to="/accueil" replace /> },
      { path: 'accueil', element: <Accueil /> },
      { path: 'semaine', element: <Semaine /> },
      { path: 'matieres', element: <Matieres /> },
      { path: 'matiere/:id', element: <Matiere /> },
      { path: 'qualification', element: <Qualification /> },
      { path: 'recherche', element: <Recherche /> },
    ],
  },
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
);
