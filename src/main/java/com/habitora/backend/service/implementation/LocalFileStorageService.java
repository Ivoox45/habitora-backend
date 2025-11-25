package com.habitora.backend.service.implementation;

import com.habitora.backend.service.interfaces.IFileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements IFileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public LocalFileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento de archivos", e);
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
