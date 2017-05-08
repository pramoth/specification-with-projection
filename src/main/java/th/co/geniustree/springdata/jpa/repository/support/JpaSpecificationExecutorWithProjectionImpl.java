package th.co.geniustree.springdata.jpa.repository.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.Jpa21Utils;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import th.co.geniustree.springdata.jpa.repository.JpaSpecificationExecutorWithProjection;

/**
 * Created by pramoth on 9/29/2016 AD.
 */
public class JpaSpecificationExecutorWithProjectionImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements JpaSpecificationExecutorWithProjection<T> {

    private static final Logger log = LoggerFactory.getLogger(JpaSpecificationExecutorWithProjectionImpl.class);

    private final EntityManager entityManager;

    private final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

    private final JpaEntityInformation entityInformation;

    public JpaSpecificationExecutorWithProjectionImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    @Override
    public <R> Page<R> findAll(Specification<T> spec, Class<R> projectionType, Pageable pageable) {
        TypedQuery<T> query = getQuery(spec, pageable);
        return readPageWithProjection(spec, projectionType, pageable, query);
    }

    @Override
    public <R> Page<R> findAll(Specification<T> spec, Class<R> projectionType, String namedEntityGraph, org.springframework.data.jpa.repository.EntityGraph.EntityGraphType type, Pageable pageable) {
        EntityGraph<?> entityGraph = this.entityManager.getEntityGraph(namedEntityGraph);
        if (entityGraph == null) {
            throw new IllegalArgumentException("Not found named entity graph -> " + namedEntityGraph);
        }
        TypedQuery<T> query = getQuery(spec, pageable);
        query.setHint(type.getKey(), entityGraph);
        return readPageWithProjection(spec, projectionType, pageable, query);
    }

    @Override
    public <R> Page<R> findAll(Specification<T> spec, Class<R> projectionType, JpaEntityGraph dynamicEntityGraph, Pageable pageable) {
        TypedQuery<T> query = getQuery(spec, pageable);
        Map<String, Object> entityGraphHints = new HashMap<String, Object>();
        entityGraphHints.putAll(Jpa21Utils.tryGetFetchGraphHints(this.entityManager, dynamicEntityGraph, getDomainClass()));
        applyEntityGraphQueryHints(query, entityGraphHints);
        return readPageWithProjection(spec, projectionType, pageable, query);
    }

    private <R> Page<R> readPageWithProjection(Specification<T> spec, Class<R> projectionType, Pageable pageable, TypedQuery<T> query) {
        if (log.isDebugEnabled()) {
            query.getHints().forEach((key, value) -> log.info("apply query hints -> {} : {}", key, value));
        }
        Page<T> result = (pageable == null) ? new PageImpl<>(query.getResultList()) : readPage(query, getDomainClass(), pageable, spec);
        return result.map(item -> projectionFactory.createProjection(projectionType, item));
    }

    private void applyEntityGraphQueryHints(Query query, Map<String, Object> hints) {
        for (Map.Entry<String, Object> hint : hints.entrySet()) {
            query.setHint(hint.getKey(), hint.getValue());
        }
    }
}
