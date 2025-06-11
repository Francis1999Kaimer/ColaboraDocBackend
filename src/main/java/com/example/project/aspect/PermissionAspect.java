package com.example.project.aspect;

import com.example.project.annotation.RequiresPermission;
import com.example.project.services.AuthorizationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;



@Aspect
@Component
public class PermissionAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);
    
    @Autowired
    private AuthorizationService authorizationService;
    
    

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        
        try {
            
            Integer projectId = extractProjectId(joinPoint, requiresPermission);
            
            if (projectId == null) {
                logger.warn("No se pudo extraer projectId del método {}", joinPoint.getSignature().getName());
                throw new IllegalArgumentException("No se pudo determinar el ID del proyecto para verificar permisos");
            }
            
            
            authorizationService.checkPermission(projectId, requiresPermission.value());
            
            logger.debug("Permiso {} verificado exitosamente para proyecto {}", 
                requiresPermission.value(), projectId);
            
            
            return joinPoint.proceed();
            
        } catch (Exception e) {
            logger.warn("Error de permisos en método {}: {}", 
                joinPoint.getSignature().getName(), e.getMessage());
            throw e;
        }
    }
      

    private Integer extractProjectId(ProceedingJoinPoint joinPoint, RequiresPermission annotation) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        String methodName = joinPoint.getSignature().getName();
          
        if (annotation.autoDetectProject() && args.length > 0) {
            Object firstArg = args[0];

            if (firstArg != null && !isPrimitiveType(firstArg)) {
                Integer projectIdFromDto = extractProjectIdFromDto(firstArg);
                if (projectIdFromDto != null) {
                    return projectIdFromDto;
                }
                
                
                if (methodName.toLowerCase().contains("create") && 
    (methodName.toLowerCase().contains("document") || methodName.toLowerCase().contains("folder"))) {
                    Integer folderIdFromDto = extractFolderIdFromDto(firstArg);
                    if (folderIdFromDto != null) {
                        return authorizationService.getProjectIdFromFolder(folderIdFromDto);
                    }
                }
            }


            if (firstArg instanceof Integer) {
                Integer entityId = (Integer) firstArg;
                
                
                Integer derivedProjectId = deriveProjectIdFromContext(methodName, entityId);
                if (derivedProjectId != null) {
                    return derivedProjectId;
                }
                
                
                return entityId;
            }
        }
        
        
        if (!annotation.projectIdParam().isEmpty()) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(annotation.projectIdParam()) && 
                    args[i] instanceof Integer) {
                    return (Integer) args[i];
                }
            }
        }
        
        
        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName().toLowerCase();
            if ((paramName.contains("project") || paramName.contains("Project")) && 
                args[i] instanceof Integer) {
                return (Integer) args[i];
            }
        }
        
        return null;
    }
    
    

    private Integer deriveProjectIdFromContext(String methodName, Integer entityId) {
        try {
            
            if (methodName.contains("Folder") || methodName.contains("folder")) {
                return authorizationService.getProjectIdFromFolder(entityId);
            }
            
            
            if (methodName.contains("Document") || methodName.contains("document")) {
                return authorizationService.getProjectIdFromDocument(entityId);
            }
            
            
            if (methodName.contains("Version") || methodName.contains("version")) {
                return authorizationService.getProjectIdFromVersion(entityId);
            }
            
            
            return null;
        } catch (Exception e) {
            logger.warn("Error al derivar projectId para método {} con entityId {}: {}", 
                methodName, entityId, e.getMessage());
            return null;
        }
    }

    private boolean isPrimitiveType(Object obj) {
        return obj instanceof Integer || 
            obj instanceof String || 
            obj instanceof Boolean || 
            obj instanceof Long ||
            obj instanceof Double;
    }
    private Integer extractProjectIdFromDto(Object dto) {
        try {
            
            Field[] fields = dto.getClass().getDeclaredFields();
            
            for (Field field : fields) {
                String fieldName = field.getName().toLowerCase();
                
                
                if (fieldName.contains("project") && fieldName.contains("id")) {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    
                    if (value instanceof Integer) {
                        return (Integer) value;
                    }
                }
            }
            
            
            Method[] methods = dto.getClass().getDeclaredMethods();
            for (Method method : methods) {
                String methodName = method.getName().toLowerCase();
                
                if ((methodName.startsWith("get") && 
                    methodName.contains("project") && 
                    methodName.contains("id")) ||
                    methodName.equals("getprojectid")) {
                    
                    method.setAccessible(true);
                    Object value = method.invoke(dto);
                    
                    if (value instanceof Integer) {
                        return (Integer) value;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.debug("No se pudo extraer projectId del DTO {}: {}", 
                        dto.getClass().getSimpleName(), e.getMessage());
        }
        
        return null;
    }

    

    private Integer extractFolderIdFromDto(Object dto) {
        try {
            Field[] fields = dto.getClass().getDeclaredFields();
            
            for (Field field : fields) {
                String fieldName = field.getName().toLowerCase();
                
                
                if (fieldName.contains("folder") && fieldName.contains("id")) {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    
                    if (value instanceof Integer) {
                        return (Integer) value;
                    }
                }
            }
            
            
            Method[] methods = dto.getClass().getDeclaredMethods();
            for (Method method : methods) {
                String methodName = method.getName().toLowerCase();
                
                if ((methodName.startsWith("get") && 
                    methodName.contains("folder") && 
                    methodName.contains("id")) ||
                    methodName.equals("getfolderid")) {
                    
                    method.setAccessible(true);
                    Object value = method.invoke(dto);
                    
                    if (value instanceof Integer) {
                        return (Integer) value;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.debug("No se pudo extraer folderId del DTO {}: {}", 
                        dto.getClass().getSimpleName(), e.getMessage());
        }
        
        return null;
    }

}
