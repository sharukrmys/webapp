package com.example.applib.service;

import com.example.applib.entity.MasterTenant;
import com.example.applib.repository.MasterTenantRepository;
import com.example.applib.tenant.TenantContext;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final MasterTenantRepository masterTenantRepository;

    public List<MasterTenant> getAllTenants() {
        return masterTenantRepository.findAll();
    }

    public Optional<MasterTenant> getTenantById(Long id) {
        return masterTenantRepository.findById(id);
    }

    public Optional<MasterTenant> getTenantByTenantId(String tenantId) {
        return masterTenantRepository.findByTenantId(tenantId);
    }

    public Optional<MasterTenant> getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("No tenant ID found in context");
            return Optional.empty();
        }
        return getTenantByTenantId(tenantId);
    }

    public MasterTenant createTenant(MasterTenant tenant) {
        return masterTenantRepository.save(tenant);
    }

    public Optional<MasterTenant> updateTenant(Long id, MasterTenant tenant) {
        return masterTenantRepository.findById(id)
                .map(existingTenant -> {
                    tenant.setId(id);
                    return masterTenantRepository.save(tenant);
                });
    }

    public boolean deleteTenant(Long id) {
        return masterTenantRepository.findById(id)
                .map(tenant -> {
                    masterTenantRepository.delete(tenant);
                    return true;
                })
                .orElse(false);
    }

    public boolean existsByTenantId(String tenantId) {
        return masterTenantRepository.existsByTenantId(tenantId);
    }
}
