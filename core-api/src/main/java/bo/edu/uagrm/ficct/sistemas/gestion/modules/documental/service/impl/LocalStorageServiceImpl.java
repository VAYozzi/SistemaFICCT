package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.impl;

import bo.edu.uagrm.ficct.sistemas.gestion.common.exception.BusinessRuleException;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.StorageService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageServiceImpl implements StorageService {

    private final Path rootLocation;

    public LocalStorageServiceImpl(@Value("${storage.base-path:C:/Users/vajho/Desktop/SistemaFICCT/storage}") String basePath) {
        this.rootLocation = Paths.get(basePath);
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de almacenamiento", e);
        }
    }

    @Override
    public String almacenarArchivo(MultipartFile file, String subcarpeta, String nombreGuardado) {
        try {
            if (file.isEmpty()) {
                throw new BusinessRuleException("No se puede almacenar un archivo vacío", HttpStatus.BAD_REQUEST);
            }
            Path destinationDir = this.rootLocation.resolve(subcarpeta);
            Files.createDirectories(destinationDir);

            Path destinationFile = destinationDir.resolve(nombreGuardado).normalize().toAbsolutePath();
            if (!destinationFile.getParent().startsWith(this.rootLocation.toAbsolutePath())) {
                throw new BusinessRuleException("Ruta de archivo no permitida (path traversal)", HttpStatus.FORBIDDEN);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return subcarpeta + "/" + nombreGuardado;
        } catch (IOException e) {
            throw new RuntimeException("Fallo al almacenar el archivo", e);
        }
    }

    @Override
    public String calcularSha256(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular el hash SHA-256 del archivo", e);
        }
    }

    @Override
    public InputStream obtenerArchivoComoStream(String rutaRelativa) {
        try {
            Path file = this.rootLocation.resolve(rutaRelativa);
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new BusinessRuleException("Archivo no encontrado en almacenamiento", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Path obtenerRutaAbsoluta(String rutaRelativa) {
        return this.rootLocation.resolve(rutaRelativa).normalize().toAbsolutePath();
    }

    @Override
    public void eliminarArchivo(String rutaRelativa) {
        try {
            Path file = this.rootLocation.resolve(rutaRelativa);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Ignorar o loguear
        }
    }
}

