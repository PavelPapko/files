package com.storage.files.repository;

import com.storage.files.models.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByFilename(String filename);

    List<FileEntity> findByUserId(Long userId);

    List<FileEntity> findByFolderId(Long folderId);

    Page<FileEntity> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT f FROM FileEntity f WHERE " +
            "(:filename IS NULL OR f.filename LIKE %:filename%) AND " +
            "(:uploadedBy IS NULL OR f.uploadedBy = :uploadedBy) AND " +
            "(:folderId IS NULL OR f.folderId = :folderId) AND " +
            "f.isDeleted = false")
    List<FileEntity> searchFiles(@Param("filename") String filename,
                                 @Param("uploadedBy") String uploadedBy,
                                 @Param("folderId") Long folderId);

    @Modifying
    @Query("UPDATE FileEntity f SET f.isDeleted = true WHERE f.id = :id")
    void softDelete(@Param("id") Long id);

    boolean existsByFilename(String filename);
}