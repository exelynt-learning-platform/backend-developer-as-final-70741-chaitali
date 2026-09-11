package com.booking.resource_booking_system.service;

import com.booking.resource_booking_system.dto.ResourceRequest;
import com.booking.resource_booking_system.entity.Resource;
import com.booking.resource_booking_system.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Resource not found"));
    }

    public Resource createResource(ResourceRequest request) {

        Resource resource = new Resource();

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setAvailable(request.isAvailable());

        return resourceRepository.save(resource);
    }

    public Resource updateResource(Long id, ResourceRequest request) {

        Resource resource = getResourceById(id);

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setAvailable(request.isAvailable());

        return resourceRepository.save(resource);
    }

    public void deleteResource(Long id) {

        Resource resource = getResourceById(id);

        resourceRepository.delete(resource);
    }
}