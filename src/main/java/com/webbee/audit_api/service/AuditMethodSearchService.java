package com.webbee.audit_api.service;

import com.webbee.audit_api.document.AuditMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Сервис для получения данные о документе AuditMethod из Elasticsearch.
 */
public interface AuditMethodSearchService {

    /**
     * {@inheritDoc}
     * Полнотекстовый поиск по логам методов
     * @return - {@code List<AuditMethod>} с результатами
     */
    List<AuditMethod> searchMethods(String query, String level);

    /**
     * {@inheritDoc}
     * Поиск по конкретным полям
     * @return - {@code List<AuditMethod>} с результатами
     */
    List<AuditMethod> findMethodsByFields(String methodName, String level, String eventType);

    /**
     * {@inheritDoc}
     * Агрегация логов по уровням/методам
     * @return - {@code Map<String, Long>} со статистикой
     */
    Map<String, Long> getMethodStats(String groupBy, LocalDateTime from, LocalDateTime to);

}
