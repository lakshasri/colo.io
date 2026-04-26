package com.coloio.srms.repository;

import com.coloio.srms.entity.MaintenanceTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicketEntity, Long> {
    List<MaintenanceTicketEntity> findByServer_ServerIdOrderByCreatedAtDesc(Long serverId);
    List<MaintenanceTicketEntity> findByStatusOrderByCreatedAtDesc(String status);
    List<MaintenanceTicketEntity> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
    List<MaintenanceTicketEntity> findByApprovedFalseAndStatusOrderByCreatedAtDesc(String status);
    long countByApprovedFalseAndStatus(String status);
}
