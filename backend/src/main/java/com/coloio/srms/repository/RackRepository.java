package com.coloio.srms.repository;

import com.coloio.srms.domain.enums.RackStatus;
import com.coloio.srms.entity.RackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RackRepository extends JpaRepository<RackEntity, Long> {

    List<RackEntity> findAllByZone_ZoneId(Long zoneId);

    List<RackEntity> findAllByStatus(RackStatus status);

    @Query("SELECT r FROM RackEntity r WHERE (r.totalUSpace - r.usedUSpace) >= :minUSpace AND r.status = 'ACTIVE'")
    List<RackEntity> findActiveRacksWithAvailableUSpace(int minUSpace);

    boolean existsByName(String name);
}
