import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../../services/apiClient';
import { Users, UserPlus, CheckCircle, XCircle } from 'lucide-react';

export const UsuariosPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = React.useState(false);
  const [correoDestino, setCorreoDestino] = React.useState('');
  const [rolId, setRolId] = React.useState<number>(2);
  const [msg, setMsg] = React.useState<{ text: string; error: boolean } | null>(null);

  const { data: usuariosData, isLoading } = useQuery({
    queryKey: ['usuarios'],
    queryFn: async () => {
      const res = await apiClient.get('/usuarios?page=0&size=20');
      return res.data.data;
    },
  });

  const cambiarEstadoMutation = useMutation({
    mutationFn: async ({ id, activo }: { id: number; activo: boolean }) => {
      const res = await apiClient.patch(`/usuarios/${id}/estado`, { activo });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
    },
    onError: (err: any) => {
      alert(err.response?.data?.message || 'Error al cambiar estado');
    },
  });

  const crearInvitacionMutation = useMutation({
    mutationFn: async () => {
      const res = await apiClient.post('/usuarios/invitaciones', { correoDestino, rolId });
      return res.data;
    },
    onSuccess: () => {
      setMsg({ text: 'Invitación creada exitosamente', error: false });
      setCorreoDestino('');
      setModalOpen(false);
    },
    onError: (err: any) => {
      setMsg({ text: err.response?.data?.message || 'Error al crear invitación', error: true });
    },
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <Users className="w-5 h-5 text-indigo-600" />
            Administración de Personal y Roles
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Gestión de usuarios internos de la Dirección de Carrera (RBAC).
          </p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-lg shadow flex items-center gap-2 transition-colors"
        >
          <UserPlus className="w-4 h-4" />
          Nueva Invitación
        </button>
      </div>

      {msg && (
        <div
          className={`p-3 text-xs rounded-lg border ${
            msg.error ? 'bg-red-50 text-red-700 border-red-200' : 'bg-emerald-50 text-emerald-700 border-emerald-200'
          }`}
        >
          {msg.text}
        </div>
      )}

      {/* Tabla de Usuarios */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase">
            <tr>
              <th className="px-6 py-3">Nombre Completo</th>
              <th className="px-6 py-3">Correo Institucional</th>
              <th className="px-6 py-3">Cargo</th>
              <th className="px-6 py-3">Rol</th>
              <th className="px-6 py-3">Estado</th>
              <th className="px-6 py-3 text-right">Acción</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-slate-700">
            {isLoading ? (
              <tr>
                <td colSpan={6} className="px-6 py-8 text-center text-slate-400">
                  Cargando usuarios...
                </td>
              </tr>
            ) : (
              usuariosData?.content?.map((u: any) => (
                <tr key={u.id} className="hover:bg-slate-50/60 transition-colors">
                  <td className="px-6 py-3.5 font-medium text-slate-900">{u.nombreCompleto}</td>
                  <td className="px-6 py-3.5 font-mono text-[11px] text-slate-600">{u.correoInstitucional}</td>
                  <td className="px-6 py-3.5">{u.cargo}</td>
                  <td className="px-6 py-3.5">
                    <span
                      className={`inline-block px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                        u.rolCodigo === 'ROLE_DIRECTOR'
                          ? 'bg-blue-100 text-blue-800'
                          : u.rolCodigo === 'ROLE_COORDINADOR'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-slate-100 text-slate-800'
                      }`}
                    >
                      {u.rolNombre}
                    </span>
                  </td>
                  <td className="px-6 py-3.5">
                    {u.activo ? (
                      <span className="inline-flex items-center gap-1 text-emerald-600 font-medium">
                        <CheckCircle className="w-3.5 h-3.5" /> Activo
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 text-slate-400 font-medium">
                        <XCircle className="w-3.5 h-3.5" /> Inactivo
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-3.5 text-right">
                    {u.rolCodigo !== 'ROLE_DIRECTOR' && (
                      <button
                        onClick={() => cambiarEstadoMutation.mutate({ id: u.id, activo: !u.activo })}
                        className={`text-[11px] font-medium px-2.5 py-1 rounded transition-colors ${
                          u.activo
                            ? 'text-red-600 hover:bg-red-50'
                            : 'text-emerald-600 hover:bg-emerald-50'
                        }`}
                      >
                        {u.activo ? 'Desactivar' : 'Activar'}
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Nueva Invitación */}
      {modalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-xl max-w-sm w-full p-6 space-y-4">
            <h2 className="text-sm font-bold text-slate-900">Invitar Nuevo Funcionario</h2>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Correo Institucional
              </label>
              <input
                type="email"
                placeholder="funcionario@uagrm.edu.bo"
                value={correoDestino}
                onChange={(e) => setCorreoDestino(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-600"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Rol a Asignar
              </label>
              <select
                value={rolId}
                onChange={(e) => setRolId(Number(e.target.value))}
                className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-600"
              >
                <option value={2}>ROLE_COORDINADOR (Coordinador Académico)</option>
                <option value={3}>ROLE_AUXILIAR (Auxiliar de Oficina / Secretaría)</option>
              </select>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setModalOpen(false)}
                className="px-3 py-1.5 text-xs text-slate-600 hover:bg-slate-100 rounded-lg font-medium"
              >
                Cancelar
              </button>
              <button
                onClick={() => crearInvitacionMutation.mutate()}
                disabled={!correoDestino || crearInvitacionMutation.isPending}
                className="px-3 py-1.5 text-xs bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50"
              >
                {crearInvitacionMutation.isPending ? 'Emitiendo...' : 'Enviar Invitación'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

