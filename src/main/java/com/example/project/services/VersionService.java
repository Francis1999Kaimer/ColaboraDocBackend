package com.example.project.services;

import com.example.project.DTO.VersionCreateRequestDTO;
import com.example.project.DTO.VersionDTO;
import com.example.project.entities.Document;
import com.example.project.entities.Version;
import com.example.project.repositories.DocumentRepository;
import com.example.project.repositories.VersionRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionService {

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private dtoMapper dtoMapper;

    @Transactional
    public VersionDTO createVersion(VersionCreateRequestDTO requestDTO) {
        Document document = documentRepository.findById(requestDTO.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + requestDTO.getDocumentId()));

        Version version = new Version();
        version.setDocument(document);
        version.setVersionNumber(requestDTO.getVersionNumber());
        version.setDropboxFileId(requestDTO.getDropboxFileId());
        version.setDropboxFilePath(requestDTO.getDropboxFilePath());
        version.setFileSize(requestDTO.getFileSize());
        version.setMimeType(requestDTO.getMimeType());
        version.setComments(requestDTO.getComments());


        Version savedVersion = versionRepository.save(version);


        return dtoMapper.toVersionDTO(savedVersion);
    }



    @Transactional(readOnly = true)
    public List<VersionDTO> getVersionsForDocument(Integer documentId) {
  
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));

        return document.getVersions().stream() 
                .map(dtoMapper::toVersionDTO)
                .collect(Collectors.toList());
    }
}