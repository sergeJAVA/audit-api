package com.webbee.audit_api.service;

import com.webbee.audit_api.document.AuditRequest;

import java.util.List;
import java.util.Map;

public interface AuditRequestSearchService {

    List<AuditRequest> searchRequests(String query, String statusCode);

    Map<String, Long> getRequestStats(String groupBy, String direction);

    List<AuditRequest> findRequestsByFields(String url, String method, String statusCode);

}
