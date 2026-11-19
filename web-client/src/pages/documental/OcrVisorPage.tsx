import React, { useState } from 'react';
import { ZoomIn, ZoomOut, RotateCw, CheckCircle, Search, FileText } from 'lucide-react';
import { apiClient } from '../../services/apiClient';

interface PaginaOcr {
  id: number;
  numeroPagina: number;
  textoExtraidoBruto: string;
  textoCorregido: string | null;
  confianzaPagina: number;
}

interface DocumentoOcr {
  documentoId: number;
  nombreOriginal: string;
  rutaAlmacenamiento: string;
  tipoMime: string;
  estadoOcr: string;
  confianzaPromedio: number;
  paginas: PaginaOcr[];
}

interface ResultadoBusqueda {
  paginaId: number;
  documentoId: number;
  numeroPagina: number;
  confianzaPagina: number;
  fragmentoResaltado: string;
  nombreOriginal: string;
  rutaAlmacenamiento: string;
  tipoMime: string;
}

export const OcrVisorPage: React.FC = () => {
  // Estado para el visor
  const [docIdInput, setDocIdInput] = useState('2');
  const [docData, setDocData] = useState<DocumentoOcr | null>(null);
  const [paginaActual, setPaginaActual] = useState(1);
  const [textoEditable, setTextoEditable] = useState('');
  const [zoom, setZoom] = useState(100);
  const [rotacion, setRotacion] = useState(0);
  const [cargando, setCargando] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [mensajeExito, setMensajeExito] = useState<string | null>(null);

  // Estado para búsqueda de texto completo
  const [terminoBusqueda, setTerminoBusqueda] = useState('AUTONOMA');
  const [resultadosBusqueda, setResultadosBusqueda] = useState<ResultadoBusqueda[]>([]);
  const [buscando, setBuscando] = useState(false);

  const cargarDocumento = async () => {
    if (!docIdInput) return;
    setCargando(true);
    setMensajeExito(null);
    try {
      const res = await apiClient.get(`/ocr/${docIdInput}`);
      const data: DocumentoOcr = res.data.data;
      setDocData(data);
      if (data.paginas && data.paginas.length > 0) {
        setPaginaActual(1);
        setTextoEditable(data.paginas[0].textoCorregido || data.paginas[0].textoExtraidoBruto || '');
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error al cargar documento OCR.');
    } finally {
      setCargando(false);
    }
  };

  const procesarOcrEnServidor = async () => {
    if (!docIdInput) return;
    setCargando(true);
    setMensajeExito(null);
    try {
      const res = await apiClient.post(`/ocr/${docIdInput}/procesar`);
      const data: DocumentoOcr = res.data.data;
      setDocData(data);
      if (data.paginas && data.paginas.length > 0) {
        setPaginaActual(1);
        setTextoEditable(data.paginas[0].textoCorregido || data.paginas[0].textoExtraidoBruto || '');
      }
      setMensajeExito('Extracción OCR completada con visión artificial.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error al procesar OCR.');
    } finally {
      setCargando(false);
    }
  };

  const handleValidarTexto = async () => {
    if (!docData) return;
    setGuardando(true);
    try {
      const payload = {
        documentoId: docData.documentoId,
        paginas: [
          {
            numeroPagina: paginaActual,
            textoCorregido: textoEditable,
          },
        ],
      };
      const res = await apiClient.put('/ocr/validar', payload);
      setDocData(res.data.data);
      setMensajeExito('¡Texto validado por soberanía humana! Indexado en PostgreSQL para búsqueda.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error al validar texto OCR.');
    } finally {
      setGuardando(false);
    }
  };

  const handleBuscar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!terminoBusqueda.trim()) return;
    setBuscando(true);
    try {
      const res = await apiClient.get('/ocr/busqueda', {
        params: { q: terminoBusqueda.trim() },
      });
      setResultadosBusqueda(res.data.data || []);
    } catch (err: any) {
      console.error(err);
    } finally {
      setBuscando(false);
    }
  };

  const paginaInfo = docData?.paginas.find((p) => p.numeroPagina === paginaActual);
  const confianza = paginaInfo?.confianzaPagina || 0;

  const getConfianzaBadge = (conf: number) => {
    if (conf >= 85) return 'bg-emerald-100 text-emerald-800 border-emerald-300';
    if (conf >= 65) return 'bg-amber-100 text-amber-800 border-amber-300';
    return 'bg-rose-100 text-rose-800 border-rose-300';
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Visor Dual OCR y Búsqueda Documental</h1>
          <p className="text-sm text-slate-500">
            Soberanía humana obligatoria: visión artificial con OpenCV + PyTesseract e indexación nativa tsvector (CU-21 a CU-24).
          </p>
        </div>

        {/* Carga por ID de Documento */}
        <div className="flex items-center gap-2">
          <input
            type="number"
            value={docIdInput}
            onChange={(e) => setDocIdInput(e.target.value)}
            className="w-20 border border-slate-300 rounded-lg px-2 py-1.5 text-sm"
            placeholder="ID"
          />
          <button
            onClick={cargarDocumento}
            disabled={cargando}
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-900 text-white rounded-lg text-sm font-medium transition"
          >
            Cargar
          </button>
          <button
            onClick={procesarOcrEnServidor}
            disabled={cargando}
            className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition"
          >
            Ejecutar OCR
          </button>
        </div>
      </div>

      {mensajeExito && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-sm flex items-center gap-2">
          <CheckCircle className="w-4 h-4 text-emerald-600" />
          {mensajeExito}
        </div>
      )}

      {/* Visor Comparativo Dual (50% Original / 50% Texto Humano) */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 bg-white border border-slate-200 rounded-2xl shadow-sm p-4">
        {/* Panel Izquierdo: Documento Original con Zoom y Rotación */}
        <div className="flex flex-col border border-slate-200 rounded-xl p-3 bg-slate-50 min-h-[460px]">
          <div className="flex justify-between items-center pb-2 border-b border-slate-200 text-xs font-semibold text-slate-600">
            <span>DOCUMENTO ORIGINAL</span>
            <div className="flex items-center gap-1">
              <button
                onClick={() => setZoom((z) => Math.max(50, z - 10))}
                className="p-1 hover:bg-white rounded border border-slate-200"
                title="Zoom Out"
              >
                <ZoomOut className="w-4 h-4" />
              </button>
              <span className="w-12 text-center">{zoom}%</span>
              <button
                onClick={() => setZoom((z) => Math.min(200, z + 10))}
                className="p-1 hover:bg-white rounded border border-slate-200"
                title="Zoom In"
              >
                <ZoomIn className="w-4 h-4" />
              </button>
              <button
                onClick={() => setRotacion((r) => (r + 90) % 360)}
                className="p-1 hover:bg-white rounded border border-slate-200 ml-2"
                title="Rotar 90°"
              >
                <RotateCw className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="flex-1 overflow-auto flex items-center justify-center p-4">
            {docData ? (
              <div
                style={{
                  transform: `scale(${zoom / 100}) rotate(${rotacion}deg)`,
                  transition: 'transform 0.2s ease',
                }}
                className="border shadow-md bg-white p-4 max-w-full text-center"
              >
                <div className="text-xs text-slate-400 mb-2">{docData.nombreOriginal}</div>
                <div className="p-8 border-2 border-dashed border-slate-300 rounded font-serif text-slate-700 bg-amber-50/20">
                  <p className="font-bold tracking-widest text-sm">FICCT — UAGRM</p>
                  <p className="text-xs mt-2">DIRECCIÓN DE CARRERA DE SISTEMAS</p>
                  <p className="text-[11px] mt-4 italic text-slate-500">
                    Archivo renderizado: {docData.rutaAlmacenamiento}
                  </p>
                </div>
              </div>
            ) : (
              <div className="text-center text-slate-400 text-sm">
                Seleccione un documento para visualizar el original escaneado.
              </div>
            )}
          </div>
        </div>

        {/* Panel Derecho: Editor Humano de Validación y Confianza */}
        <div className="flex flex-col border border-slate-200 rounded-xl p-3 bg-white min-h-[460px]">
          <div className="flex justify-between items-center pb-2 border-b border-slate-200">
            <span className="text-xs font-semibold text-slate-600 uppercase">
              Validación y Soberanía Humana
            </span>
            <div className="flex items-center gap-2">
              <span className="text-xs text-slate-500">Nivel de Confianza:</span>
              <span
                className={`px-2 py-0.5 rounded text-xs font-bold border ${getConfianzaBadge(
                  confianza
                )}`}
              >
                {confianza}%
              </span>
            </div>
          </div>

          <div className="flex-1 flex flex-col pt-3 space-y-3">
            <div className="flex justify-between items-center text-xs text-slate-500">
              <span>Estado: <strong className="text-slate-800">{docData?.estadoOcr || 'SIN DATOS'}</strong></span>
              <span>Página {paginaActual}</span>
            </div>

            <textarea
              value={textoEditable}
              onChange={(e) => setTextoEditable(e.target.value)}
              placeholder="El texto extraído aparecerá aquí para revisión y edición por parte del funcionario..."
              className="flex-1 w-full border border-slate-300 rounded-xl p-3 text-sm font-mono leading-relaxed focus:ring-2 focus:ring-blue-500 resize-none min-h-[260px]"
            />

            <button
              onClick={handleValidarTexto}
              disabled={guardando || !docData}
              className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-medium text-sm transition flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <CheckCircle className="w-4 h-4" />
              {guardando ? 'Guardando e Indexando...' : 'Confirmar y Validar Texto'}
            </button>
          </div>
        </div>
      </div>

      {/* Buscador de Texto Completo con tsvector y Resaltado */}
      <div className="bg-white border border-slate-200 rounded-2xl shadow-sm p-6 space-y-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <Search className="w-5 h-5 text-blue-600" />
            Búsqueda Documental de Texto Completo (`tsvector`)
          </h2>
          <p className="text-xs text-slate-500">
            Búsqueda lingüística en español con soporte de morfología y fragmentos resaltados.
          </p>
        </div>

        <form onSubmit={handleBuscar} className="flex gap-2">
          <input
            type="text"
            value={terminoBusqueda}
            onChange={(e) => setTerminoBusqueda(e.target.value)}
            placeholder="Buscar por palabra clave (ej. CONVALIDACION, HORARIO, DECISIÓN)..."
            className="flex-1 border border-slate-300 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            disabled={buscando}
            className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-medium transition"
          >
            {buscando ? 'Buscando...' : 'Buscar'}
          </button>
        </form>

        {/* Lista de Resultados */}
        <div className="space-y-3 pt-2">
          {resultadosBusqueda.length > 0 ? (
            resultadosBusqueda.map((item) => (
              <div
                key={item.paginaId}
                className="p-4 border border-slate-200 rounded-xl bg-slate-50 hover:bg-blue-50/40 transition"
              >
                <div className="flex justify-between items-center mb-1.5">
                  <div className="flex items-center gap-2">
                    <FileText className="w-4 h-4 text-blue-600" />
                    <span className="font-semibold text-sm text-slate-800">{item.nombreOriginal}</span>
                    <span className="text-xs text-slate-400">Página {item.numeroPagina}</span>
                  </div>
                  <span className="text-xs bg-slate-200 text-slate-700 px-2 py-0.5 rounded font-mono">
                    Confianza: {item.confianzaPagina}%
                  </span>
                </div>
                <div
                  className="text-xs text-slate-600 font-mono bg-white p-2.5 rounded-lg border border-slate-200"
                  dangerouslySetInnerHTML={{ __html: item.fragmentoResaltado }}
                />
              </div>
            ))
          ) : (
            <div className="text-center py-6 text-slate-400 text-xs">
              No se han realizado búsquedas o no hay coincidencias entre documentos validados.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
