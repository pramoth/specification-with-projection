package th.co.geniustree.springdata.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Created by pramoth on 9/29/2016 AD.
 */
@NoRepositoryBean
public interface JpaSpecificationExecutorWithProjection<T, ID> {

    <R> Optional<R> findById(ID id, Class<R> projectionClass);

    <R> Optional<R> findOne(Specification<T> spec, Class<R> projectionClass);

    <R> List<R> findAll(Specification<T> spec, Class<R> projectionClass);

    <R> List<R> findAll(Specification<T> spec, Class<R> projectionClass, Sort sort);

    <R> Page<R> findAll(Specification<T> spec, Class<R> projectionClass, Pageable pageable);

    /**
     * Use Spring Data Annotation instead of manually provide EntityGraph.
     *
     * @param spec
     * @param projectionType
     * @param namedEntityGraph
     * @param type
     * @param pageable
     * @param <R>
     * @return
     */
    @Deprecated
    <R> Page<R> findAll(Specification<T> spec, Class<R> projectionType, String namedEntityGraph, EntityGraph.EntityGraphType type, Pageable pageable);

    /**
     * Use Spring Data Annotation instead of manually provide EntityGraph.
     *
     * @param spec
     * @param projectionClass
     * @param dynamicEntityGraph
     * @param pageable
     * @param <R>
     * @return
     */
    @Deprecated
    <R> Page<R> findAll(Specification<T> spec, Class<R> projectionClass, JpaEntityGraph dynamicEntityGraph, Pageable pageable);
}
