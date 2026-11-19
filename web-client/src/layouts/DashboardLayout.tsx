import React, { useState } from 'react';
import { useAuthStore } from '../stores/authStore';
import {
  Users,
  FileText,
  Calendar,
  AlertTriangle,
  History,
  LogOut,
  ShieldCheck,
  Building,
  Menu,
  X,
  ChevronRight,
} from 'lucide-react';

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({ children }) => {
  const { session, clearSession, hasRole } = useAuthStore();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    clearSession();
    window.location.href = '/login';
  };

  const navItemClass = (active: boolean) =>
    `flex items-center gap-3 px-3.5 py-2.5 text-sm font-medium rounded-xl transition-all ${
      active
        ? 'bg-blue-600 text-white shadow-sm shadow-blue-500/20'
        : 'text-slate-300 hover:text-white hover:bg-slate-800/70'
    }`;

  const currentPath = window.location.pathname;

  return (
    <div className="min-h-screen flex bg-slate-100/75 text-slate-800 antialiased font-sans">
      {/* Sidebar Lateral para Desktop */}
      <aside className="hidden lg:flex w-72 bg-slate-900 text-white flex-col justify-between flex-shrink-0 border-r border-slate-800 shadow-xl">
        <div className="flex flex-col flex-1 overflow-y-auto">
          {/* Logo / Encabezado FICCT */}
          <div className="h-20 flex items-center gap-3.5 px-6 border-b border-slate-800/80 bg-slate-950/40">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-700 to-indigo-600 flex items-center justify-center font-bold text-white shadow-md text-lg tracking-wider border border-white/10">
              <Building className="w-5 h-5 text-white" />
            </div>
            <div className="flex-1 min-w-0">
              <h2 className="text-sm font-bold tracking-tight text-white leading-tight truncate">
                FICCT — UAGRM
              </h2>
              <p className="text-[11px] text-blue-300 font-medium truncate">
                Ingeniería en Sistemas
              </p>
            </div>
          </div>

          {/* Menú de Navegación según Rol */}
          <nav className="p-4 space-y-6 flex-1">
            <div>
              <div className="px-3 pb-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                Gestión Operativa
              </div>
              <div className="space-y-1">
                <a
                  href="/correspondencia"
                  className={navItemClass(currentPath.startsWith('/correspondencia'))}
                >
                  <FileText className="w-4 h-4 text-blue-400 flex-shrink-0" />
                  <span className="flex-1">Correspondencia</span>
                </a>

                <a
                  href="/tramites"
                  className={navItemClass(currentPath.startsWith('/tramites'))}
                >
                  <Calendar className="w-4 h-4 text-cyan-400 flex-shrink-0" />
                  <span className="flex-1">Trámites Académicos</span>
                </a>

                <a
                  href="/ocr"
                  className={navItemClass(currentPath.startsWith('/ocr'))}
                >
                  <FileText className="w-4 h-4 text-emerald-400 flex-shrink-0" />
                  <span className="flex-1">Visor Dual OCR</span>
                </a>
              </div>
            </div>

            {hasRole(['ROLE_DIRECTOR', 'ROLE_COORDINADOR']) && (
              <div>
                <div className="px-3 pb-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                  Planificación y Control
                </div>
                <div className="space-y-1">
                  <a
                    href="/dashboard"
                    className={navItemClass(currentPath === '/dashboard' || currentPath === '/alertas')}
                  >
                    <AlertTriangle className="w-4 h-4 text-amber-400 flex-shrink-0" />
                    <span className="flex-1">Panel y Alertas</span>
                  </a>

                  <a
                    href="/academico"
                    className={navItemClass(currentPath.startsWith('/academico'))}
                  >
                    <Calendar className="w-4 h-4 text-indigo-400 flex-shrink-0" />
                    <span className="flex-1">Malla y Cupos</span>
                  </a>
                </div>
              </div>
            )}

            {hasRole(['ROLE_DIRECTOR']) && (
              <div>
                <div className="px-3 pb-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                  Gobierno y Auditoría
                </div>
                <div className="space-y-1">
                  <a
                    href="/usuarios"
                    className={navItemClass(currentPath.startsWith('/usuarios'))}
                  >
                    <Users className="w-4 h-4 text-purple-400 flex-shrink-0" />
                    <span className="flex-1">Personal y Roles</span>
                  </a>

                  <a
                    href="/auditoria"
                    className={navItemClass(currentPath.startsWith('/auditoria'))}
                  >
                    <History className="w-4 h-4 text-rose-400 flex-shrink-0" />
                    <span className="flex-1">Bitácora Inmutable</span>
                  </a>
                </div>
              </div>
            )}
          </nav>
        </div>

        {/* Perfil del Usuario y Logout */}
        <div className="p-4 border-t border-slate-800/80 bg-slate-950/60">
          <div className="bg-slate-800/50 rounded-xl p-3 border border-slate-700/50 flex items-center justify-between gap-3">
            <div className="flex-1 min-w-0">
              <p className="text-xs font-semibold text-white truncate leading-tight">
                {session?.nombreCompleto || 'Usuario Autenticado'}
              </p>
              <div className="flex items-center gap-1.5 mt-1">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                <span className="text-[10px] text-blue-300 font-medium truncate">
                  {session?.rolNombre || session?.rolCodigo}
                </span>
              </div>
            </div>
            <button
              onClick={handleLogout}
              title="Cerrar sesión"
              className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-700/50 transition-colors flex-shrink-0 cursor-pointer"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Contenido Principal con Topbar */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Barra Superior Institucional */}
        <header className="h-16 bg-white border-b border-slate-200/80 flex items-center justify-between px-6 sm:px-8 shadow-xs sticky top-0 z-30">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden p-2 rounded-lg text-slate-600 hover:bg-slate-100"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>

            <div className="flex items-center gap-2 text-xs">
              <span className="font-semibold text-slate-500 uppercase tracking-wider">SistemaFICCT</span>
              <ChevronRight className="w-3.5 h-3.5 text-slate-300" />
              <span className="font-bold text-slate-800">
                {currentPath === '/dashboard' || currentPath === '/alertas'
                  ? 'Panel Directivo y Alertas'
                  : currentPath.substring(1).toUpperCase()}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-50 border border-emerald-200/80 text-emerald-800 text-xs font-medium">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              <span>Servicios Activos</span>
            </div>

            <div className="flex items-center gap-2 border-l border-slate-200 pl-4 text-xs text-slate-600">
              <ShieldCheck className="w-4 h-4 text-blue-600" />
              <span className="hidden md:inline font-medium">{session?.correoInstitucional}</span>
            </div>
          </div>
        </header>

        {/* Menú Móvil Desplegable */}
        {mobileMenuOpen && (
          <div className="lg:hidden bg-slate-900 text-white p-4 border-b border-slate-800 space-y-2 z-40">
            <a href="/correspondencia" className={navItemClass(currentPath.startsWith('/correspondencia'))}>
              Correspondencia
            </a>
            <a href="/tramites" className={navItemClass(currentPath.startsWith('/tramites'))}>
              Trámites Académicos
            </a>
            <a href="/ocr" className={navItemClass(currentPath.startsWith('/ocr'))}>
              Visor Dual OCR
            </a>
            {hasRole(['ROLE_DIRECTOR', 'ROLE_COORDINADOR']) && (
              <>
                <a href="/dashboard" className={navItemClass(currentPath === '/dashboard')}>
                  Panel Directivo y Alertas
                </a>
                <a href="/academico" className={navItemClass(currentPath.startsWith('/academico'))}>
                  Malla y Cupos
                </a>
              </>
            )}
            {hasRole(['ROLE_DIRECTOR']) && (
              <>
                <a href="/usuarios" className={navItemClass(currentPath.startsWith('/usuarios'))}>
                  Personal y Roles
                </a>
                <a href="/auditoria" className={navItemClass(currentPath.startsWith('/auditoria'))}>
                  Bitácora de Auditoría
                </a>
              </>
            )}
            <div className="pt-2 border-t border-slate-800">
              <button
                onClick={handleLogout}
                className="w-full flex items-center justify-center gap-2 py-2 text-rose-400 text-sm font-semibold hover:bg-slate-800 rounded-lg"
              >
                <LogOut className="w-4 h-4" />
                Cerrar Sesión
              </button>
            </div>
          </div>
        )}

        {/* Vista dinámica / Contenedor principal con scroll suave */}
        <main className="flex-1 p-6 sm:p-8 overflow-y-auto max-w-7xl w-full mx-auto">
          {children}
        </main>
      </div>
    </div>
  );
};
