package th.co.geniustree.springdata.jpa.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import th.co.geniustree.springdata.jpa.domain.Document;

import java.util.List;

/**
 * Created by pramoth on 9/28/2016 AD.
 */
public interface DocumentRepository extends JpaRepository<Document,Long>,JpaSpecificationExecutorWithProjection<Document, Long> {

    List<DocumentWithoutParent> findByParentIsNull();

    interface DocumentWithoutParent{
        Long getId();
        String getDescription();
        String getDocumentType();
        String getDocumentCategory();
        List<DocumentWithoutParent> getChild();
    }

    interface DocumentWithoutChild{
        Long getId();
        String getDescription();
        String getDocumentType();
        String getDocumentCategory();
    }

    interface OnlyId{
        Long getId();
    }

    interface OnlyParent extends OnlyId{
        OnlyId getParent();
    }

    interface OpenProjection extends OnlyId{
        @Value("#{target.description}")
        String getDescriptionString();
    }

}
