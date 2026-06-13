package com.ems.modules.alert.repository;

import com.ems.modules.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    long countByFactoryIdAndStatus(UUID factoryId, String status);
    List<Alert> findByFactoryIdOrderByCreatedAtDesc(UUID factoryId);
}
