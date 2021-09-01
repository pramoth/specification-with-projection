package th.co.geniustree.springdata.jpa.repository;

import lombok.Data;

@Data
public class ClassDocumentWithoutParent {

    private Long id;
    private String description;
    private String documentType;
    private String documentCategory;

    public ClassDocumentWithoutParent(Long id, String description, String documentType, String documentCategory) {
        this.id = id;
        this.description = description;
        this.documentType = documentType;
        this.documentCategory = documentCategory;
    }
}
