package com.webbee.audit_api.service.impl;

import com.webbee.audit_api.document.AuditRequest;
import com.webbee.audit_api.service.AuditRequestSearchService;
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
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AuditRequestSearchServiceImplTest extends Testcontainer {

    @Autowired
    private AuditRequestSearchService auditRequestSearchService;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setUp() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(AuditRequest.class);
        if (!indexOperations.exists()) {
            indexOperations.create();
            indexOperations.putMapping();
        }


        elasticsearchOperations.save(
                new AuditRequest(
                        "1",
                        LocalDateTime.now(),
                        "Incoming",
                        "PUT",
                        "201",
                        "/contractor/save/test",
                        "requestBody: newContractor",
                        "responseBody: savedContractor"
                ),
                new AuditRequest(
                        "2",
                        LocalDateTime.now(),
                        "Incoming",
                        "POST",
                        "204",
                        "/contractor/save",
                        "requestBody: oldContractor",
                        "responseBody: savedContractor"
                ),
                new AuditRequest(
                        "3",
                        LocalDateTime.now(),
                        "Incoming",
                        "PUT",
                        "200",
                        "/contractor/save/something",
                        "requestBody: contractor",
                        "responseBody: savedContractor"
                ),
                new AuditRequest(
                        "4",
                        LocalDateTime.now(),
                        "Outcoming",
                        "DELETE",
                        "200",
                        "/contractor/delete/1",
                        "responseBody: deleted",
                        "responseBody:"
                ),
                new AuditRequest(
                        "5",
                        LocalDateTime.now(),
                        "Outcoming",
                        "DELETE",
                        "210",
                        "/contractor/delete/2",
                        "responseBody: deleted",
                        "responseBody:"
                )
        );

        indexOperations.refresh();
    }

    @AfterEach
    void tearDown() {
        elasticsearchOperations.indexOps(AuditRequest.class).delete();
    }

    @Test
    void shouldSaveAndCountDocuments() {
        Query matchAllQuery = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .build();

        long count = elasticsearchOperations.count(matchAllQuery, AuditRequest.class);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void searchRequests_SearchingByStatusCode() {
        List<AuditRequest> results = auditRequestSearchService.searchRequests(null, "201");

        assertThat(results).hasSize(1);
        AuditRequest auditRequest = results.getFirst();
        assertEquals("requestBody: newContractor", auditRequest.getRequestBody());
        assertEquals("201", auditRequest.getStatusCode());

        results = auditRequestSearchService.searchRequests(null, "404");
        assertThat(results).hasSize(0);
    }

    @Test
    void searchRequests_SearchingByURI() {
        List<AuditRequest> results = auditRequestSearchService.searchRequests("test", null);

        assertThat(results).hasSize(1);
        AuditRequest auditRequest = results.getFirst();
        assertEquals("requestBody: newContractor", auditRequest.getRequestBody());
        assertTrue(auditRequest.getPath().contains("test"));

        results = auditRequestSearchService.searchRequests("save", null);
        assertThat(results).hasSize(3);
        assertThat(results).extracting(AuditRequest::getPath).contains("/contractor/save");

        results = auditRequestSearchService.searchRequests("save", "200");
        auditRequest = results.getFirst();
        assertThat(results).hasSize(1);
        assertTrue(auditRequest.getPath().contains("save"));
        assertEquals("200", auditRequest.getStatusCode());
    }

    @Test
    void searchRequest_SearchingByRequestBody() {
        List<AuditRequest> results = auditRequestSearchService.searchRequests("deleted", null);

        assertThat(results).hasSize(2);
        AuditRequest auditRequest = results.getFirst();
        assertThat(auditRequest.getRequestBody().contains("deleted"));
        assertThat(auditRequest.getMethod().equals("DELETE"));
        assertThat(auditRequest.getMethod().equals("Outcoming"));
        assertThat(auditRequest.getStatusCode().equals("200"));

        results = auditRequestSearchService.searchRequests("newContractor", null);
        assertThat(results).hasSize(1);
        auditRequest = results.getFirst();
        assertThat(auditRequest.getRequestBody().contains("newContractor"));

        results = auditRequestSearchService.searchRequests("deleted", "210");
        assertThat(results).hasSize(1);
        auditRequest = results.getFirst();
        assertEquals("210", auditRequest.getStatusCode());
        assertTrue(auditRequest.getRequestBody().contains("deleted"));

    }

    @Test
    void getStats_ShouldGetStats_ByStatusCode() {
        Map<String, Long> stats = auditRequestSearchService.getRequestStats("statusCode", "Incoming");
        assertThat(stats.size()).isEqualTo(3);
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            assertThat(entry.getValue().equals(1L));
        }

        stats = auditRequestSearchService.getRequestStats("statusCode", "Outcoming");
        assertThat(stats.size()).isEqualTo(2);
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            assertThat(entry.getValue().equals(1L));
        }

        stats = auditRequestSearchService.getRequestStats("statusCode", null);
        assertThat(stats.size()).isEqualTo(4);
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            if (entry.getKey().equals("200")) {
                assertThat(entry.getValue().equals(2L));
            } else {
                assertThat(entry.getValue().equals(1L));
            }
        }
    }

    @Test
    void getStats_ShouldGetStats_ByMethod() {

        Map<String, Long> stats = auditRequestSearchService.getRequestStats("method", "Incoming");
        assertThat(stats.size()).isEqualTo(2);
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            if (entry.getKey().equals("PUT")) {
                assertThat(entry.getValue().equals(2L));
            } else {
                assertThat(entry.getValue().equals(1L));
            }
        }

        stats = auditRequestSearchService.getRequestStats("method", "Outcoming");
        assertThat(stats.size()).isEqualTo(1);
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            assertThat(entry.getKey().equals("DELETE"));
            assertThat(entry.getValue().equals(2L));
        }
    }

    @Test
    void getStats_ShouldGetStats_ByURL() {
        Map<String, Long> stats = auditRequestSearchService.getRequestStats("url", "Incoming");
        assertThat(stats.size()).isEqualTo(3);
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            assertThat(entry.getValue().equals(1L));
        }

        stats = auditRequestSearchService.getRequestStats("url", "Outcoming");
        assertThat(stats.size()).isEqualTo(2);
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            assertThat(entry.getValue().equals(1L));
        }
    }

    @Test
    void findRequestsByFields_ShouldFindRequests() {
        List<AuditRequest> results = auditRequestSearchService.findRequestsByFields("save", "PUT", "201");
        assertThat(results).hasSize(1);
        AuditRequest auditRequest = results.getFirst();
        assertEquals("PUT", auditRequest.getMethod());
        assertEquals("201", auditRequest.getStatusCode());
        assertTrue(auditRequest.getPath().contains("save"));

        results = auditRequestSearchService.findRequestsByFields("delete", "DELETE", "210");
        assertThat(results).hasSize(1);
        auditRequest = results.getFirst();
        assertThat(auditRequest.getPath().contains("delete"));
        assertEquals("210", auditRequest.getStatusCode());

        results = auditRequestSearchService.findRequestsByFields("save", "PUT", null);
        assertThat(results).hasSize(2);
        for (AuditRequest request : results) {
            assertThat(request.getPath().contains("/contractor/save"));
            assertTrue(request.getMethod().equals("PUT"));
        }

        results = auditRequestSearchService.findRequestsByFields("delete", null, null);
        assertThat(results).hasSize(2);
        for (AuditRequest request : results) {
            assertThat(request.getPath().contains("/contractor/delete"));
            assertTrue(request.getMethod().equals("DELETE"));
        }
    }

}