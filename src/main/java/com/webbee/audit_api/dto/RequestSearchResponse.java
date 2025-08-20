package com.webbee.audit_api.dto;

import com.webbee.audit_api.document.AuditRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RequestSearchResponse {
    private List<AuditRequest> results;
}
