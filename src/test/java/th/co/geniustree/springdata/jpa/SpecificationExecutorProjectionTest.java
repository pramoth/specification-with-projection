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
public class SpecificationExecutorProjectionTest {
    @Autowired
    private DocumentRepository documentRepository;


    @Test
    public void findAll() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));
        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(where, DocumentRepository.DocumentWithoutParent.class, PageRequest.of(0,10));
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getDocumentType()).isEqualTo("ต้นฉบับ");
    }

    @Test
    public void findAll2() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));
        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(where, DocumentRepository.DocumentWithoutParent.class, PageRequest.of(0,10));
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getChild().size()).isEqualTo(1);
    }

    @Test
    public void findAll3() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));
        Page<DocumentRepository.OnlyId> all = documentRepository.findAll(where, DocumentRepository.OnlyId.class, PageRequest.of(0,10));
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void findAll4() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(24L));
        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(where, DocumentRepository.DocumentWithoutParent.class, PageRequest.of(0,10));
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getChild()).isNull();
    }

    @Test
    public void findAll5() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(24L));
        Page<DocumentRepository.OnlyParent> all = documentRepository.findAll(where, DocumentRepository.OnlyParent.class, PageRequest.of(0,10));
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getParent().getId()).isEqualTo(13L);
    }
    @Test
    public void find_single_page() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(24L));
        Page<DocumentRepository.OnlyParent> all = documentRepository.findAll(where, DocumentRepository.OnlyParent.class, PageRequest.of(0,10));
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(all.getTotalPages()).isEqualTo(1);
    }

    @Test
    public void find_all_page() {
        Specification<Document> where = Specification.where(null);
        Page<DocumentRepository.OnlyParent> all = documentRepository.findAll(where, DocumentRepository.OnlyParent.class, PageRequest.of(0,10));
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getTotalElements()).isEqualTo(24);
        Assertions.assertThat(all.getTotalPages()).isEqualTo(3);
    }

    @Test
    public void findOne() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));
        Optional<DocumentRepository.DocumentWithoutParent> one = documentRepository.findOne(where, DocumentRepository.DocumentWithoutParent.class);
        Assertions.assertThat(one.get().getDocumentType()).isEqualTo("ต้นฉบับ");
    }
    @Test
    public void findOneWithOpenProjection() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));
        Optional<DocumentRepository.OpenProjection> one = documentRepository.findOne(where, DocumentRepository.OpenProjection.class);
        Assertions.assertThat(one.get().getDescriptionString()).isEqualTo("descriptiontest");
    }

    @Test
    public void findAllWithOpenProjection() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));
        Page<DocumentRepository.OpenProjection> page = documentRepository.findAll(where, DocumentRepository.OpenProjection.class,PageRequest.of(0,10));
        Assertions.assertThat(page.getContent().get(0).getDescriptionString()).isEqualTo("descriptiontest");
    }

}
