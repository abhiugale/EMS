package com.ems.modules.machine.repository;

import com.ems.modules.machine.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MachineRepository extends JpaRepository<Machine, UUID> {
    List<Machine> findByFactoryId(UUID factoryId);
    Optional<Machine> findByFactoryIdAndName(UUID factoryId, String name);
    boolean existsByFactoryIdAndName(UUID factoryId, String name);
}
