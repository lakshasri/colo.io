package com.coloio.srms.service;

import com.coloio.srms.domain.zone.Zone;
import com.coloio.srms.entity.ZoneEntity;
import com.coloio.srms.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ZoneService {

    private final ZoneRepository zoneRepository;

    public ZoneService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    public Zone createZone(Zone zone) {
        if (zoneRepository.existsByName(zone.getName())) {
            throw new IllegalArgumentException("Zone name already exists: " + zone.getName());
        }
        ZoneEntity saved = zoneRepository.save(toEntity(zone));
        return toDomain(saved);
    }

    @Transactional(readOnly = true)
    public Zone getZone(Long id) {
        return toDomain(zoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<Zone> getAllZones() {
        return zoneRepository.findAll().stream().map(this::toDomain).toList();
    }

    public Zone updateZone(Long id, Zone updated) {
        ZoneEntity entity = zoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + id));
        entity.setName(updated.getName());
        entity.setFloor(updated.getFloor());
        entity.setPowerBudgetKw(updated.getPowerBudgetKw());
        entity.setCoolingCapacity(updated.getCoolingCapacity());
        return toDomain(zoneRepository.save(entity));
    }

    public void deleteZone(Long id) {
        zoneRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ZoneEntity loadEntity(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + id));
    }

    // Mappers
    private ZoneEntity toEntity(Zone z) {
        ZoneEntity e = new ZoneEntity();
        e.setName(z.getName());
        e.setFloor(z.getFloor());
        e.setPowerBudgetKw(z.getPowerBudgetKw());
        e.setCoolingCapacity(z.getCoolingCapacity());
        return e;
    }

    public Zone toDomain(ZoneEntity e) {
        return new Zone(e.getZoneId(), e.getName(), e.getFloor(),
                e.getPowerBudgetKw(), e.getCoolingCapacity());
    }
}
