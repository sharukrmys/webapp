package com.example.metadata.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.metadata.entity.MetadataEntity;
import com.example.metadata.repository.MetadataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final MetadataRepository metadataRepository;
    
    public List<MetadataEntity> getAllMetadata() {
        return metadataRepository.findAll();
    }
    
    public Optional<MetadataEntity> getMetadataById(Long id) {
        return metadataRepository.findById(id);
    }
    
    public MetadataEntity saveMetadata(MetadataEntity metadata) {
        return metadataRepository.save(metadata);
    }
    
    public void deleteMetadata(Long id) {
        metadataRepository.deleteById(id);
    }
}

