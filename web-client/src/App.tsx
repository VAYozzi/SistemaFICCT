import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { LoginPage } from './pages/auth/LoginPage';
import { DashboardLayout } from './layouts/DashboardLayout';
import { UsuariosPage } from './pages/usuarios/UsuariosPage';
import { AuditoriaPage } from './pages/auditoria/AuditoriaPage';
import { CorrespondenciaPage } from './pages/documental/CorrespondenciaPage';
import { TramitesPage } from './pages/documental/TramitesPage';
import { OcrVisorPage } from './pages/documental/OcrVisorPage';
import { PlanificacionAcademicaPage } from './pages/academico/PlanificacionAcademicaPage';
import { DashboardInstitucionalPage } from './pages/institucional/DashboardInstitucionalPage';
import { useAuthStore } from './stores/authStore';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <DashboardLayout>{children}</DashboardLayout>;
};

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardInstitucionalPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/alertas"
            element={
              <ProtectedRoute>
                <DashboardInstitucionalPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/usuarios"
            element={
              <ProtectedRoute>
                <UsuariosPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/auditoria"
            element={
              <ProtectedRoute>
                <AuditoriaPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/correspondencia"
            element={
              <ProtectedRoute>
                <CorrespondenciaPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/tramites"
            element={
              <ProtectedRoute>
                <TramitesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/ocr"
            element={
              <ProtectedRoute>
                <OcrVisorPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/academico"
            element={
              <ProtectedRoute>
                <PlanificacionAcademicaPage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
