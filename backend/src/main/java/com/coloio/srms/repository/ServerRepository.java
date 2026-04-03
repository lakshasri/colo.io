package com.coloio.srms.repository;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.entity.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<ServerEntity, Long> {

    List<ServerEntity> findAllByRack_RackId(Long rackId);

    List<ServerEntity> findAllByCustomer_UserId(Long customerId);

    List<ServerEntity> findAllByStatus(ServerStatus status);

    boolean existsByHostname(String hostname);
}
