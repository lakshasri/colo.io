package com.coloio.srms.repository;

import com.coloio.srms.entity.ZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<ZoneEntity, Long> {
    Optional<ZoneEntity> findByName(String name);
    List<ZoneEntity> findAllByFloor(Integer floor);
    boolean existsByName(String name);
}
