package com.storage.files.repository;


import com.storage.files.repository.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Long> {

    List<FolderEntity> findByParentId(Long parentId);

    List<FolderEntity> findByCreatedBy(Long userId);

    @Query("SELECT f FROM FolderEntity f WHERE f.parentId IS NULL")
    List<FolderEntity> findRootFolders();

    boolean existsByNameAndParentId(String name, Long parentId);
}