package com.coloio.srms.repository;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<ServerEntity, Long> {

    List<ServerEntity> findAllByRack_RackId(Long rackId);

    List<ServerEntity> findAllByCustomer_UserId(Long customerId);

    List<ServerEntity> findAllByStatus(ServerStatus status);

    boolean existsByHostname(String hostname);

    List<ServerEntity> findAllByHostnameContainingIgnoreCase(String hostname);

    @Query("SELECT s FROM ServerEntity s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:hostname IS NULL OR LOWER(s.hostname) LIKE LOWER(CONCAT('%', :hostname, '%'))) AND " +
           "(:rackId IS NULL OR s.rack.rackId = :rackId)")
    List<ServerEntity> search(@Param("status") ServerStatus status,
                               @Param("hostname") String hostname,
                               @Param("rackId") Long rackId);
}
