package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service;

import java.io.InputStream;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String almacenarArchivo(MultipartFile file, String subcarpeta, String nombreGuardado);
    String calcularSha256(MultipartFile file);
    InputStream obtenerArchivoComoStream(String rutaRelativa);
    Path obtenerRutaAbsoluta(String rutaRelativa);
    void eliminarArchivo(String rutaRelativa);
}

