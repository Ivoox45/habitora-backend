package com.habitora.backend.service.implementation;

import com.habitora.backend.service.interfaces.IFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements IFileStorageService {

    private final Path rootLocation;

    public LocalFileStorageService(@Value("${file.storage.upload-dir:uploads}") String uploadDir) {
        // Si es una ruta relativa, la resolvemos desde el directorio del proyecto
        Path uploadPath = Paths.get(uploadDir);
        
        if (!uploadPath.isAbsolute()) {
            // Obtener el directorio del proyecto (donde está el JAR o las clases compiladas)
            String projectDir = System.getProperty("user.dir");
            uploadPath = Paths.get(projectDir, uploadDir);
        }
        
        this.rootLocation = uploadPath.toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(rootLocation);
            System.out.println("📁 File storage initialized at: " + rootLocation.toString());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento de archivos en: " + rootLocation, e);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String subDirectory) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Fallo al guardar archivo vacío.");
        }

        Path destinationDir = rootLocation.resolve(subDirectory);
        if (!Files.exists(destinationDir)) {
            Files.createDirectories(destinationDir);
        }

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path destinationFile = destinationDir.resolve(filename);

        Files.copy(file.getInputStream(), destinationFile);

        return subDirectory + "/" + filename;
    }

    @Override
    public byte[] loadFile(String filePath) throws IOException {
        Path file = rootLocation.resolve(filePath);
        if (Files.exists(file) && Files.isReadable(file)) {
            return Files.readAllBytes(file);
        } else {
            throw new IOException("No se pudo leer el archivo: " + filePath);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null)
            return;
        Path file = rootLocation.resolve(filePath);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Log warning but don't throw
            System.err.println("No se pudo eliminar el archivo: " + filePath);
        }
    }
}
