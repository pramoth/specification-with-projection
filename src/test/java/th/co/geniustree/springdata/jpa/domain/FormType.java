package th.co.geniustree.springdata.jpa.domain;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by neng on 22/9/2559.
 */
@Entity
@Table(name = "FORM_TYPE")
@Cacheable
public class FormType implements Serializable {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "FORM_DESC")
    private String formDesc;

    public FormType() {
    }

    public FormType(String id, String formDesc) {
        this.id = id;
        this.formDesc = formDesc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormDesc() {
        return formDesc;
    }

    public void setFormDesc(String formDesc) {
        this.formDesc = formDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormType)) return false;

        FormType formType = (FormType) o;

        return getId() != null ? getId().equals(formType.getId()) : formType.getId() == null;

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
