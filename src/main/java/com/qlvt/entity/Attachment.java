package com.qlvt.entity;

import com.qlvt.enums.AttachmentReferenceType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "nvarchar(260)")
    private String originalFileName;
    @Column(nullable = false, length = 120)
    private String storedFileName;
    @Column(nullable = false, length = 120)
    private String contentType;
    private long fileSize;
    @Column(nullable = false, columnDefinition = "nvarchar(1000)")
    private String storagePath;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AttachmentReferenceType referenceType;
    @Column(nullable = false)
    private Long referenceId;
    private String uploadedBy;
    private LocalDateTime uploadedAt = LocalDateTime.now();
    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    private boolean deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public AttachmentReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(AttachmentReferenceType referenceType) { this.referenceType = referenceType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
