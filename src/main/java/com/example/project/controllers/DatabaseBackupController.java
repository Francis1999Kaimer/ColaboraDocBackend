package com.example.project.controllers;

import com.example.project.annotation.RequiresPermission;
import com.example.project.enums.Permission;
import com.example.project.services.DatabaseBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/backup")
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class DatabaseBackupController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupController.class);

    @Autowired
    private DatabaseBackupService backupService;

    

    @PostMapping("/create")
  
    public ResponseEntity<?> createBackup(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            logger.info("Iniciando creación de backup por usuario: {}", userDetails.getUsername());
            
            String backupFilePath = backupService.createDatabaseBackup();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Backup creado exitosamente");
            response.put("filePath", backupFilePath);
            response.put("timestamp", java.time.LocalDateTime.now());

            logger.info("Backup creado exitosamente por {}: {}", userDetails.getUsername(), backupFilePath);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al crear backup para usuario {}: {}", userDetails.getUsername(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al crear backup: " + e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    

    @GetMapping("/list")
    @RequiresPermission(Permission.DELETE_PROJECT) 
    public ResponseEntity<?> listBackups(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<DatabaseBackupService.BackupInfo> backups = backupService.getAvailableBackups();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("backups", backups);
            response.put("count", backups.size());
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al obtener lista de backups para usuario {}: {}", 
                        userDetails.getUsername(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener lista de backups: " + e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    

    @GetMapping("/download/{fileName}")
    @RequiresPermission(Permission.DELETE_PROJECT) 
    public ResponseEntity<?> downloadBackup(
            @PathVariable String fileName,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            
            if (!fileName.matches("backup_docdb_\\d{8}_\\d{6}\\.sql")) {
                logger.warn("Intento de descarga de archivo con nombre inválido: {} por {}", 
                           fileName, userDetails.getUsername());
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Nombre de archivo inválido"));
            }

            byte[] backupContent = backupService.getBackupContent(fileName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(backupContent.length);

            logger.info("Backup descargado por {}: {}", userDetails.getUsername(), fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(backupContent));

        } catch (Exception e) {
            logger.error("Error al descargar backup {} para usuario {}: {}", 
                        fileName, userDetails.getUsername(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al descargar backup: " + e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    

    @DeleteMapping("/delete/{fileName}")
    @RequiresPermission(Permission.DELETE_PROJECT) 
    public ResponseEntity<?> deleteBackup(
            @PathVariable String fileName,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            
            if (!fileName.matches("backup_docdb_\\d{8}_\\d{6}\\.sql")) {
                logger.warn("Intento de eliminación de archivo con nombre inválido: {} por {}", 
                           fileName, userDetails.getUsername());
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Nombre de archivo inválido"));
            }

            boolean deleted = backupService.deleteBackup(fileName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Backup eliminado exitosamente" : "No se pudo eliminar el backup");
            response.put("timestamp", java.time.LocalDateTime.now());

            if (deleted) {
                logger.info("Backup eliminado por {}: {}", userDetails.getUsername(), fileName);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            logger.error("Error al eliminar backup {} para usuario {}: {}", 
                        fileName, userDetails.getUsername(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar backup: " + e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    

    @GetMapping("/status")
    @RequiresPermission(Permission.DELETE_PROJECT) 
    public ResponseEntity<?> getBackupStatus(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<DatabaseBackupService.BackupInfo> backups = backupService.getAvailableBackups();
            
            Map<String, Object> status = new HashMap<>();
            status.put("success", true);
            status.put("serviceStatus", "ACTIVE");
            status.put("totalBackups", backups.size());
            status.put("lastBackup", backups.isEmpty() ? null : backups.get(0));
            status.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            logger.error("Error al obtener estado de backup para usuario {}: {}", 
                        userDetails.getUsername(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("serviceStatus", "ERROR");
            errorResponse.put("message", "Error al obtener estado: " + e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
