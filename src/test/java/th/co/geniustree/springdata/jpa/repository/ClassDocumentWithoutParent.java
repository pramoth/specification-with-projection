package th.co.geniustree.springdata.jpa.repository;

import lombok.Data;

@Data
public class ClassDocumentWithoutParent {

    private Long id;
    private String description;
    private String documentType;
    private String documentCategory;

}
