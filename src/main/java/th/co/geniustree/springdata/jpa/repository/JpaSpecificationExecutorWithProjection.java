package th.co.geniustree.springdata.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Created by pramoth on 9/29/2016 AD.
 */
@NoRepositoryBean
public interface JpaSpecificationExecutorWithProjection<T,ID extends Serializable> extends JpaRepository<T,ID>{
  public <R> Page<R> findAll(Specification<T> spec,Class<R> projectionClass, Pageable pageable);
}
