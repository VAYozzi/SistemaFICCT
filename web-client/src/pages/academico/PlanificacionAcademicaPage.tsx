import React, { useState, useEffect } from 'react';
import { Calendar, AlertTriangle, BarChart3 } from 'lucide-react';
import { apiClient } from '../../services/apiClient';

interface PlanEstudio {
  id: number;
  codigoPlan: string;
  nombre: string;
  anioVigencia: number;
}

interface Materia {
  id: number;
  sigla: string;
  nombre: string;
  semestreNivel: number;
  creditos: number;
}

interface Periodo {
  id: number;
  codigo: string;
  estado: string;
}


interface PrediccionResult {
  materiaSigla: string;
  probabilidadSobredemanda: number;
  nivelAlerta: 'ROJO' | 'AMARILLO' | 'VERDE';
  cuposOfertados: number;
  demandaProyectada: number;
  deficitEstimado: number;
  esDatosPrueba: boolean;
  avisoInstitucional: string;
}

export const PlanificacionAcademicaPage: React.FC = () => {
  const [planes, setPlanes] = useState<PlanEstudio[]>([]);
  const [planSeleccionado, setPlanSeleccionado] = useState<number | null>(null);
  const [materias, setMaterias] = useState<Materia[]>([]);
  const [periodos, setPeriodos] = useState<Periodo[]>([]);
  const [periodoSeleccionado, setPeriodoSeleccionado] = useState<number | null>(null);

  // Estados de predicción ML
  const [prediccion, setPrediccion] = useState<PrediccionResult | null>(null);
  const [analizando, setAnalizando] = useState(false);

  const cargarDatosIniciales = async () => {
    try {
      const [resPlanes, resPeriodos] = await Promise.all([
        apiClient.get('/academico/planes'),
        apiClient.get('/academico/periodos'),
      ]);
      const pData = resPlanes.data.data || [];
      const perData = resPeriodos.data.data || [];
      setPlanes(pData);
      setPeriodos(perData);

      if (pData.length > 0) {
        setPlanSeleccionado(pData[0].id);
      }
      if (perData.length > 0) {
        setPeriodoSeleccionado(perData[0].id);
      }
    } catch (err: any) {
      console.error(err);
    }
  };

  useEffect(() => {
    cargarDatosIniciales();
  }, []);

  useEffect(() => {
    if (planSeleccionado) {
      apiClient
        .get(`/academico/planes/${planSeleccionado}/materias`)
        .then((res) => setMaterias(res.data.data || []))
        .catch(console.error);
    }
  }, [planSeleccionado]);

  const handleEjecutarPrediccion = async (materia: Materia) => {
    if (!periodoSeleccionado) return;
    setAnalizando(true);
    setPrediccion(null);
    try {
      const res = await apiClient.get(
        `/academico/predicciones/materias/${materia.id}/periodos/${periodoSeleccionado}`
      );
      setPrediccion(res.data.data);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error al ejecutar modelo predictivo.');
    } finally {
      setAnalizando(false);
    }
  };

  const getSemaforoClass = (nivel?: string) => {
    switch (nivel) {
      case 'ROJO':
        return 'bg-rose-50 border-rose-300 text-rose-800';
      case 'AMARILLO':
        return 'bg-amber-50 border-amber-300 text-amber-800';
      case 'VERDE':
        return 'bg-emerald-50 border-emerald-300 text-emerald-800';
      default:
        return 'bg-slate-50 border-slate-300 text-slate-800';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Planificación Académica y Cupos</h1>
          <p className="text-sm text-slate-500">
            Mallas curriculares, periodos semestrales y predicción supervisada de sobredemanda mediante Random Forest (CU-13, CU-14, CA-04).
          </p>
        </div>

        {/* Selector de Periodo */}
        <div className="flex items-center gap-2 bg-white px-3 py-1.5 rounded-xl border border-slate-200 shadow-sm">
          <Calendar className="w-4 h-4 text-blue-600" />
          <span className="text-xs font-semibold text-slate-600">Semestre:</span>
          <select
            value={periodoSeleccionado || ''}
            onChange={(e) => setPeriodoSeleccionado(Number(e.target.value))}
            className="text-xs font-bold text-slate-800 bg-transparent focus:outline-none"
          >
            {periodos.map((p) => (
              <option key={p.id} value={p.id}>
                {p.codigo} ({p.estado})
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Columna Izquierda: Malla Curricular y Asignaturas */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-4">
            <div className="flex justify-between items-center mb-4">
              <div className="flex items-center gap-2">
                <BarChart3 className="w-5 h-5 text-indigo-600" />
                <h2 className="text-base font-bold text-slate-800">Malla Curricular Activa</h2>
              </div>
              <select
                value={planSeleccionado || ''}
                onChange={(e) => setPlanSeleccionado(Number(e.target.value))}
                className="border border-slate-300 rounded-lg px-2.5 py-1 text-xs"
              >
                {planes.map((pl) => (
                  <option key={pl.id} value={pl.id}>
                    {pl.codigoPlan} — {pl.nombre}
                  </option>
                ))}
              </select>
            </div>

            <div className="overflow-hidden border border-slate-200 rounded-xl">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="bg-slate-50 text-slate-600 text-xs font-semibold uppercase border-b border-slate-200">
                    <th className="py-2.5 px-3">Sigla</th>
                    <th className="py-2.5 px-3">Asignatura</th>
                    <th className="py-2.5 px-3">Semestre</th>
                    <th className="py-2.5 px-3">Créditos</th>
                    <th className="py-2.5 px-3 text-right">Acción Analítica</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {materias.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="py-6 text-center text-slate-400 text-xs">
                        No hay asignaturas registradas en este plan.
                      </td>
                    </tr>
                  ) : (
                    materias.map((m) => (
                      <tr key={m.id} className="hover:bg-slate-50">
                        <td className="py-2.5 px-3 font-mono font-bold text-blue-600">{m.sigla}</td>
                        <td className="py-2.5 px-3 font-medium text-slate-800">{m.nombre}</td>
                        <td className="py-2.5 px-3 text-slate-600">Nivel {m.semestreNivel}</td>
                        <td className="py-2.5 px-3 text-slate-500">{m.creditos} CR</td>
                        <td className="py-2.5 px-3 text-right">
                          <button
                            onClick={() => handleEjecutarPrediccion(m)}
                            className="px-2.5 py-1 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-semibold rounded-lg transition"
                          >
                            Predecir Cupos (ML)
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Columna Derecha: Panel de Predicción Supervisada */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 space-y-4">
          <div className="flex items-center gap-2 border-b pb-3">
            <AlertTriangle className="w-5 h-5 text-amber-500" />
            <h2 className="text-base font-bold text-slate-800">Inferencia de Demanda</h2>
          </div>

          {analizando ? (
            <div className="text-center py-16 text-slate-400 text-xs animate-pulse">
              Ejecutando ensamble Random Forest en ai-worker-api...
            </div>
          ) : prediccion ? (
            <div className="space-y-4">
              {/* Tarjeta Semafórica */}
              <div className={`p-4 rounded-xl border-2 ${getSemaforoClass(prediccion.nivelAlerta)} space-y-2`}>
                <div className="flex justify-between items-center">
                  <span className="font-mono font-bold text-lg">{prediccion.materiaSigla}</span>
                  <span className="px-3 py-1 rounded-full text-xs font-extrabold uppercase tracking-wider bg-white/70 shadow-sm">
                    Alerta {prediccion.nivelAlerta}
                  </span>
                </div>
                <div className="text-xs">
                  Probabilidad de Sobredemanda:{' '}
                  <strong>{(prediccion.probabilidadSobredemanda * 100).toFixed(1)}%</strong>
                </div>
              </div>

              {/* Métricas Proyectadas */}
              <div className="grid grid-cols-2 gap-3 text-xs">
                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
                  <span className="text-slate-400 block">Cupos Planificados:</span>
                  <span className="text-base font-bold text-slate-800">{prediccion.cuposOfertados}</span>
                </div>
                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
                  <span className="text-slate-400 block">Demanda Estimada:</span>
                  <span className="text-base font-bold text-slate-800">{prediccion.demandaProyectada}</span>
                </div>
              </div>

              {prediccion.deficitEstimado > 0 ? (
                <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-xl text-xs font-medium">
                  Déficit proyectado:{' '}
                  <strong>{prediccion.deficitEstimado} estudiantes sin cupo</strong>. Se sugiere apertura de un paralelo adicional.
                </div>
              ) : (
                <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-xs font-medium">
                  Cupos suficientes para la demanda estimada del periodo.
                </div>
              )}

              {/* Aviso Institucional Obligatorio */}
              <div className="p-3 bg-amber-50/60 border border-amber-200 rounded-xl text-[11px] text-amber-900 leading-relaxed">
                <strong>Aviso Metodológico:</strong> {prediccion.avisoInstitucional}
              </div>
            </div>
          ) : (
            <div className="text-center py-16 text-slate-400 text-xs">
              Seleccione una asignatura de la malla para evaluar el riesgo de sobredemanda y calcular el déficit de cupos con IA.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
