package com.coloio.srms.repository;

import com.coloio.srms.entity.SlaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaRepository extends JpaRepository<SlaEntity, Long> {
    List<SlaEntity> findByCustomer_UserIdOrderByStartDateDesc(Long customerId);
    List<SlaEntity> findByStatusOrderByStartDateDesc(String status);
    List<SlaEntity> findAllByOrderByStartDateDesc();
}
