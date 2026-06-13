package com.ems.modules.report.entity;

import com.ems.modules.factory.entity.Factory;
import com.ems.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType; // DAILY, MONTHLY

    @Column(nullable = false, length = 20)
    private String status; // PROCESSING, SUCCESS, FAILED

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
