import React, { useState, useEffect } from 'react';
import {
  Users,
  FileText,
  GraduationCap,
  AlertTriangle,
  CheckCircle,
  Download,
  DollarSign,
  TrendingUp,
} from 'lucide-react';
import { apiClient } from '../../services/apiClient';

interface KpisData {
  totalEstudiantes: number;
  totalTramitesEnCurso: number;
  totalTituladosHistorico: number;
  totalAlertasPendientes: number;
  alertasRojasPendientes: number;
  tituladosPorModalidad: Record<string, number>;
  presupuestoPoaTotal: number;
  porcentajeAvancePoaPromedio: number;
}

interface AlertaItem {
  id: number;
  categoria: string;
  nivelCriticidad: 'ROJO' | 'AMARILLO' | 'VERDE';
  titulo: string;
  mensajeDetalle: string;
  estado: string;
  fechaGeneracion: string;
}

export const DashboardInstitucionalPage: React.FC = () => {
  const [kpis, setKpis] = useState<KpisData | null>(null);
  const [alertas, setAlertas] = useState<AlertaItem[]>([]);
  const [loading, setLoading] = useState(true);

  // Modal para atender alerta
  const [alertaSeleccionada, setAlertaSeleccionada] = useState<AlertaItem | null>(null);
  const [resolucion, setResolucion] = useState('');
  const [procesandoAlerta, setProcesandoAlerta] = useState(false);

  // Generación de dossier
  const [generandoDossier, setGenerandoDossier] = useState(false);
  const [mensajeDossier, setMensajeDossier] = useState<string | null>(null);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      const [resKpis, resAlertas] = await Promise.all([
        apiClient.get('/institucional/dashboard/kpis'),
        apiClient.get('/institucional/alertas'),
      ]);
      setKpis(resKpis.data.data);
      setAlertas(resAlertas.data.data || []);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  const handleAtenderAlerta = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!alertaSeleccionada || !resolucion.trim()) return;
    setProcesandoAlerta(true);
    try {
      await apiClient.post(`/institucional/alertas/${alertaSeleccionada.id}/atender`, {
        accionTomada: resolucion.trim(),
      });
      setAlertaSeleccionada(null);
      setResolucion('');
      await cargarDatos();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error al atender la alerta.');
    } finally {
      setProcesandoAlerta(false);
    }
  };

  const handleGenerarDossier = async () => {
    setGenerandoDossier(true);
    setMensajeDossier(null);
    try {
      const res = await apiClient.post('/institucional/dossier/generar', {
        periodoCodigo: '2026-1',
        fechaDesde: '2026-01-01',
        fechaHasta: '2026-06-30',
        incluirModulos: ['CORRESPONDENCIA', 'TRAMITES', 'ALERTAS', 'POA'],
      });
      setMensajeDossier(
        `Dossier oficial generado: ${res.data.data.nombreArchivo} (Hash SHA-256: ${res.data.data.hashSha256.substring(
          0,
          16
        )}...)`
      );
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error al generar el dossier.');
    } finally {
      setGenerandoDossier(false);
    }
  };

  const getSemaforoBadge = (nivel: string) => {
    switch (nivel) {
      case 'ROJO':
        return 'bg-rose-100 text-rose-800 border-rose-300';
      case 'AMARILLO':
        return 'bg-amber-100 text-amber-800 border-amber-300';
      case 'VERDE':
        return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      default:
        return 'bg-slate-100 text-slate-800 border-slate-300';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Panel Directivo Institucional</h1>
          <p className="text-sm text-slate-500">
            KPIs consolidados, monitoreo semafórico de alertas y generación de informes oficiales (CU-26 a CU-30, CU-34).
          </p>
        </div>

        <button
          onClick={handleGenerarDossier}
          disabled={generandoDossier}
          className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold flex items-center gap-2 shadow-sm transition disabled:opacity-50"
        >
          <Download className="w-4 h-4" />
          {generandoDossier ? 'Compilando Dossier...' : 'Generar Dossier Oficial (PDF)'}
        </button>
      </div>

      {mensajeDossier && (
        <div className="p-3 bg-indigo-50 border border-indigo-200 text-indigo-900 rounded-xl text-xs flex items-center gap-2">
          <CheckCircle className="w-4 h-4 text-indigo-600" />
          {mensajeDossier}
        </div>
      )}

      {/* Tarjetas de KPIs Principales */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-medium block">Estudiantes</span>
            <span className="text-2xl font-bold text-slate-800">
              {loading ? '...' : kpis?.totalEstudiantes}
            </span>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-cyan-50 text-cyan-600 flex items-center justify-center font-bold">
            <FileText className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-medium block">Trámites en Curso</span>
            <span className="text-2xl font-bold text-slate-800">
              {loading ? '...' : kpis?.totalTramitesEnCurso}
            </span>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
            <GraduationCap className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-medium block">Titulados Histórico</span>
            <span className="text-2xl font-bold text-slate-800">
              {loading ? '...' : kpis?.totalTituladosHistorico}
            </span>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center font-bold">
            <AlertTriangle className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-medium block">Alertas Críticas (Rojo)</span>
            <span className="text-2xl font-bold text-rose-700">
              {loading ? '...' : kpis?.alertasRojasPendientes}
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Bandeja de Alertas Predictivas y Operativas */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-sm p-5 space-y-4">
          <div className="flex justify-between items-center border-b pb-3">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-amber-500" />
              <h2 className="text-base font-bold text-slate-800">
                Centro de Alertas y Semáforos Predictivos
              </h2>
            </div>
            <span className="text-xs text-slate-400 font-medium">Soberanía Humana Obligatoria</span>
          </div>

          <div className="space-y-3">
            {alertas.length === 0 ? (
              <div className="text-center py-10 text-slate-400 text-xs">
                No hay alertas registradas en el sistema.
              </div>
            ) : (
              alertas.map((a) => (
                <div
                  key={a.id}
                  className="p-4 rounded-xl border border-slate-200 bg-slate-50 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span
                        className={`px-2 py-0.5 rounded text-[10px] font-extrabold uppercase border ${getSemaforoBadge(
                          a.nivelCriticidad
                        )}`}
                      >
                        {a.nivelCriticidad}
                      </span>
                      <span className="font-bold text-xs text-slate-800">{a.titulo}</span>
                    </div>
                    <p className="text-xs text-slate-600 leading-relaxed">{a.mensajeDetalle}</p>
                    <div className="text-[10px] text-slate-400">
                      Fecha: {new Date(a.fechaGeneracion).toLocaleString()} — Estado: <strong>{a.estado}</strong>
                    </div>
                  </div>

                  {a.estado === 'PENDIENTE' && (
                    <button
                      onClick={() => setAlertaSeleccionada(a)}
                      className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-semibold whitespace-nowrap shadow-sm"
                    >
                      Atender Alerta
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
        </div>

        {/* Panel Lateral: Seguimiento POA y Titulación */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 space-y-5">
          <h2 className="text-base font-bold text-slate-800 border-b pb-3 flex items-center gap-2">
            <DollarSign className="w-5 h-5 text-emerald-600" />
            Control del POA Institucional
          </h2>

          <div className="space-y-3">
            <div className="flex justify-between items-center text-xs">
              <span className="text-slate-500">Presupuesto Asignado:</span>
              <strong className="text-slate-800 font-mono">
                BOB {kpis?.presupuestoPoaTotal?.toLocaleString()}
              </strong>
            </div>

            <div>
              <div className="flex justify-between items-center text-xs mb-1">
                <span className="text-slate-500">Avance Físico / Financiero:</span>
                <strong className="text-emerald-700">{kpis?.porcentajeAvancePoaPromedio}%</strong>
              </div>
              <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                <div
                  className="bg-emerald-600 h-2.5 rounded-full transition-all"
                  style={{ width: `${kpis?.porcentajeAvancePoaPromedio || 0}%` }}
                />
              </div>
            </div>
          </div>

          <div className="pt-3 border-t">
            <h3 className="text-xs font-bold text-slate-700 uppercase mb-3 flex items-center gap-1.5">
              <TrendingUp className="w-4 h-4 text-indigo-600" />
              Titulados por Modalidad
            </h3>
            <div className="space-y-2 text-xs">
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Tesis de Grado:</span>
                <strong className="text-slate-800 font-mono">{kpis?.tituladosPorModalidad?.TESIS || 1}</strong>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Proyecto de Grado:</span>
                <strong className="text-slate-800 font-mono">
                  {kpis?.tituladosPorModalidad?.PROYECTO_GRADO || 0}
                </strong>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Graduación por Excelencia:</span>
                <strong className="text-slate-800 font-mono">
                  {kpis?.tituladosPorModalidad?.EXCELENCIA || 0}
                </strong>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modal para Atender Alerta con Resolución de Autoridad */}
      {alertaSeleccionada && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-lg w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="font-bold text-slate-800 text-sm">Registrar Resolución Directiva (CU-30)</h3>
              <button
                onClick={() => setAlertaSeleccionada(null)}
                className="text-slate-400 hover:text-slate-600 text-xl font-bold"
              >
                &times;
              </button>
            </div>

            <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-900">
              <strong>Alerta:</strong> {alertaSeleccionada.titulo}
              <p className="mt-1 text-slate-600">{alertaSeleccionada.mensajeDetalle}</p>
            </div>

            <form onSubmit={handleAtenderAlerta} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Acción Administrativa Adoptada *
                </label>
                <textarea
                  required
                  rows={3}
                  value={resolucion}
                  onChange={(e) => setResolucion(e.target.value)}
                  placeholder="Ej. Resolución Decanal N° 20/2026: Se aprueba la apertura de grupo adicional..."
                  className="w-full border border-slate-300 rounded-xl p-3 text-xs focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t">
                <button
                  type="button"
                  onClick={() => setAlertaSeleccionada(null)}
                  className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs text-slate-700"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={procesandoAlerta}
                  className="px-4 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-semibold disabled:opacity-50"
                >
                  {procesandoAlerta ? 'Registrando...' : 'Confirmar Resolución'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

