package th.co.geniustree.springdata.jpa.repository.support;

import th.co.geniustree.springdata.jpa.repository.JpaSpecificationExecutorWithProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.Serializable;

/**
 * Created by pramoth on 9/29/2016 AD.
 */
public class JpaSpecificationExecutorWithProjectionImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements JpaSpecificationExecutorWithProjection<T> {
    private final EntityManager entityManager;
    private final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

    public JpaSpecificationExecutorWithProjectionImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public <R> Page<R> findAll(Specification<T> spec, Class<R> projectionType, Pageable pageable) {
        TypedQuery<T> query = getQuery(spec, pageable);
        Page<T> result = (pageable == null) ? new PageImpl<>(query.getResultList()) : readPage(query, getDomainClass(), pageable, spec);
        return result.map(item -> projectionFactory.createProjection(projectionType, item));
    }
}
