package com.webbee.audit_api.service;

import com.webbee.audit_api.document.AuditRequest;

import java.util.List;
import java.util.Map;

/**
 * Сервис для получения данные о документе AuditRequest из Elasticsearch.
 */
public interface AuditRequestSearchService {

    /**
     * {@inheritDoc}
     * Полнотекстовый поиск по HTTP-запросам
     * @return - {@code List<AuditRequest>} с результатами
     */
    List<AuditRequest> searchRequests(String query, String statusCode);

    /**
     * {@inheritDoc}
     *Агрегация запросов по статусам/методам
     * @return - {@code Map<String, Long>} со статистикой
     */
    Map<String, Long> getRequestStats(String groupBy, String direction);

    /**
     * {@inheritDoc}
     * Поиск по конкретным полям
     * @return - {@code List<AuditRequest>} с результатами
     */
    List<AuditRequest> findRequestsByFields(String url, String method, String statusCode);

}
