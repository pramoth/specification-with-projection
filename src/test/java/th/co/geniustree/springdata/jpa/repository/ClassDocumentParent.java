package th.co.geniustree.springdata.jpa.repository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ClassDocumentParent {

    private Long id;
    private String description;
    private String documentType;
    private String documentCategory;

    private ParentDTO parent;

    @Data
    @AllArgsConstructor
    public class ParentDTO {

        private Long id;
        private String description;
    }

    public ClassDocumentParent(Long id, Long parent_id, String parent_description, String description, String documentType, String documentCategory) {
        this.id = id;
        this.description = description;
        this.documentType = documentType;
        this.documentCategory = documentCategory;
        this.parent = new ParentDTO(parent_id, parent_description);
    }
}
