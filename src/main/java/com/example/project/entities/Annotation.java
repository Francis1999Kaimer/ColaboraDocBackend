package com.example.project.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "annotations")
@EntityListeners(AuditingEntityListener.class)
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idannotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    @JsonIgnore
    private Version version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "annotation_type", nullable = false)
    private AnnotationType annotationType;

    @Column(name = "coordinates", columnDefinition = "JSON")
    private String coordinates; 

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "style_properties", columnDefinition = "JSON")
    private String styleProperties; 

    @Column(name = "version_annotation", nullable = false)
    private Long versionAnnotation = 1L; 

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    
    public Annotation() {}

    
    public Integer getIdannotation() { return idannotation; }
    public void setIdannotation(Integer idannotation) { this.idannotation = idannotation; }

    public Version getVersion() { return version; }
    public void setVersion(Version version) { this.version = version; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public AnnotationType getAnnotationType() { return annotationType; }
    public void setAnnotationType(AnnotationType annotationType) { this.annotationType = annotationType; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStyleProperties() { return styleProperties; }
    public void setStyleProperties(String styleProperties) { this.styleProperties = styleProperties; }

    public Long getVersionAnnotation() { return versionAnnotation; }
    public void setVersionAnnotation(Long versionAnnotation) { this.versionAnnotation = versionAnnotation; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    
    public String getColor() {
        
        if (styleProperties != null && styleProperties.contains("\"color\"")) {
            try {
                
                String[] parts = styleProperties.split("\"color\"\\s*:\\s*\"");
                if (parts.length > 1) {
                    String colorPart = parts[1].split("\"")[0];
                    return colorPart;
                }
            } catch (Exception e) {
                
            }
        }
        return null;
    }

    public void setColor(String color) {
        
        if (color != null) {
            if (styleProperties == null || styleProperties.trim().isEmpty()) {
                styleProperties = "{\"color\":\"" + color + "\"}";
            } else {
                
                if (styleProperties.contains("\"color\"")) {
                    styleProperties = styleProperties.replaceAll(
                        "\"color\"\\s*:\\s*\"[^\"]*\"", 
                        "\"color\":\"" + color + "\""
                    );
                } else {
                    
                    styleProperties = styleProperties.replaceFirst(
                        "\\{", 
                        "{\"color\":\"" + color + "\","
                    );
                }
            }
        }
    }

    
    public boolean getDeleted() {
        return !isActive;
    }

    public void setDeleted(boolean deleted) {
        this.isActive = !deleted;
    }
}
