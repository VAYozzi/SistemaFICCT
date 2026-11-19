import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface UserSession {
  token: string;
  usuarioId: number;
  nombreCompleto: string;
  correoInstitucional: string;
  cargo: string;
  rolCodigo: 'ROLE_DIRECTOR' | 'ROLE_COORDINADOR' | 'ROLE_AUXILIAR' | string;
  rolNombre: string;
}

interface AuthState {
  session: UserSession | null;
  isAuthenticated: boolean;
  setSession: (session: UserSession) => void;
  clearSession: () => void;
  hasRole: (roles: string[]) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      session: null,
      isAuthenticated: false,
      setSession: (session) => set({ session, isAuthenticated: true }),
      clearSession: () => set({ session: null, isAuthenticated: false }),
      hasRole: (roles) => {
        const currentRole = get().session?.rolCodigo;
        if (!currentRole) return false;
        return roles.includes(currentRole);
      },
    }),
    {
      name: 'ficct-auth-storage',
    }
  )
);

