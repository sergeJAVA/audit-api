package com.webbee.audit_api.controller;

import com.webbee.audit_api.document.AuditMethod;
import com.webbee.audit_api.dto.MethodSearchResponse;
import com.webbee.audit_api.dto.StatsResponse;
import com.webbee.audit_api.service.AuditMethodSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Audit Methods", description = "API для работы с аудитом методов")
public class AuditMethodController {

    private final AuditMethodSearchService auditMethodSearchService;

    @Operation(summary = "Поиск методов",
            description = "Полнотекстовый поиск среди методов аудита," +
            " с возможностью фильтрации по уровню.")
    @ApiResponse(responseCode = "200", description = "Успешный ответ",
            content = @Content(schema = @Schema(implementation = MethodSearchResponse.class)))
    @GetMapping("/search")
    public ResponseEntity<MethodSearchResponse> searchMethods(
            @Parameter(description = "Текст для поиска")
            @RequestParam(required = false) String query,

            @Parameter(description = "Фильтрация по уровню")
            @RequestParam(required = false) String level) {

        List<AuditMethod> results = auditMethodSearchService.searchMethods(query, level);
        return ResponseEntity.ok(new MethodSearchResponse(results));
    }

    @Operation(summary = "Поиск по полям",
            description = "Поиск методов по конкретным полям, таким как имя метода, уровень и тип события.")
    @ApiResponse(responseCode = "200", description = "Успешный ответ",
            content = @Content(schema = @Schema(implementation = MethodSearchResponse.class)))
    @GetMapping
    public ResponseEntity<MethodSearchResponse> findMethodsByFields(
            @Parameter(description = "Имя метода")
            @RequestParam(required = false) String method,

            @Parameter(description = "Уровень логирования")
            @RequestParam(required = false) String level,

            @Parameter(description = "Тип события")
            @RequestParam(required = false) String eventType) {

        List<AuditMethod> results = auditMethodSearchService.findMethodsByFields(method, level, eventType);
        return ResponseEntity.ok(new MethodSearchResponse(results));
    }

    @Operation(summary = "Статистика по методам", description = "Возвращает агрегированные данные по методам.")
    @ApiResponse(responseCode = "200", description = "Успешный ответ со статистикой",
            content = @Content(schema = @Schema(implementation = StatsResponse.class)))
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getMethodStats(
            @Parameter(description = "Поле для группировки данных (например, level или method). Поле обязательное.")
            @RequestParam String groupBy,

            @Parameter(description = "Начальная дата и время для фильтрации (в формате 'yyyy-MM-dd HH:mm:ss.SSS')")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime from,

            @Parameter(description = "Конечная дата и время для фильтрации (в формате 'yyyy-MM-dd HH:mm:ss.SSS')")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime to) {

        Map<String, Long> stats = auditMethodSearchService.getMethodStats(groupBy, from, to);
        return ResponseEntity.ok(new StatsResponse(stats));
    }

}
