package com.ems.modules.upload.repository;

import com.ems.modules.upload.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UploadRepository extends JpaRepository<Upload, UUID> {
    List<Upload> findByFactoryIdOrderByCreatedAtDesc(UUID factoryId);

    @Query("SELECT u FROM Upload u JOIN FETCH u.factory WHERE u.id = :id")
    Optional<Upload> findByIdWithFactory(@Param("id") UUID id);
}
