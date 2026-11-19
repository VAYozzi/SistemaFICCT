import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { apiClient } from '../../services/apiClient';
import { useAuthStore } from '../../stores/authStore';
import { ShieldCheck, LogIn, AlertCircle, Building2, Lock, Mail } from 'lucide-react';

const loginSchema = z.object({
  correoInstitucional: z
    .string()
    .min(1, 'El correo institucional es obligatorio')
    .email('Formato de correo electrónico inválido'),
  password: z.string().min(1, 'La contraseña es obligatoria'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export const LoginPage: React.FC = () => {
  const [errorMsg, setErrorMsg] = React.useState<string | null>(null);
  const [loading, setLoading] = React.useState(false);
  const setSession = useAuthStore((state) => state.setSession);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setLoading(true);
    setErrorMsg(null);
    try {
      const response = await apiClient.post('/auth/login', data);
      if (response.data.success) {
        setSession(response.data.data);
        window.location.href = '/dashboard';
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error de conexión con el servidor transaccional.';
      setErrorMsg(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-850 to-blue-950 flex flex-col justify-center items-center px-4 py-12 relative overflow-hidden">
      {/* Elementos de fondo decorativos sobrios */}
      <div className="absolute -top-32 -right-32 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-32 -left-32 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-md w-full bg-white rounded-2xl shadow-2xl border border-slate-100 overflow-hidden relative z-10">
        {/* Cabecera institucional */}
        <div className="bg-gradient-to-r from-blue-900 to-indigo-900 px-8 py-8 text-white text-center relative">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-white/10 backdrop-blur-sm border border-white/20 mb-3 shadow-inner">
            <ShieldCheck className="w-9 h-9 text-blue-200" />
          </div>
          <h1 className="text-xl font-bold tracking-tight text-white">
            Dirección de Carrera de Ingeniería en Sistemas
          </h1>
          <p className="text-xs font-semibold uppercase tracking-wider text-blue-200 mt-1 flex items-center justify-center gap-1.5">
            <Building2 className="w-3.5 h-3.5" />
            FICCT — UAGRM
          </p>
        </div>

        {/* Cuerpo del Formulario */}
        <div className="p-8">
          <div className="mb-6">
            <h2 className="text-lg font-bold text-slate-800">Iniciar Sesión</h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Ingrese sus credenciales institucionales autorizadas
            </p>
          </div>

          {errorMsg && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 rounded-xl flex items-start gap-3 text-xs text-rose-700 animate-fadeIn">
              <AlertCircle className="w-4 h-4 flex-shrink-0 text-rose-500 mt-0.5" />
              <div className="font-medium leading-relaxed">{errorMsg}</div>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4.5">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Correo Institucional
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  placeholder="usuario@uagrm.edu.bo"
                  className="w-full pl-10 pr-3.5 py-2.5 text-sm bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition-all placeholder:text-slate-400 text-slate-800"
                  {...register('correoInstitucional')}
                />
              </div>
              {errors.correoInstitucional && (
                <p className="text-xs text-rose-600 mt-1 font-medium">{errors.correoInstitucional.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Contraseña
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  placeholder="••••••••••••"
                  className="w-full pl-10 pr-3.5 py-2.5 text-sm bg-slate-50 border border-slate-300 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent transition-all placeholder:text-slate-400 text-slate-800"
                  {...register('password')}
                />
              </div>
              {errors.password && (
                <p className="text-xs text-rose-600 mt-1 font-medium">{errors.password.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 py-3 px-4 bg-gradient-to-r from-blue-900 to-indigo-900 hover:from-blue-800 hover:to-indigo-800 text-white font-semibold text-sm rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer disabled:cursor-not-allowed"
            >
              {loading ? (
                <span className="inline-block animate-spin mr-2">⟳</span>
              ) : (
                <LogIn className="w-4 h-4" />
              )}
              {loading ? 'Validando Credenciales...' : 'Ingresar al Sistema'}
            </button>
          </form>

          <div className="mt-8 pt-5 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
            <span>Sistema Web de Gestión Administrativa</span>
            <span className="font-medium text-slate-500">v1.0</span>
          </div>
        </div>
      </div>

      <div className="mt-6 text-center text-xs text-slate-400 max-w-sm">
        <p>Facultad Integral de Computación, Carreras de Informática y Telecomunicaciones (UAGRM)</p>
      </div>
    </div>
  );
};
