import React, { useState, useEffect } from 'react';
import { apiClient } from '../../services/apiClient';

interface Tramite {
  id: number;
  codigoTramite: string;
  expedienteNombre: string;
  expedienteRegistro: string;
  tipoTramiteNombre: string;
  estadoActual: string;
  dictamenDirector?: string;
  dictamenCoordinador?: string;
  fechaIngreso: string;
  fechaConclusion?: string;
}

export const TramitesPage: React.FC = () => {
  const [tramites, setTramites] = useState<Tramite[]>([]);
  const [loading, setLoading] = useState(true);
  const [tramiteSeleccionado, setTramiteSeleccionado] = useState<Tramite | null>(null);
  const [nuevoEstado, setNuevoEstado] = useState('EN_VERIFICACION');
  const [dictamen, setDictamen] = useState('');
  const [procesando, setProcesando] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);


  const cargarTramite = async () => {
    try {
      setLoading(true);
      // Obtenemos el trámite de prueba existente en la base de datos
      const res = await apiClient.get('/tramites/1');
      if (res.data?.data) {
        setTramites([res.data.data]);
      }
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarTramite();
  }, []);

  const handleTransicion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!tramiteSeleccionado) return;
    setErrorMsg(null);
    setProcesando(true);

    try {
      const res = await apiClient.post(`/tramites/${tramiteSeleccionado.id}/transicion`, {
        estadoNuevo: nuevoEstado,
        observacionODictamen: dictamen,
      });

      setTramiteSeleccionado(res.data.data);
      setTramites([res.data.data]);
      setDictamen('');
      alert(`Transición a "${res.data.data.estadoActual}" realizada con éxito.`);
    } catch (err: any) {
      setErrorMsg(
        err.response?.data?.message || 'Error al ejecutar la transición de estado del trámite.'
      );
    } finally {
      setProcesando(false);
    }
  };

  const getBadgeClass = (estado: string) => {
    switch (estado) {
      case 'INGRESADO':
        return 'bg-blue-100 text-blue-800';
      case 'EN_VERIFICACION':
        return 'bg-amber-100 text-amber-800';
      case 'CON_DICTAMEN':
        return 'bg-purple-100 text-purple-800';
      case 'APROBADO':
        return 'bg-emerald-100 text-emerald-800';
      case 'RECHAZADO':
        return 'bg-rose-100 text-rose-800';
      case 'CONCLUIDO':
        return 'bg-slate-200 text-slate-800';
      default:
        return 'bg-slate-100 text-slate-700';
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Gestión de Trámites Académicos</h1>
        <p className="text-sm text-slate-500">
          Flujo de convalidaciones, traspasos y graduación con máquina de estados estricta y soberanía humana (CU-07, CU-08, CU-09).
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Listado de Trámites */}
        <div className="lg:col-span-2 bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
          <div className="p-4 border-b border-slate-200 font-semibold text-slate-800">
            Trámites en Curso
          </div>
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 text-slate-600 text-xs font-semibold uppercase tracking-wider border-b border-slate-200">
                <th className="py-3 px-4">Código</th>
                <th className="py-3 px-4">Estudiante</th>
                <th className="py-3 px-4">Tipo</th>
                <th className="py-3 px-4">Estado</th>
                <th className="py-3 px-4">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm text-slate-700">
              {loading ? (
                <tr>
                  <td colSpan={5} className="text-center py-8 text-slate-400">
                    Cargando trámites...
                  </td>
                </tr>
              ) : tramites.length === 0 ? (
                <tr>
                  <td colSpan={5} className="text-center py-8 text-slate-400">
                    No hay trámites registrados.
                  </td>
                </tr>
              ) : (
                tramites.map((t) => (
                  <tr key={t.id} className="hover:bg-slate-50 transition">
                    <td className="py-3 px-4 font-mono font-bold text-blue-600">{t.codigoTramite}</td>
                    <td className="py-3 px-4">
                      <div className="font-medium text-slate-900">{t.expedienteNombre}</div>
                      <div className="text-xs text-slate-400">Reg: {t.expedienteRegistro}</div>
                    </td>
                    <td className="py-3 px-4 text-slate-600">{t.tipoTramiteNombre}</td>
                    <td className="py-3 px-4">
                      <span className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold ${getBadgeClass(t.estadoActual)}`}>
                        {t.estadoActual}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <button
                        onClick={() => {
                          setTramiteSeleccionado(t);
                          setErrorMsg(null);
                        }}
                        className="text-xs font-medium text-blue-600 hover:text-blue-800 underline"
                      >
                        Gestionar
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Panel de Gestión de Estado y Dictamen */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-5 space-y-4">
          <h2 className="text-base font-bold text-slate-800 border-b pb-2">
            Máquina de Estados de Trámite
          </h2>

          {tramiteSeleccionado ? (
            <form onSubmit={handleTransicion} className="space-y-4 text-sm">
              <div>
                <span className="text-xs text-slate-400 block">Trámite Activo:</span>
                <span className="font-mono font-bold text-slate-800">
                  {tramiteSeleccionado.codigoTramite}
                </span>
              </div>

              <div>
                <span className="text-xs text-slate-400 block">Estado Actual:</span>
                <span
                  className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold mt-1 ${getBadgeClass(
                    tramiteSeleccionado.estadoActual
                  )}`}
                >
                  {tramiteSeleccionado.estadoActual}
                </span>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Siguiente Estado
                </label>
                <select
                  value={nuevoEstado}
                  onChange={(e) => setNuevoEstado(e.target.value)}
                  className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                >
                  <option value="EN_VERIFICACION">EN_VERIFICACION (Revisión técnica)</option>
                  <option value="OBSERVADO">OBSERVADO (Faltan requisitos)</option>
                  <option value="CON_DICTAMEN">CON_DICTAMEN (Dictamen Coordinación)</option>
                  <option value="APROBADO">APROBADO (Resolución Director)</option>
                  <option value="RECHAZADO">RECHAZADO (Denegado Director)</option>
                  <option value="CONCLUIDO">CONCLUIDO (Finalizado)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Dictamen / Observación Institucional *
                </label>
                <textarea
                  rows={3}
                  required
                  value={dictamen}
                  onChange={(e) => setDictamen(e.target.value)}
                  placeholder="Detalle de motivos técnicos o resolutivos..."
                  className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {errorMsg && (
                <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700">
                  {errorMsg}
                </div>
              )}

              <button
                type="submit"
                disabled={procesando}
                className="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium text-sm transition disabled:opacity-50"
              >
                {procesando ? 'Procesando...' : 'Aplicar Transición'}
              </button>
            </form>
          ) : (
            <div className="text-center py-12 text-slate-400 text-sm">
              Seleccione un trámite de la lista para gestionar su ciclo de vida y emitir dictámenes.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
