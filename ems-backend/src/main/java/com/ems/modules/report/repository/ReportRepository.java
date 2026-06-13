package com.ems.modules.report.repository;

import com.ems.modules.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByFactoryIdOrderByCreatedAtDesc(UUID factoryId);
}
