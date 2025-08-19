package com.webbee.audit_api.service;

import com.webbee.audit_api.document.AuditMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditMethodSearchService {

    List<AuditMethod> searchMethods(String query, String level);

    List<AuditMethod> findMethodsByFields(String methodName, String level, String eventType);

    Map<String, Long> getMethodStats(String groupBy, LocalDateTime from, LocalDateTime to);

}
