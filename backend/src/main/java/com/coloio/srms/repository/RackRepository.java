package com.coloio.srms.repository;

import com.coloio.srms.domain.enums.RackStatus;
import com.coloio.srms.entity.RackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RackRepository extends JpaRepository<RackEntity, Long> {

    List<RackEntity> findAllByZone_ZoneId(Long zoneId);

    List<RackEntity> findAllByStatus(RackStatus status);

    @Query("SELECT r FROM RackEntity r WHERE (r.totalUSpace - r.usedUSpace) >= :minUSpace AND r.status = 'ACTIVE'")
    List<RackEntity> findActiveRacksWithAvailableUSpace(int minUSpace);

    boolean existsByName(String name);

    @Query("SELECT r FROM RackEntity r WHERE " +
           "(:zoneId IS NULL OR r.zone.zoneId = :zoneId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:maxPower IS NULL OR r.currentPowerKw <= :maxPower)")
    List<RackEntity> search(@Param("zoneId") Long zoneId,
                             @Param("status") RackStatus status,
                             @Param("maxPower") Double maxPower);
}
