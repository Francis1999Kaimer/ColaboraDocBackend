package com.example.project.DTO;

import com.example.project.entities.AnnotationType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AnnotationDTO {
    private Integer idannotation;
    private Integer versionId;
    private UserSummaryDTO createdBy;
    private Integer pageNumber;
    private AnnotationType annotationType;
    private String coordinates;
    private String content;
    private String styleProperties;
    private String color; 
    private Long versionAnnotation;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    
    public AnnotationDTO() {}    public AnnotationDTO(Integer idannotation, Integer versionId, UserSummaryDTO createdBy, 
                        Integer pageNumber, AnnotationType annotationType, String coordinates, 
                        String content, String styleProperties, String color, Long versionAnnotation, 
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.idannotation = idannotation;
        this.versionId = versionId;
        this.createdBy = createdBy;
        this.pageNumber = pageNumber;
        this.annotationType = annotationType;
        this.coordinates = coordinates;
        this.content = content;
        this.styleProperties = styleProperties;
        this.color = color;
        this.versionAnnotation = versionAnnotation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    
    public Integer getIdannotation() { return idannotation; }
    public void setIdannotation(Integer idannotation) { this.idannotation = idannotation; }

    public Integer getVersionId() { return versionId; }
    public void setVersionId(Integer versionId) { this.versionId = versionId; }

    public UserSummaryDTO getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserSummaryDTO createdBy) { this.createdBy = createdBy; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public AnnotationType getAnnotationType() { return annotationType; }
    public void setAnnotationType(AnnotationType annotationType) { this.annotationType = annotationType; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }    public String getStyleProperties() { return styleProperties; }
    public void setStyleProperties(String styleProperties) { this.styleProperties = styleProperties; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Long getVersionAnnotation() { return versionAnnotation; }
    public void setVersionAnnotation(Long versionAnnotation) { this.versionAnnotation = versionAnnotation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
