package com.qlvt.repository;

import com.qlvt.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop30ByOrderByCreatedAtDesc();

    List<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(String entityName, String entityId);

    @Query("""
            select a from AuditLog a
            where (:actor = '' or lower(a.actorUsername) like lower(concat('%', :actor, '%')))
              and (:action = '' or lower(a.action) like lower(concat('%', :action, '%')))
              and (:entity = '' or lower(a.entityName) like lower(concat('%', :entity, '%')))
            order by a.createdAt desc
            """)
    List<AuditLog> search(@Param("actor") String actor, @Param("action") String action, @Param("entity") String entity);
}
