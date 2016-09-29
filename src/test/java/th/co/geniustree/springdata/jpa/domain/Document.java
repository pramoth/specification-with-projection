package th.co.geniustree.springdata.jpa.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Created by best on 7/9/2559.
 */
@Entity
@Table(name = "DOCUMENT")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "DocumentSeq", sequenceName = "DOCUMENT_SEQ", allocationSize = 1)
    @GeneratedValue(generator = "DocumentSeq", strategy = GenerationType.SEQUENCE)
    @Column(name = "ID")
    private Long id;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TYPE")
    private String documentType;

    @Column(name = "CATEGORY")
    private String documentCategory;

    @OneToMany(mappedBy = "parent")
    private List<Document> child;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PARENT_ID")
    private Document parent;

    @Column(name = "FLAG_HAS_SUB")
    private Boolean flagHasSubDocument = Boolean.FALSE;

    @ManyToOne
    @JoinColumn(name = "FORM_TYPE_ID", nullable = false)
    private FormType formType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentCategory() {
        return documentCategory;
    }

    public void setDocumentCategory(String documentCategory) {
        this.documentCategory = documentCategory;
    }

    public List<Document> getChild() {
        return child;
    }

    public void setChild(List<Document> child) {
        this.child = child;
    }

    public Document getParent() {
        return parent;
    }

    public void setParent(Document parent) {
        this.parent = parent;
    }

    public Boolean getFlagHasSubDocument() {
        return flagHasSubDocument;
    }

    public void setFlagHasSubDocument(Boolean flagHasSubDocument) {
        this.flagHasSubDocument = flagHasSubDocument;
    }

    public FormType getFormType() {
        return formType;
    }

    public void setFormType(FormType formType) {
        this.formType = formType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
