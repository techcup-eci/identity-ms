package com.escuelaing.techcup.repository;

import com.escuelaing.techcup.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}