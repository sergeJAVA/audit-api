package com.webbee.audit_api.controller;

import com.webbee.audit_api.document.AuditRequest;
import com.webbee.audit_api.dto.RequestSearchResponse;
import com.webbee.audit_api.dto.StatsResponse;
import com.webbee.audit_api.service.AuditRequestSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit/requests")
@RequiredArgsConstructor
public class AuditRequestController {

    private final AuditRequestSearchService auditRequestSearchService;

    @GetMapping("/search")
    public ResponseEntity<RequestSearchResponse> searchRequests(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String statusCode) {

        List<AuditRequest> requests = auditRequestSearchService.searchRequests(query, statusCode);
        return ResponseEntity.ok(new RequestSearchResponse(requests));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getRequestStats(
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String direction) {

        Map<String, Long> stats = auditRequestSearchService.getRequestStats(groupBy, direction);
        return ResponseEntity.ok(new StatsResponse(stats));
    }

    @GetMapping
    public ResponseEntity<RequestSearchResponse> findRequestByFields(
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String statusCode) {

        List<AuditRequest> requests = auditRequestSearchService.findRequestsByFields(url, method, statusCode);
        return ResponseEntity.ok(new RequestSearchResponse(requests));
    }

}
