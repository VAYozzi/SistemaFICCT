import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../../services/apiClient';
import { History, Search } from 'lucide-react';

export const AuditoriaPage: React.FC = () => {
  const [moduloFiltro, setModuloFiltro] = React.useState('');
  const [accionFiltro, setAccionFiltro] = React.useState('');

  const { data: auditData, isLoading, refetch } = useQuery({
    queryKey: ['auditoria', moduloFiltro, accionFiltro],
    queryFn: async () => {
      let url = '/auditoria?page=0&size=20';
      if (moduloFiltro) url += `&modulo=${moduloFiltro}`;
      if (accionFiltro) url += `&accion=${accionFiltro}`;
      const res = await apiClient.get(url);
      return res.data.data;
    },
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
          <History className="w-5 h-5 text-cyan-600" />
          Bitácora de Auditoría Inmutable
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Registro inalterable de eventos, transacciones de seguridad y cambios de estado (RN-03 / RF-21).
        </p>
      </div>

      {/* Filtros */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-wrap gap-4 items-center">
        <div>
          <label className="block text-[11px] font-semibold text-slate-600 mb-1">Módulo</label>
          <input
            type="text"
            placeholder="ej. AUTH, USUARIOS"
            value={moduloFiltro}
            onChange={(e) => setModuloFiltro(e.target.value)}
            className="px-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-600"
          />
        </div>
        <div>
          <label className="block text-[11px] font-semibold text-slate-600 mb-1">Acción</label>
          <input
            type="text"
            placeholder="ej. LOGIN_OK, CREATE"
            value={accionFiltro}
            onChange={(e) => setAccionFiltro(e.target.value)}
            className="px-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-600"
          />
        </div>
        <div className="pt-4">
          <button
            onClick={() => refetch()}
            className="px-3 py-1.5 bg-cyan-700 hover:bg-cyan-800 text-white text-xs font-semibold rounded-lg shadow flex items-center gap-1.5"
          >
            <Search className="w-3.5 h-3.5" /> Filtrar
          </button>
        </div>
      </div>

      {/* Tabla de Eventos */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase">
            <tr>
              <th className="px-6 py-3">ID</th>
              <th className="px-6 py-3">Fecha y Hora</th>
              <th className="px-6 py-3">Módulo</th>
              <th className="px-6 py-3">Acción</th>
              <th className="px-6 py-3">Actor</th>
              <th className="px-6 py-3">IP Origen</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-slate-700">
            {isLoading ? (
              <tr>
                <td colSpan={6} className="px-6 py-8 text-center text-slate-400">
                  Cargando eventos de auditoría...
                </td>
              </tr>
            ) : (
              auditData?.content?.map((b: any) => (
                <tr key={b.id} className="hover:bg-slate-50/60 transition-colors">
                  <td className="px-6 py-3 font-mono text-[11px] text-slate-400">#{b.id}</td>
                  <td className="px-6 py-3 text-slate-600 font-mono text-[11px]">
                    {new Date(b.fechaHora).toLocaleString()}
                  </td>
                  <td className="px-6 py-3 font-semibold text-slate-800">{b.modulo}</td>
                  <td className="px-6 py-3">
                    <span
                      className={`inline-block px-2 py-0.5 rounded font-mono text-[10px] font-semibold ${
                        b.accion.includes('FAIL')
                          ? 'bg-red-100 text-red-800'
                          : b.accion.includes('OK')
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-blue-100 text-blue-800'
                      }`}
                    >
                      {b.accion}
                    </span>
                  </td>
                  <td className="px-6 py-3 text-slate-900 font-medium">{b.correoActor || 'ANÓNIMO'}</td>
                  <td className="px-6 py-3 font-mono text-[11px] text-slate-500">{b.direccionIp}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

