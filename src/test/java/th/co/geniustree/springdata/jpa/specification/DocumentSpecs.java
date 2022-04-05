package th.co.geniustree.springdata.jpa.specification;

import org.springframework.data.jpa.domain.Specification;
import th.co.geniustree.springdata.jpa.domain.Document;
import th.co.geniustree.springdata.jpa.domain.Document_;

/**
 * Created by pramoth on 9/29/2016 AD.
 */
public class DocumentSpecs {

    public static Specification<Document> idEq(Long id) {
        return (root, query, cb) -> cb.equal(root.get(Document_.id), id);
    }

    public static Specification<Document> descriptionLike(String descriptionLike) {
        return (root, query, cb) -> cb.like(root.get(Document_.description), descriptionLike);
    }

}
