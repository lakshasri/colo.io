package com.coloio.srms.service;

import com.coloio.srms.domain.enums.RackStatus;
import com.coloio.srms.domain.rack.Rack;
import com.coloio.srms.entity.RackEntity;
import com.coloio.srms.entity.ZoneEntity;
import com.coloio.srms.repository.RackRepository;
import com.coloio.srms.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RackService {

    private final RackRepository rackRepository;
    private final ZoneRepository zoneRepository;

    public RackService(RackRepository rackRepository, ZoneRepository zoneRepository) {
        this.rackRepository = rackRepository;
        this.zoneRepository = zoneRepository;
    }

    public Rack createRack(Rack rack) {
        ZoneEntity zone = zoneRepository.findById(rack.getZoneId())
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + rack.getZoneId()));
        RackEntity saved = rackRepository.save(toEntity(rack, zone));
        return toDomain(saved);
    }

    @Transactional(readOnly = true)
    public Rack getRack(Long id) {
        return toDomain(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<Rack> getAllRacks() {
        return rackRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public List<Rack> getRacksByZone(Long zoneId) {
        return rackRepository.findAllByZone_ZoneId(zoneId).stream().map(this::toDomain).toList();
    }

    public Rack updateRack(Long id, Rack updated) {
        RackEntity entity = findEntity(id);
        entity.setName(updated.getName());
        entity.setLocation(updated.getLocation());
        entity.setTotalUSpace(updated.getTotalUSpace());
        entity.setMaxPowerKw(updated.getMaxPowerKw());
        if (updated.getZoneId() != null) {
            ZoneEntity zone = zoneRepository.findById(updated.getZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("Zone not found"));
            entity.setZone(zone);
        }
        return toDomain(rackRepository.save(entity));
    }

    public void changeStatus(Long id, RackStatus newStatus) {
        RackEntity entity = findEntity(id);
        entity.setStatus(newStatus);
        rackRepository.save(entity);
    }

    public void deleteRack(Long id) {
        rackRepository.deleteById(id);
    }

    // Called by ServerService when a server is placed/removed
    public void updateUSpaceAndPower(Long rackId, int uSpaceDelta, double powerDelta) {
        RackEntity entity = findEntity(rackId);
        entity.setUsedUSpace(entity.getUsedUSpace() + uSpaceDelta);
        entity.setCurrentPowerKw(entity.getCurrentPowerKw() + powerDelta);
        rackRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUtilization(Long id) {
        Rack rack = getRack(id);
        return Map.of(
                "rackId", rack.getRackId(),
                "name", rack.getName(),
                "uSpaceUsed", rack.getUsedUSpace(),
                "uSpaceTotal", rack.getTotalUSpace(),
                "uSpacePercent", rack.getUSpaceUtilizationPercent(),
                "powerUsedKw", rack.getCurrentPowerKw(),
                "powerMaxKw", rack.getMaxPowerKw(),
                "powerPercent", rack.getPowerUtilizationPercent()
        );
    }

    @Transactional(readOnly = true)
    public List<RackEntity> findActiveRackEntities() {
        return rackRepository.findAllByStatus(RackStatus.ACTIVE);
    }

    public RackEntity findEntity(Long id) {
        return rackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rack not found: " + id));
    }

    // --- Mappers ---
    private RackEntity toEntity(Rack r, ZoneEntity zone) {
        RackEntity e = new RackEntity();
        e.setName(r.getName());
        e.setZone(zone);
        e.setLocation(r.getLocation());
        e.setTotalUSpace(r.getTotalUSpace() > 0 ? r.getTotalUSpace() : 42);
        e.setUsedUSpace(r.getUsedUSpace());
        e.setMaxPowerKw(r.getMaxPowerKw());
        e.setCurrentPowerKw(r.getCurrentPowerKw());
        e.setStatus(r.getStatus() != null ? r.getStatus() : RackStatus.ACTIVE);
        return e;
    }

    public Rack toDomain(RackEntity e) {
        return new Rack(
                e.getRackId(), e.getName(),
                e.getZone() != null ? e.getZone().getZoneId() : null,
                e.getLocation(), e.getTotalUSpace(), e.getUsedUSpace(),
                e.getMaxPowerKw() != null ? e.getMaxPowerKw() : 0,
                e.getCurrentPowerKw() != null ? e.getCurrentPowerKw() : 0,
                e.getStatus()
        );
    }
}
