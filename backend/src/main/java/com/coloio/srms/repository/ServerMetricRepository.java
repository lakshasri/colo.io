package com.coloio.srms.repository;

import com.coloio.srms.entity.ServerMetricEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServerMetricRepository extends JpaRepository<ServerMetricEntity, Long> {

    List<ServerMetricEntity> findAllByServer_ServerIdOrderByRecordedAtDesc(Long serverId, Pageable pageable);

    Optional<ServerMetricEntity> findTopByServer_ServerIdOrderByRecordedAtDesc(Long serverId);
}
