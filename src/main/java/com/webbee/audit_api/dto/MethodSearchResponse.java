package com.webbee.audit_api.dto;

import com.webbee.audit_api.document.AuditMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MethodSearchResponse {
    private List<AuditMethod> results;
}
