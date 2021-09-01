package th.co.geniustree.springdata.jpa.repository;

import java.util.List;
import javax.persistence.Id;
import lombok.Data;
import th.co.geniustree.springdata.jpa.annotation.Load;

@Data
public class ClassDocumentWithoutParentList {

    @Id
    private Long id;
    private String description;
    private String documentType;
    private String documentCategory;
    
    @Load("child")
    List<ChildDTO> child;
    
    public interface ChildDTO {
        Long getId();
        String getDescription();
        String getDocumentType();
        String getDocumentCategory();
    }

    public ClassDocumentWithoutParentList(Long id, String description, String documentType, String documentCategory) {
        this.id = id;
        this.description = description;
        this.documentType = documentType;
        this.documentCategory = documentCategory;
    }
}
