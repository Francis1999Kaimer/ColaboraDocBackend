package com.example.project.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DatabaseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupService.class);

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${backup.directory:./backups}")
    private String backupDirectory;

    @Value("${backup.max.files:10}")
    private int maxBackupFiles;    

    @Transactional(readOnly = true)
    public String createDatabaseBackup() throws Exception {
        try {
            
            if (!isMysqldumpAvailable()) {
                throw new RuntimeException(getMysqldumpNotFoundMessage());
            }

            
            createBackupDirectory();            
            DatabaseInfo dbInfo = extractDatabaseInfo();
            logger.info("Información de BD - Host: {}, Puerto: {}, Base de datos: {}, Usuario: {}", 
                       dbInfo.getHost(), dbInfo.getPort(), dbInfo.getDatabaseName(), dbInfo.getUsername());            
            String backupFileName = generateBackupFileName();
            Path backupFilePath = Paths.get(backupDirectory, backupFileName);
            logger.info("Creando backup en: {}", backupFilePath.toAbsolutePath());

            
            executeBackupCommand(dbInfo, backupFilePath);

            
            cleanupOldBackups();

            logger.info("Backup de base de datos creado exitosamente: {}", backupFilePath);
            return backupFilePath.toString();

        } catch (Exception e) {
            logger.error("Error al crear backup de la base de datos: {}", e.getMessage());
            throw new RuntimeException("Error al generar backup: " + e.getMessage(), e);
        }
    }

    

    private String getMysqldumpNotFoundMessage() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        if (isWindows) {
            return "mysqldump no está disponible en el sistema.\n\n" +
                   "SOLUCIONES PARA WINDOWS:\n\n" +
                   "1. PARA XAMPP:\n" +
                   "   - Agregar 'C:\\xampp\\mysql\\bin' al PATH del sistema\n" +
                   "   - Reiniciar la aplicación después del cambio\n\n" +
                   "2. PARA MYSQL INSTALACIÓN INDEPENDIENTE:\n" +
                   "   - Buscar la carpeta de instalación de MySQL (ej: C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin)\n" +
                   "   - Agregar esa ruta al PATH del sistema\n\n" +
                   "3. INSTALAR MYSQL CLIENT:\n" +
                   "   - Descargar desde: https://dev.mysql.com/downloads/mysql/\n" +
                   "   - Instalar solo el cliente si no necesitas el servidor\n\n" +
                   "4. CÓMO AGREGAR AL PATH:\n" +
                   "   - Panel de Control > Sistema > Configuración avanzada del sistema\n" +
                   "   - Variables de entorno > Variable PATH > Editar > Nuevo\n" +
                   "   - Agregar la ruta del bin de MySQL\n" +
                   "   - Reiniciar la aplicación";
        } else {
            return "mysqldump no está disponible en el sistema.\n\n" +
                   "SOLUCIONES PARA LINUX/MAC:\n\n" +
                   "1. UBUNTU/DEBIAN:\n" +
                   "   sudo apt-get install mysql-client\n\n" +
                   "2. CENTOS/RHEL:\n" +
                   "   sudo yum install mysql\n\n" +
                   "3. MAC (con Homebrew):\n" +
                   "   brew install mysql-client\n\n" +
                   "4. Verificar instalación:\n" +
                   "   which mysqldump";
        }
    }

    

    public List<BackupInfo> getAvailableBackups() {
        List<BackupInfo> backups = new ArrayList<>();
        
        try {
            Path backupDir = Paths.get(backupDirectory);
            if (!Files.exists(backupDir)) {
                return backups;
            }

            Files.list(backupDir)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .sorted((p1, p2) -> p2.toFile().lastModified() > p1.toFile().lastModified() ? 1 : -1)
                    .forEach(path -> {
                        try {
                            File file = path.toFile();
                            BackupInfo info = new BackupInfo();
                            info.setFileName(file.getName());
                            info.setFilePath(file.getAbsolutePath());
                            info.setFileSize(file.length());
                            info.setCreatedDate(LocalDateTime.ofEpochSecond(
                                    file.lastModified() / 1000, 0, 
                                    java.time.ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now())
                            ));
                            backups.add(info);
                        } catch (Exception e) {
                            logger.warn("Error al procesar archivo de backup: {}", path);
                        }
                    });

        } catch (Exception e) {
            logger.error("Error al obtener lista de backups: {}", e.getMessage());
        }

        return backups;
    }

    

    public boolean deleteBackup(String fileName) {
        try {
            Path backupFilePath = Paths.get(backupDirectory, fileName);
            
            
            if (!Files.exists(backupFilePath) || !backupFilePath.startsWith(backupDirectory)) {
                return false;
            }

            Files.delete(backupFilePath);
            logger.info("Backup eliminado exitosamente: {}", fileName);
            return true;

        } catch (Exception e) {
            logger.error("Error al eliminar backup {}: {}", fileName, e.getMessage());
            return false;
        }
    }

    

    public byte[] getBackupContent(String fileName) throws IOException {
        Path backupFilePath = Paths.get(backupDirectory, fileName);
        
        
        if (!Files.exists(backupFilePath) || !backupFilePath.startsWith(backupDirectory)) {
            throw new IOException("Archivo de backup no encontrado: " + fileName);
        }

        return Files.readAllBytes(backupFilePath);
    }

    

    private void createBackupDirectory() throws IOException {
        Path backupDir = Paths.get(backupDirectory);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
            logger.info("Directorio de backups creado: {}", backupDirectory);
        }
    }

    

    private DatabaseInfo extractDatabaseInfo() {
        DatabaseInfo info = new DatabaseInfo();
        
        
        String[] parts = datasourceUrl.split("/");
        String databaseName = parts[parts.length - 1];
        
        String[] hostPort = parts[2].split(":");
        String host = hostPort[0];
        int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 3306;

        info.setHost(host);
        info.setPort(port);
        info.setDatabaseName(databaseName);
        info.setUsername(datasourceUsername);
        info.setPassword(datasourcePassword);

        return info;
    }

    

    private String generateBackupFileName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return "backup_docdb_" + now.format(formatter) + ".sql";
    }    

    private void executeBackupCommand(DatabaseInfo dbInfo, Path backupFilePath) throws Exception {
        List<String> command = new ArrayList<>();
        
        
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        if (isWindows) {
            command.add("cmd");
            command.add("/c");
        }

        command.addAll(Arrays.asList(
                "mysqldump",
                "--host=" + dbInfo.getHost(),
                "--port=" + dbInfo.getPort(),
                "--user=" + dbInfo.getUsername(),
                "--password=" + dbInfo.getPassword(),
                "--single-transaction",
                "--routines",
                "--triggers",
                "--events",
                "--result-file=" + backupFilePath.toString(),
                dbInfo.getDatabaseName()
        ));

        
        List<String> logCommand = new ArrayList<>(command);
        for (int i = 0; i < logCommand.size(); i++) {
            if (logCommand.get(i).startsWith("--password=")) {
                logCommand.set(i, "--password=****");
            }
        }
        logger.info("Ejecutando comando mysqldump: {}", String.join(" ", logCommand));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("MYSQL_PWD", dbInfo.getPassword());        
        Process process = processBuilder.start();

        
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.debug("mysqldump output: {}", line);
            }

            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
                logger.warn("mysqldump error: {}", line);
            }
        }

        int exitCode = process.waitFor();
        logger.info("mysqldump terminó con código: {}", exitCode);

        if (exitCode != 0) {
            String errorMessage = errorOutput.toString();
            logger.error("Error en mysqldump (código {}): {}", exitCode, errorMessage);
            
            
            if (errorMessage.contains("Access denied")) {
                throw new RuntimeException("Error de autenticación: Verificar usuario y contraseña de la base de datos");
            } else if (errorMessage.contains("Can't connect")) {
                throw new RuntimeException("Error de conexión: No se puede conectar a la base de datos MySQL");
            } else if (errorMessage.contains("Unknown database")) {
                throw new RuntimeException("Error: La base de datos especificada no existe");
            } else {
                throw new RuntimeException("Error al ejecutar mysqldump: " + errorMessage);
            }
        }

        
        if (!Files.exists(backupFilePath) || Files.size(backupFilePath) == 0) {
            throw new RuntimeException("El archivo de backup no se generó correctamente");
        }

        logger.info("Comando mysqldump ejecutado exitosamente. Archivo: {}, Tamaño: {} bytes", 
                   backupFilePath, Files.size(backupFilePath));
    }

    

    private void cleanupOldBackups() {
        try {
            Path backupDir = Paths.get(backupDirectory);
            List<Path> backupFiles = new ArrayList<>();

            Files.list(backupDir)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .forEach(backupFiles::add);

            
            backupFiles.sort((p1, p2) -> Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()));

            
            if (backupFiles.size() > maxBackupFiles) {
                for (int i = maxBackupFiles; i < backupFiles.size(); i++) {
                    try {
                        Files.delete(backupFiles.get(i));
                        logger.info("Backup antiguo eliminado: {}", backupFiles.get(i).getFileName());
                    } catch (Exception e) {
                        logger.warn("No se pudo eliminar backup antiguo: {}", backupFiles.get(i));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error al limpiar backups antiguos: {}", e.getMessage());
        }
    }

    

    private boolean isMysqldumpAvailable() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");
            
            List<String> command = new ArrayList<>();
            if (isWindows) {
                command.add("cmd");
                command.add("/c");
                command.add("where");
                command.add("mysqldump");
            } else {
                command.add("which");
                command.add("mysqldump");
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            return exitCode == 0;
        } catch (Exception e) {
            logger.debug("Error al verificar disponibilidad de mysqldump: {}", e.getMessage());
            return false;
        }
    }

    

    private static class DatabaseInfo {
        private String host;
        private int port;
        private String databaseName;
        private String username;
        private String password;

        
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getDatabaseName() { return databaseName; }
        public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    

    public static class BackupInfo {
        private String fileName;
        private String filePath;
        private long fileSize;
        private LocalDateTime createdDate;

        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

        public String getFormattedFileSize() {
            if (fileSize < 1024) return fileSize + " B";
            if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
            if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
