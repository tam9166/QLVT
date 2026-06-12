package com.qlvt.repository;

import com.qlvt.entity.Attachment;
import com.qlvt.enums.AttachmentReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByReferenceTypeAndReferenceIdAndDeletedFalseOrderByUploadedAtDesc(AttachmentReferenceType referenceType, Long referenceId);
}
