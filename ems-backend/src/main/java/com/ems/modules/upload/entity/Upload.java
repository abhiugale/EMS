package com.ems.modules.upload.entity;

import com.ems.modules.factory.entity.Factory;
import com.ems.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "uploads")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Upload {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(nullable = false)
    private String filename;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "row_count")
    private Integer rowCount = 0;

    @Column(nullable = false)
    private String status; // PROCESSING, SUCCESS, FAILED

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "factory_timezone", length = 50)
    private String factoryTimezone;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
