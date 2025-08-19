package com.webbee.audit_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class StatsResponse {
    private Map<String, Long> stats;
}
