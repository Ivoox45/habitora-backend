package com.habitora.backend.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface IFileStorageService {
    String saveFile(MultipartFile file, String subDirectory) throws IOException;

    byte[] loadFile(String filePath) throws IOException;

    void deleteFile(String filePath);
}
