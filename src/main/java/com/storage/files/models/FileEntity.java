package com.storage.files.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    private String description;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    private Integer version;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "folder_id")
    private Long folderId;

    @PrePersist
    protected void onCreate() {
        uploadedDate = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
        isDeleted = false;
        version = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}