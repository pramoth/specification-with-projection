package th.co.geniustree.springdata.jpa;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import th.co.geniustree.springdata.jpa.domain.Document;
import th.co.geniustree.springdata.jpa.repository.DocumentRepository;
import th.co.geniustree.springdata.jpa.specification.DocumentSpecs;

import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@DataJpaTest
@Transactional
public class SpecificationExecutorProjectionTests {
    @Autowired
    private DocumentRepository documentRepository;

    @Test
    public void findAll() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));
        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(where, DocumentRepository.DocumentWithoutParent.class, PageRequest.of(0,10));
        Assertions.assertThat(all).isNotEmpty();
        System.out.println(all.getContent());
        Assertions.assertThat(all.getContent().get(0).getDocumentType()).isEqualTo("ต้นฉบับ");
    }

    @Test
    public void findOne() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));
        Optional<DocumentRepository.DocumentWithoutParent> one = documentRepository.findOne(where, DocumentRepository.DocumentWithoutParent.class);
        Assertions.assertThat(one.get().getDocumentType()).isEqualTo("ต้นฉบับ");
    }


}
