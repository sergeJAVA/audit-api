package com.webbee.audit_api.controller;

import com.webbee.audit_api.document.AuditMethod;
import com.webbee.audit_api.dto.MethodSearchResponse;
import com.webbee.audit_api.dto.StatsResponse;
import com.webbee.audit_api.service.AuditMethodSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit/methods")
@RequiredArgsConstructor
public class AuditMethodController {

    private final AuditMethodSearchService auditMethodSearchService;

    @GetMapping("/search")
    public ResponseEntity<MethodSearchResponse> searchMethods(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String level) {

        List<AuditMethod> results = auditMethodSearchService.searchMethods(query, level);
        return ResponseEntity.ok(new MethodSearchResponse(results));
    }

    @GetMapping
    public ResponseEntity<MethodSearchResponse> findMethodsByFields(
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String eventType) {

        List<AuditMethod> results = auditMethodSearchService.findMethodsByFields(method, level, eventType);
        return ResponseEntity.ok(new MethodSearchResponse(results));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getMethodStats(
            @RequestParam String groupBy,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime to) {

        Map<String, Long> stats = auditMethodSearchService.getMethodStats(groupBy, from, to);
        return ResponseEntity.ok(new StatsResponse(stats));
    }

}
