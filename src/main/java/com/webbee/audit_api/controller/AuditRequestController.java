package com.webbee.audit_api.controller;

import com.webbee.audit_api.document.AuditRequest;
import com.webbee.audit_api.dto.RequestSearchResponse;
import com.webbee.audit_api.dto.StatsResponse;
import com.webbee.audit_api.service.AuditRequestSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Audit Requests", description = "API для работы с аудитом запросов")
public class AuditRequestController {

    private final AuditRequestSearchService auditRequestSearchService;

    @Operation(summary = "Поиск запросов", description = "Выполняет полнотекстовый поиск среди запросов аудита," +
            " с возможностью фильтрации по коду статуса.")
    @ApiResponse(responseCode = "200", description = "Успешный ответ",
            content = @Content(schema = @Schema(implementation = RequestSearchResponse.class)))
    @GetMapping("/search")
    public ResponseEntity<RequestSearchResponse> searchRequests(
            @Parameter(description = "Текст для полнотекстового поиска")
            @RequestParam(required = false) String query,

            @Parameter(description = "Фильтрация по коду статуса (например, 200, 404)")
            @RequestParam(required = false) String statusCode) {

        List<AuditRequest> requests = auditRequestSearchService.searchRequests(query, statusCode);
        return ResponseEntity.ok(new RequestSearchResponse(requests));
    }

    @Operation(summary = "Статистика по запросам", description = "Возвращает агрегированные данные по запросам аудита.")
    @ApiResponse(responseCode = "200", description = "Успешный ответ со статистикой",
            content = @Content(schema = @Schema(implementation = StatsResponse.class)))
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getRequestStats(
            @Parameter(description = "Поле для группировки данных (например, url, statusCode, method)")
            @RequestParam(required = false) String groupBy,

            @Parameter(description = "Направление сортировки (например, Incoming, Outcoming)")
            @RequestParam(required = false) String direction) {

        Map<String, Long> stats = auditRequestSearchService.getRequestStats(groupBy, direction);
        return ResponseEntity.ok(new StatsResponse(stats));
    }

    @Operation(summary = "Поиск по полям", description = "Поиск запросов по конкретным полям," +
            " таким как URL, метод HTTP и код статуса.")
    @ApiResponse(responseCode = "200", description = "Успешный ответ",
            content = @Content(schema = @Schema(implementation = RequestSearchResponse.class)))
    @GetMapping
    public ResponseEntity<RequestSearchResponse> findRequestByFields(
            @Parameter(description = "URL запроса")
            @RequestParam(required = false) String url,

            @Parameter(description = "Метод HTTP (например, GET, POST)")
            @RequestParam(required = false) String method,

            @Parameter(description = "Код статуса ответа")
            @RequestParam(required = false) String statusCode) {

        List<AuditRequest> requests = auditRequestSearchService.findRequestsByFields(url, method, statusCode);
        return ResponseEntity.ok(new RequestSearchResponse(requests));
    }

}
