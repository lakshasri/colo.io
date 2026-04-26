package com.coloio.srms.repository;

import com.coloio.srms.domain.enums.AlertSeverity;
import com.coloio.srms.domain.enums.AlertType;
import com.coloio.srms.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    List<AlertEntity> findAllByAcknowledgedFalseOrderByCreatedAtDesc();

    List<AlertEntity> findAllBySourceTypeAndSourceId(String sourceType, String sourceId);

    List<AlertEntity> findAllBySeverityOrderByCreatedAtDesc(AlertSeverity severity);

    List<AlertEntity> findAllByTypeOrderByCreatedAtDesc(AlertType type);

    long countByAcknowledgedFalse();
}
