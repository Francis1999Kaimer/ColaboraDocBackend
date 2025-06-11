package com.example.project.services;

import com.example.project.DTO.AnnotationDTO;
import com.example.project.entities.Annotation;
import com.example.project.entities.AnnotationType;
import com.example.project.entities.User;
import com.example.project.entities.Version;
import com.example.project.repositories.AnnotationRepository;
import com.example.project.repositories.UserRepository;
import com.example.project.repositories.VersionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnotationService {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private VersionRepository versionRepository;    @Autowired
    private UserRepository userRepository;

    @Autowired
    private dtoMapper mapper;

    

    @Transactional(readOnly = true)
    public List<AnnotationDTO> getAnnotationsByVersion(Integer versionId) {
        List<Annotation> annotations = annotationRepository.findByVersionId(versionId);
        return annotations.stream()
                .map(mapper::toAnnotationDTO)
                .collect(Collectors.toList());
    }

    

    public AnnotationDTO createAnnotation(Integer versionId, Integer userId, AnnotationDTO annotationDTO) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Version not found with id: " + versionId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Annotation annotation = new Annotation();
        annotation.setVersion(version);
        annotation.setCreatedBy(user);
        annotation.setAnnotationType(annotationDTO.getAnnotationType());
        annotation.setContent(annotationDTO.getContent());
        annotation.setCoordinates(annotationDTO.getCoordinates());
        annotation.setColor(annotationDTO.getColor());
        annotation.setPageNumber(annotationDTO.getPageNumber());

        Annotation savedAnnotation = annotationRepository.save(annotation);
        return mapper.toAnnotationDTO(savedAnnotation);
    }

    

    public AnnotationDTO updateAnnotation(Integer annotationId, Integer userId, AnnotationDTO annotationDTO) {
        Annotation annotation = annotationRepository.findById(annotationId)
                .orElseThrow(() -> new EntityNotFoundException("Annotation not found with id: " + annotationId));

        
        if (!annotation.getCreatedBy().getIduser().equals(userId)) {
            throw new SecurityException("User can only update their own annotations");
        }        
        annotation.setContent(annotationDTO.getContent());
        annotation.setCoordinates(annotationDTO.getCoordinates());
        annotation.setColor(annotationDTO.getColor());
        annotation.setPageNumber(annotationDTO.getPageNumber());

        Annotation savedAnnotation = annotationRepository.save(annotation);
        return mapper.toAnnotationDTO(savedAnnotation);
    }

    

    public void deleteAnnotation(Integer annotationId, Integer userId) {
        Annotation annotation = annotationRepository.findById(annotationId)
                .orElseThrow(() -> new EntityNotFoundException("Annotation not found with id: " + annotationId));

        
        if (!annotation.getCreatedBy().getIduser().equals(userId)) {
            throw new SecurityException("User can only delete their own annotations");
        }        annotation.setDeleted(true);
        annotationRepository.save(annotation);
    }

    

    @Transactional(readOnly = true)
    public AnnotationDTO getAnnotationById(Integer annotationId) {
        Annotation annotation = annotationRepository.findById(annotationId)
                .orElseThrow(() -> new EntityNotFoundException("Annotation not found with id: " + annotationId));
        
        if (annotation.getDeleted()) {
            throw new EntityNotFoundException("Annotation has been deleted");
        }

        return mapper.toAnnotationDTO(annotation);
    }

    

    @Transactional(readOnly = true)
    public List<AnnotationDTO> getAnnotationsByVersionAndType(Integer versionId, AnnotationType type) {
        List<Annotation> annotations = annotationRepository.findByVersionIdAndType(versionId, type);
        return annotations.stream()
                .map(mapper::toAnnotationDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional(readOnly = true)
    public Long countAnnotationsByVersion(Integer versionId) {
        return annotationRepository.countByVersionId(versionId);
    }
}
