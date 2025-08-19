package com.webbee.audit_api.service.impl;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import com.webbee.audit_api.document.AuditMethod;
import com.webbee.audit_api.service.AuditMethodSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditMethodSearchServiceImpl implements AuditMethodSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public List<AuditMethod> searchMethods(String query, String level) {
        Criteria criteria = new Criteria();
        try {
            if (StringUtils.hasText(query)) {
                criteria = criteria.or(new Criteria("methodName").matches(query))
                        .or(new Criteria("args").matches(query))
                        .or(new Criteria("result").matches(query));
            }

            if (StringUtils.hasText(level)) {
                criteria = criteria.and(new Criteria("logLevel").is(level));
            }

            if (!StringUtils.hasText(query) && !StringUtils.hasText(level) &&
                    !StringUtils.hasText(level)){
                return Collections.emptyList();
            }

            Query searchQuery = new CriteriaQuery(criteria);
            SearchHits<AuditMethod> searchHits = elasticsearchOperations.search(searchQuery, AuditMethod.class);

            return searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        }catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<AuditMethod> findMethodsByFields(String methodName, String level, String eventType) {
        Criteria criteria = new Criteria();

        if (StringUtils.hasText(methodName)) {
            criteria = criteria.and(new Criteria("methodName").contains(methodName));
        }

        if (StringUtils.hasText(level)) {
            criteria = criteria.and(new Criteria("logLevel").is(level));
        }

        if (StringUtils.hasText(eventType)) {
            criteria = criteria.and(new Criteria("logType").contains(eventType));
        }

        if (!StringUtils.hasText(methodName) && !StringUtils.hasText(level) &&
                !StringUtils.hasText(eventType)){
            return Collections.emptyList();
        }

        Query searchQuery = new CriteriaQuery(criteria);
        SearchHits<AuditMethod> searchHits = elasticsearchOperations.search(searchQuery, AuditMethod.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getMethodStats(String groupBy, LocalDateTime from, LocalDateTime to) {
        String groupByField = groupByConditionals(groupBy);
        if (groupByField == null) {
            return Collections.emptyMap();
        }

        NativeQueryBuilder queryBuilder = NativeQuery.builder();

        if (from != null || to != null) {
            addLocalDateTimes(queryBuilder, from, to);
        }

        String aggregationName = "stats_agg";
        queryBuilder.withAggregation(aggregationName,
                Aggregation.of(a ->
                        a.terms(t -> t.field(groupByField)))
        );

        SearchHits<AuditMethod> searchHits =
                elasticsearchOperations.search(queryBuilder.build(), AuditMethod.class);

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

    private void addLocalDateTimes(NativeQueryBuilder queryBuilder, LocalDateTime from, LocalDateTime to) {
        queryBuilder.withQuery(q -> q.range(r -> r.date(d -> {
            d.field("timestamp");
            if (from != null) d.gte(from.format(DATE_TIME_FORMATTER));
            if (to != null)  d.lte(to.format(DATE_TIME_FORMATTER));
            return d;
        })));
    }

    private StringTermsAggregate getTerms(SearchHits<AuditMethod> searchHits) {
        AggregationsContainer<?> container = searchHits.getAggregations();
        if (container == null) return null;

        List<ElasticsearchAggregation> aggs = (List<ElasticsearchAggregation>) container.aggregations();
        if (aggs == null || aggs.isEmpty()) return null;

        ElasticsearchAggregation statsAgg = aggs.stream()
                .filter(a -> "stats_agg".equals(a.aggregation().getName()))
                .findFirst()
                .orElse(null);

        if (statsAgg == null) return null;

        StringTermsAggregate terms = statsAgg.aggregation().getAggregate().sterms();
        if (terms == null) return null;

        return terms;
    }

    private String groupByConditionals(String groupBy) {
        if (groupBy.equals("method")) {
            return "methodName.keyword";
        } else if (groupBy.equals("level")) {
            return "logLevel";
        } else return null;
    }

}
