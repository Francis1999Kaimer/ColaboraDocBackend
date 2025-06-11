package com.example.project.services;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DropboxService {

    @Value("${dropbox.access.token}")
    private String accessToken;

    @Value("${dropbox.base.path:Apps/ColaboraDoc}")
    private String basePath;

    @Value("${dropbox.documents.folder:documents}")
    private String documentsFolder;

    @Value("${dropbox.versions.folder:versions}")
    private String versionsFolder;    

    private DbxClientV2 getClient() throws DbxException {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalStateException("Dropbox access token is not configured");
        }
        
        DbxRequestConfig config = DbxRequestConfig.newBuilder("colaboradoc/1.0").build();
        DbxClientV2 client = new DbxClientV2(config, accessToken);
        
        
        try {
            client.users().getCurrentAccount();
        } catch (DbxException e) {
            throw new DbxException("Invalid Dropbox access token: " + e.getMessage());
        }
        
        return client;
    }


    

    public void deleteFile(String fileId) throws DbxException {
        DbxClientV2 client = getClient();
        client.files().deleteV2(fileId);
    }    

    public String generateDocumentFolderPath(Integer projectId, Integer folderId, Integer documentId, String documentName) {
        
        String cleanDocumentName = documentName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("%s/project_%d/folder_%d/document_%d_%s", 
                           basePath, projectId, folderId, documentId, cleanDocumentName);
    }

    

    public String generateVersionFileName(Integer versionNumber, String originalFileName, String suffix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String cleanFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        if (suffix != null && !suffix.isEmpty()) {
            return String.format("v%d_%s_%s_%s", versionNumber, timestamp, suffix, cleanFileName);
        } else {
            return String.format("v%d_%s_%s", versionNumber, timestamp, cleanFileName);
        }
    }

    

    public String uploadFile(byte[] fileContent, String fullPath) throws DbxException {
        DbxClientV2 client = getClient();
        
        if (!fullPath.startsWith("/")) {
            fullPath = "/" + fullPath;
        }

        try (java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(fileContent)) {
            FileMetadata metadata = client.files().uploadBuilder(fullPath)
                    .withMode(WriteMode.ADD)
                    .uploadAndFinish(in);

            return metadata.getId();
        } catch (Exception e) {
            throw new DbxException("Failed to upload file: " + e.getMessage());
        }
    }

    

    public byte[] downloadFile(String fileId) throws DbxException {
        DbxClientV2 client = getClient();
        
        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            client.files().download(fileId).download(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new DbxException("Failed to download file: " + e.getMessage());
        }
    }

    

    public String getTemporaryDownloadLink(String fileId) throws DbxException {
        DbxClientV2 client = getClient();
        return client.files().getTemporaryLink(fileId).getLink();
    }

    

    public DropboxUploadResult uploadFile(MultipartFile file, String folderPath) throws IOException, DbxException {
        DbxClientV2 client = getClient();
        
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFileName = file.getOriginalFilename();
        String fileName = timestamp + "_" + originalFileName;
        
        
        String dropboxPath = folderPath + "/" + fileName;
        if (!dropboxPath.startsWith("/")) {
            dropboxPath = "/" + dropboxPath;
        }

        try (InputStream in = file.getInputStream()) {
            FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .uploadAndFinish(in);

            return new DropboxUploadResult(
                    metadata.getId(),
                    metadata.getPathLower(),
                    metadata.getSize(),
                    originalFileName,
                    file.getContentType()
            );
        }
    }

    

    public DropboxUploadResult uploadVersion(MultipartFile file, Integer projectId, Integer folderId, 
                                           Integer documentId, String documentName, Integer versionNumber) 
                                           throws IOException, DbxException {
        
        String folderPath = generateDocumentFolderPath(projectId, folderId, documentId, documentName);
        String fileName = generateVersionFileName(versionNumber, file.getOriginalFilename(), "original");
        String fullPath = folderPath + "/" + fileName;
        
        if (!fullPath.startsWith("/")) {
            fullPath = "/" + fullPath;
        }

        DbxClientV2 client = getClient();

        try (InputStream in = file.getInputStream()) {
            FileMetadata metadata = client.files().uploadBuilder(fullPath)
                    .withMode(WriteMode.ADD)
                    .uploadAndFinish(in);

            return new DropboxUploadResult(
                    metadata.getId(),
                    metadata.getPathLower(),
                    metadata.getSize(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );
        }
    }

    
    public static class DropboxUploadResult {
        private final String fileId;
        private final String filePath;
        private final long fileSize;
        private final String originalFileName;
        private final String mimeType;

        public DropboxUploadResult(String fileId, String filePath, long fileSize, String originalFileName, String mimeType) {
            this.fileId = fileId;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.originalFileName = originalFileName;
            this.mimeType = mimeType;
        }

        
        public String getFileId() { return fileId; }
        public String getFilePath() { return filePath; }
        public long getFileSize() { return fileSize; }
        public String getOriginalFileName() { return originalFileName; }
        public String getMimeType() { return mimeType; }
    }
}
