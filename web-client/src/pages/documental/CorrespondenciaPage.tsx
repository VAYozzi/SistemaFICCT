import React, { useState, useEffect } from 'react';
import { apiClient } from '../../services/apiClient';

interface Correspondencia {
  id: number;
  codigoCite: string;
  tipo: string;
  remitenteInstitucion: string;
  remitentePersona: string;
  destinatario: string;
  referencia: string;
  observaciones?: string;
  estado: string;
  documentoAdjuntoId?: number;
  creadoEn: string;
}

export const CorrespondenciaPage: React.FC = () => {
  const [correspondencias, setCorrespondencias] = useState<Correspondencia[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtroTipo, setFiltroTipo] = useState<string>('');
  const [modalAbierto, setModalAbierto] = useState(false);
  const [subiendo, setSubiendo] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Formulario nuevo registro
  const [codigoCite, setCodigoCite] = useState('');
  const [tipo, setTipo] = useState('ENTRANTE');
  const [remitenteInstitucion, setRemitenteInstitucion] = useState('');
  const [remitentePersona, setRemitentePersona] = useState('');
  const [destinatario, setDestinatario] = useState('Dirección de Sistemas');
  const [referencia, setReferencia] = useState('');
  const [observaciones, setObservaciones] = useState('');
  const [archivo, setArchivo] = useState<File | null>(null);

  const cargarCorrespondencias = async () => {
    try {
      setLoading(true);
      const params = filtroTipo ? { tipo: filtroTipo } : {};
      const res = await apiClient.get('/correspondencia', { params });
      if (res.data?.data?.content) {
        setCorrespondencias(res.data.data.content);
      }
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarCorrespondencias();
  }, [filtroTipo]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setSubiendo(true);

    try {
      let docId: number | undefined = undefined;

      // Si adjunta archivo, primero subir a /documentos/upload
      if (archivo) {
        const formData = new FormData();
        formData.append('file', archivo);
        const uploadRes = await apiClient.post('/documentos/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
        docId = uploadRes.data?.data?.id;
      }

      // Crear correspondencia
      await apiClient.post('/correspondencia', {
        codigoCite,
        tipo,
        remitenteInstitucion,
        remitentePersona,
        destinatario,
        referencia,
        observaciones,
        documentoAdjuntoId: docId,
      });

      // Reset y recarga
      setModalAbierto(false);
      setCodigoCite('');
      setRemitenteInstitucion('');
      setRemitentePersona('');
      setReferencia('');
      setObservaciones('');
      setArchivo(null);
      await cargarCorrespondencias();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Error al registrar la correspondencia.');
    } finally {
      setSubiendo(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Bandeja de Correspondencia</h1>
          <p className="text-sm text-slate-500">
            Registro, digitalización con integridad SHA-256 y seguimiento de correspondencia oficial (CU-05).
          </p>
        </div>
        <button
          onClick={() => setModalAbierto(true)}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium shadow-sm transition"
        >
          + Nueva Correspondencia
        </button>
      </div>

      {/* Filtros */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4">
        <label className="text-sm font-medium text-slate-700">Filtrar por tipo:</label>
        <select
          value={filtroTipo}
          onChange={(e) => setFiltroTipo(e.target.value)}
          className="border border-slate-300 rounded-lg px-3 py-1.5 text-sm focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Todos los tipos</option>
          <option value="ENTRANTE">Entrante</option>
          <option value="SALIENTE">Saliente</option>
          <option value="INTERNO">Interno</option>
        </select>
      </div>

      {/* Tabla */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 text-slate-600 text-xs font-semibold uppercase tracking-wider border-b border-slate-200">
              <th className="py-3 px-4">CITE</th>
              <th className="py-3 px-4">Tipo</th>
              <th className="py-3 px-4">Remitente</th>
              <th className="py-3 px-4">Destinatario</th>
              <th className="py-3 px-4">Referencia</th>
              <th className="py-3 px-4">Estado</th>
              <th className="py-3 px-4">Fecha</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 text-sm text-slate-700">
            {loading ? (
              <tr>
                <td colSpan={7} className="text-center py-8 text-slate-400">
                  Cargando correspondencias...
                </td>
              </tr>
            ) : correspondencias.length === 0 ? (
              <tr>
                <td colSpan={7} className="text-center py-8 text-slate-400">
                  No se encontraron registros de correspondencia.
                </td>
              </tr>
            ) : (
              correspondencias.map((item) => (
                <tr key={item.id} className="hover:bg-slate-50 transition">
                  <td className="py-3 px-4 font-mono font-medium text-blue-600">{item.codigoCite}</td>
                  <td className="py-3 px-4">
                    <span
                      className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                        item.tipo === 'ENTRANTE'
                          ? 'bg-emerald-100 text-emerald-800'
                          : item.tipo === 'SALIENTE'
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-purple-100 text-purple-800'
                      }`}
                    >
                      {item.tipo}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    <div className="font-medium text-slate-900">{item.remitentePersona}</div>
                    <div className="text-xs text-slate-400">{item.remitenteInstitucion}</div>
                  </td>
                  <td className="py-3 px-4 text-slate-600">{item.destinatario}</td>
                  <td className="py-3 px-4 max-w-xs truncate" title={item.referencia}>
                    {item.referencia}
                  </td>
                  <td className="py-3 px-4">
                    <span className="bg-slate-100 text-slate-700 px-2.5 py-0.5 rounded-md text-xs font-medium">
                      {item.estado}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-xs text-slate-500 whitespace-nowrap">
                    {new Date(item.creadoEn).toLocaleDateString()}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Nueva Correspondencia */}
      {modalAbierto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-xl w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h2 className="text-lg font-bold text-slate-800">Registrar Correspondencia</h2>
              <button
                onClick={() => setModalAbierto(false)}
                className="text-slate-400 hover:text-slate-600 text-xl font-bold"
              >
                &times;
              </button>
            </div>

            {errorMsg && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                    Código CITE *
                  </label>
                  <input
                    type="text"
                    required
                    value={codigoCite}
                    onChange={(e) => setCodigoCite(e.target.value)}
                    placeholder="Ej. CITE-FICCT-2026-002"
                    className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                    Tipo de Trámite *
                  </label>
                  <select
                    value={tipo}
                    onChange={(e) => setTipo(e.target.value)}
                    className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="ENTRANTE">Entrante</option>
                    <option value="SALIENTE">Saliente</option>
                    <option value="INTERNO">Interno</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                    Institución Remitente *
                  </label>
                  <input
                    type="text"
                    required
                    value={remitenteInstitucion}
                    onChange={(e) => setRemitenteInstitucion(e.target.value)}
                    placeholder="Ej. Vicerrectorado / DTI"
                    className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                    Nombre Remitente *
                  </label>
                  <input
                    type="text"
                    required
                    value={remitentePersona}
                    onChange={(e) => setRemitentePersona(e.target.value)}
                    placeholder="Ej. Dr. Reinerio Vargas"
                    className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Destinatario *
                </label>
                <input
                  type="text"
                  required
                  value={destinatario}
                  onChange={(e) => setDestinatario(e.target.value)}
                  className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Referencia / Asunto *
                </label>
                <textarea
                  required
                  rows={2}
                  value={referencia}
                  onChange={(e) => setReferencia(e.target.value)}
                  placeholder="Síntesis del oficio o documento..."
                  className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                  Documento Adjunto (PDF o Imagen)
                </label>
                <input
                  type="file"
                  accept=".pdf,image/png,image/jpeg"
                  onChange={(e) => setArchivo(e.target.files?.[0] || null)}
                  className="w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setModalAbierto(false)}
                  className="px-4 py-2 border border-slate-300 rounded-lg text-sm text-slate-700 hover:bg-slate-50"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={subiendo}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium disabled:opacity-50"
                >
                  {subiendo ? 'Registrando...' : 'Guardar Correspondencia'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

