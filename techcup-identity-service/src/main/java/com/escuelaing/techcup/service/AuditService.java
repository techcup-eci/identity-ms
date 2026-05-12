package com.escuelaing.techcup.service;

import com.escuelaing.techcup.model.AuditLog;
import com.escuelaing.techcup.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String action, String userEmail, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUserEmail(userEmail);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}