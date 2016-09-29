package th.co.geniustree.springdata.jpa.repository;

import th.co.geniustree.springdata.jpa.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by pramoth on 9/28/2016 AD.
 */
public interface DocumentRepository2  extends JpaRepository<Document,Integer> {
    public List<Document> findByParentIsNull();
}
