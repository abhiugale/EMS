package com.ems.modules.factory.repository;

import com.ems.modules.factory.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, UUID> {
}
