package com.webbee.audit_api.service.impl;

import com.webbee.audit_api.document.AuditMethod;
import com.webbee.audit_api.service.AuditMethodSearchService;
import com.webbee.audit_api.testcontainer.Testcontainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AuditMethodSearchServiceImplTest extends Testcontainer {

    @Autowired
    private AuditMethodSearchService auditMethodSearchService;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private LocalDateTime saveContractorTime;
    private LocalDateTime deleteContractorTime;

    @BeforeEach
    void setUp() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(AuditMethod.class);
        if (!indexOperations.exists()) {
            indexOperations.create();
            indexOperations.putMapping();
        }

        saveContractorTime = LocalDateTime.now();
        deleteContractorTime = LocalDateTime.now().plusDays(2);

        elasticsearchOperations.save(
                new AuditMethod("1",
                        saveContractorTime,
                        "INFO",
                        "cor-1",
                        "saveContractor",
                        "args:Contractor",
                        "START",
                        "",
                        ""),
                new AuditMethod("2",
                        saveContractorTime.plusDays(1),
                        "INFO",
                        "cor-1",
                        "saveContractor",
                        "",
                        "END",
                        "result:ok",
                        ""),
                new AuditMethod("3",
                        deleteContractorTime,
                        "DEBUG",
                        "cor-2",
                        "deleteContractor",
                        "arg:1",
                        "START",
                        "",
                        ""),
                new AuditMethod("4",
                        deleteContractorTime.plusDays(1),
                        "DEBUG",
                        "cor-2",
                        "deleteContractor",
                        "",
                        "END",
                        "result:deleted",
                        "")
        );

        indexOperations.refresh();
    }

    @AfterEach
    void tearDown() {
        elasticsearchOperations.indexOps(AuditMethod.class).delete();
    }

    @Test
    void shouldSaveAndCountDocuments() {
        Query matchAllQuery = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .build();

        long count = elasticsearchOperations.count(matchAllQuery, AuditMethod.class);

        assertThat(count).isEqualTo(4L);
    }

    @Test
    void searchMethods_ShouldFindOnlyDeleteMethods_ByMethodName() {
        List<AuditMethod> results = auditMethodSearchService.searchMethods("deleteContractor", null);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(AuditMethod::getMethodName).contains("deleteContractor");
        assertThat(results).extracting(AuditMethod::getCorrelationId).contains("cor-2");
        assertTrue(results.getFirst().getId().equals("3"));
        assertTrue(results.getLast().getId().equals("4"));
    }

    @Test
    void searchMethods_ShouldFindOnlyDeleteMethods_ByLogLevel() {
        List<AuditMethod> results = auditMethodSearchService.searchMethods("", "DEBUG");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(AuditMethod::getMethodName).contains("deleteContractor");
        assertThat(results).extracting(AuditMethod::getCorrelationId).contains("cor-2");
        assertTrue(results.getFirst().getId().equals("3"));
        assertTrue(results.getLast().getId().equals("4"));
    }

    @Test
    void searchMethods_ShouldFindNothing() {
        List<AuditMethod> results = auditMethodSearchService.searchMethods("", "ERROR");
        assertThat(results).hasSize(0);
    }

    @Test
    void getMethodStats_GroupByLevel() {
        Map<String, Long> stats = auditMethodSearchService.getMethodStats("level", null, null);

        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            if (entry.getKey().equals("DEBUG")) {
                assertThat(entry.getValue().equals(2L));
            }
            if (entry.getKey().equals("INFO")) {
                assertThat(entry.getValue().equals(2L));
            }
        }
    }

    @Test
    void getMethodStats_GroupByMethod() {
        Map<String, Long> stats = auditMethodSearchService.getMethodStats("method", null, null);

        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            if (entry.getKey().equals("saveContractor")) {
                assertThat(entry.getValue().equals(2L));
            }
            if (entry.getKey().equals("deleteContractor")) {
                assertThat(entry.getValue().equals(2L));
            }
        }
    }

    @Test
    void getMethodStats_GroupBy_WithTime() {
        Map<String, Long> stats = auditMethodSearchService
                .getMethodStats("method", deleteContractorTime, null);

        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            if (entry.getKey().equals("saveContractor")) {
                assertThat(entry.getValue().equals(0L));
            }
            if (entry.getKey().equals("deleteContractor")) {
                assertThat(entry.getValue().equals(2L));
            }
        }

        stats = auditMethodSearchService
                .getMethodStats("method", null, saveContractorTime.plusDays(2));
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            if (entry.getKey().equals("saveContractor")) {
                assertThat(entry.getValue().equals(2L));
            }
            if (entry.getKey().equals("deleteContractor")) {
                assertThat(entry.getValue().equals(1L));
            }
        }

        stats = auditMethodSearchService
                .getMethodStats("method", saveContractorTime, saveContractorTime.plusDays(2));
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            if (entry.getKey().equals("saveContractor")) {
                assertThat(entry.getValue().equals(1L));
            }
            if (entry.getKey().equals("deleteContractor")) {
                assertThat(entry.getValue().equals(1L));
            }
        }

        stats = auditMethodSearchService
                .getMethodStats("level", saveContractorTime, saveContractorTime.plusDays(2));
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            if (entry.getKey().equals("INFO")) {
                assertThat(entry.getValue().equals(1L));
            }
            if (entry.getKey().equals("DEBUG")) {
                assertThat(entry.getValue().equals(1L));
            }
        }

        stats = auditMethodSearchService.getMethodStats("level", saveContractorTime.plusMonths(1), null);
        assertThat(stats.isEmpty());
    }

    @Test
    void findMethodsByFields_ByMethodName() {
        List<AuditMethod> results = auditMethodSearchService
                .findMethodsByFields("save", null, null);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(AuditMethod::getMethodName).contains("saveContractor");
    }

    @Test
    void findMethodsByFields_ByLevel() {
        List<AuditMethod> results = auditMethodSearchService
                .findMethodsByFields(null, "INFO", null);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(AuditMethod::getMethodName).contains("saveContractor");
    }

    @Test
    void findMethodsByFields_ByEventType() {
        List<AuditMethod> results = auditMethodSearchService
                .findMethodsByFields(null, null, "START");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(AuditMethod::getMethodName).contains("saveContractor", "deleteContractor");

        results = auditMethodSearchService
                .findMethodsByFields(null, null, "END");
        assertThat(results).hasSize(2);
        assertThat(results).extracting(AuditMethod::getMethodName).contains("saveContractor", "deleteContractor");
    }

    @Test
    void findMethodsByFields_AllParameters() {
        List<AuditMethod> results = auditMethodSearchService
                .findMethodsByFields("save", "INFO", "START");

        assertThat(results).hasSize(1);
        AuditMethod auditMethod = results.getFirst();
        assertEquals(auditMethod.getId(), "1");
        assertEquals(auditMethod.getArgs(), "args:Contractor");
        assertEquals(auditMethod.getMethodName(), "saveContractor");
        assertEquals(auditMethod.getLogLevel(), "INFO");
        assertEquals(auditMethod.getLogType(), "START");

        results = auditMethodSearchService
                .findMethodsByFields("delete", "DEBUG", "END");
        assertThat(results).hasSize(1);
        auditMethod = results.getFirst();
        assertEquals(auditMethod.getId(), "4");
        assertEquals(auditMethod.getMethodName(), "deleteContractor");
        assertEquals(auditMethod.getLogLevel(), "DEBUG");
        assertEquals(auditMethod.getLogType(), "END");

        results = auditMethodSearchService
                .findMethodsByFields("hello", "DEBUG", "END");
        assertThat(results).hasSize(0);

    }

}