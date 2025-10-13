package com.demo.demo.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadInterface {
    String uploadFile(MultipartFile file, String entityName, String fileType, Long entityId) throws IOException;
    String uploadTempFile(MultipartFile file, String entityName, String fileType) throws IOException;
    boolean renameTempFile(String tempPath, Long newEntityId, String entityName, String fileType);
    boolean deleteFile(String filePath);
}
