package com.webbee.audit_api.service.impl;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.webbee.audit_api.document.AuditRequest;
import com.webbee.audit_api.service.AuditRequestSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditRequestSearchServiceImpl implements AuditRequestSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<AuditRequest> searchRequests(String query, String statusCode) {
        if (!StringUtils.hasText(query) && !StringUtils.hasText(statusCode)) {
            return Collections.emptyList();
        }

        BoolQuery.Builder boolQuery = QueryBuilders.bool();

        if (StringUtils.hasText(query)) {
            boolQuery.must(q -> q
                    .multiMatch(m -> m
                            .query(query)
                            .fields("path", "requestBody")
                    )
            );
        }

        if (StringUtils.hasText(statusCode)) {
            boolQuery.must(q -> q
                    .term(t -> t
                            .field("statusCode")
                            .value(statusCode)
                    )
            );
        }

        NativeQuery searchQuery = new NativeQuery(boolQuery.build()._toQuery());

        SearchHits<AuditRequest> hits = elasticsearchOperations.search(searchQuery, AuditRequest.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getRequestStats(String groupBy, String direction) {
        if (!StringUtils.hasText(groupBy) && !StringUtils.hasText(direction)) {
            return Collections.emptyMap();
        }

        String groupByField = groupByConditionals(groupBy);
        if (groupByField == null) {
            return Collections.emptyMap();
        }

        NativeQueryBuilder queryBuilder = NativeQuery.builder();

        String aggregationName = "stats_agg";

        if (StringUtils.hasText(direction)) {
            queryBuilder.withQuery(q -> q
                    .term(t -> t
                            .field("requestType")
                            .value(direction)
                    )
            );
        }

        queryBuilder.withAggregation(aggregationName,
                Aggregation.of(a ->
                        a.terms(t -> t.field(groupByField)))
        );

        SearchHits<AuditRequest> searchHits =
                elasticsearchOperations.search(queryBuilder.build(), AuditRequest.class);

        StringTermsAggregate terms = getTerms(searchHits);
        if (terms == null) {
            return Collections.emptyMap();
        }

        return terms.buckets().array().stream()
                .collect(Collectors.toMap(
                        b -> b.key().stringValue(),
                        StringTermsBucket::docCount
                ));
    }

    @Override
    public List<AuditRequest> findRequestsByFields(String url, String method, String statusCode) {
        if (!StringUtils.hasText(url)
                && !StringUtils.hasText(method)
                && !StringUtils.hasText(statusCode)) {
            return Collections.emptyList();
        }

        BoolQuery.Builder boolQuery = QueryBuilders.bool();
        if (StringUtils.hasText(url)) {
            boolQuery.must(q -> q
                    .match(m -> m
                            .field("path")
                            .query(url)
                    )
            );
        }

        if (StringUtils.hasText(method)) {
            boolQuery.must(q -> q
                    .term(t -> t
                            .field("method")
                            .value(method)
                    )
            );
        }

        if (StringUtils.hasText(statusCode)) {
            boolQuery.must(q -> q
                    .term(t -> t
                            .field("statusCode")
                            .value(statusCode)
                    )
            );
        }
        NativeQuery searchQuery = new NativeQuery(boolQuery.build()._toQuery());
        SearchHits<AuditRequest> searchHits = elasticsearchOperations.search(searchQuery, AuditRequest.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .toList();
    }

    private String groupByConditionals(String groupBy) {
        return switch (groupBy) {
            case "statusCode" -> "statusCode";
            case "method" -> "method";
            case "url" -> "path.keyword";
            default -> null;
        };
    }

    private StringTermsAggregate getTerms(SearchHits<AuditRequest> searchHits) {
        AggregationsContainer<?> container = searchHits.getAggregations();
        if (container == null) {
            return null;
        }

        List<ElasticsearchAggregation> aggs = (List<ElasticsearchAggregation>) container.aggregations();
        if (aggs == null || aggs.isEmpty()) {
            return null;
        }

        ElasticsearchAggregation statsAgg = aggs.stream()
                .filter(a -> "stats_agg".equals(a.aggregation().getName()))
                .findFirst()
                .orElse(null);

        if (statsAgg == null) {
            return null;
        }

        StringTermsAggregate terms = statsAgg.aggregation().getAggregate().sterms();
        if (terms == null) {
            return null;
        }

        return terms;
    }

}
