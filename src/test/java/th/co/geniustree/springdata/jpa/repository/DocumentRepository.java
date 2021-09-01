package th.co.geniustree.springdata.jpa.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import th.co.geniustree.springdata.jpa.domain.Document;

import java.util.List;
import th.co.geniustree.springdata.jpa.annotation.FieldProperty;

/**
 * Created by pramoth on 9/28/2016 AD.
 */
public interface DocumentRepository extends JpaRepository<Document,Long>,JpaSpecificationExecutorWithProjection<Document, Long> {
    public List<DocumentWithoutParent> findByParentIsNull();

    public static interface DocumentWithoutParent{
        Long getId();
        String getDescription();
        String getDocumentType();
        String getDocumentCategory();
        List<DocumentWithoutParent> getChild();
    }
    public static interface OnlyId{
        Long getId();
    }

    public static interface OnlyParent extends OnlyId{
        OnlyId getParent();
    }

    public static interface OpenProjection extends OnlyId{
        @FieldProperty(path = "description")
        @Value("#{target.description}")
        String getDescriptionString();
    }
}
