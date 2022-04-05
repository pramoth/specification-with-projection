package th.co.geniustree.springdata.jpa;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import th.co.geniustree.springdata.jpa.domain.Document;
import th.co.geniustree.springdata.jpa.repository.DocumentRepository;
import th.co.geniustree.springdata.jpa.specification.DocumentSpecs;

import java.util.List;
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

        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class, PageRequest.of(0, 10)
        );
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getDocumentType()).isEqualTo("ต้นฉบับ");

        List<DocumentRepository.DocumentWithoutParent> allList = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class
        );
        Assertions.assertThat(allList).isNotEmpty();
        Assertions.assertThat(allList.get(0).getDocumentType()).isEqualTo("ต้นฉบับ");

        Sort sortByDescription = Sort.by(Sort.Order.desc("description"));
        List<DocumentRepository.DocumentWithoutParent> allListSorted = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class, sortByDescription
        );
        Assertions.assertThat(allListSorted).isNotEmpty();
        Assertions.assertThat(allListSorted.get(0).getDocumentType()).isEqualTo("ต้นฉบับ");
    }

    @Test
    public void findAll2() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));

        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class, PageRequest.of(0, 10)
        );
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getChild().size()).isEqualTo(1);

        List<DocumentRepository.DocumentWithoutParent> allList = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class
        );
        Assertions.assertThat(allList).isNotEmpty();
        Assertions.assertThat(allList.get(0).getChild().size()).isEqualTo(1);

        Sort sortByDescription = Sort.by(Sort.Order.desc("description"));
        List<DocumentRepository.DocumentWithoutParent> allListSorted = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class, sortByDescription
        );
        Assertions.assertThat(allListSorted).isNotEmpty();
        Assertions.assertThat(allListSorted.get(0).getChild().size()).isEqualTo(1);
    }

    @Test
    public void findAll3() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(1L));

        Page<DocumentRepository.OnlyId> all = documentRepository.findAll(
                where, DocumentRepository.OnlyId.class, PageRequest.of(0, 10)
        );
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getId()).isEqualTo(1L);

        List<DocumentRepository.OnlyId> allList = documentRepository.findAll(
                where, DocumentRepository.OnlyId.class
        );
        Assertions.assertThat(allList).isNotEmpty();
        Assertions.assertThat(allList.get(0).getId()).isEqualTo(1L);

        Sort sortByDescription = Sort.by(Sort.Order.desc("description"));
        List<DocumentRepository.OnlyId> allListSorted = documentRepository.findAll(
                where, DocumentRepository.OnlyId.class, sortByDescription
        );
        Assertions.assertThat(allListSorted).isNotEmpty();
        Assertions.assertThat(allListSorted.get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void findAll4() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(24L));

        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class, PageRequest.of(0, 10)
        );
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getChild()).isNull();

        List<DocumentRepository.DocumentWithoutParent> allList = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class
        );
        Assertions.assertThat(allList).isNotEmpty();
        Assertions.assertThat(allList.get(0).getChild()).isNull();

        Sort sortByDescription = Sort.by(Sort.Order.desc("description"));
        List<DocumentRepository.DocumentWithoutParent> allListSorted = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutParent.class, sortByDescription
        );
        Assertions.assertThat(allListSorted).isNotEmpty();
        Assertions.assertThat(allListSorted.get(0).getChild()).isNull();
    }

    @Test
    public void findAll5() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(24L));

        Page<DocumentRepository.OnlyParent> all = documentRepository.findAll(
                where, DocumentRepository.OnlyParent.class, PageRequest.of(0, 10)
        );
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getParent().getId()).isEqualTo(13L);

        List<DocumentRepository.OnlyParent> allList = documentRepository.findAll(
                where, DocumentRepository.OnlyParent.class
        );
        Assertions.assertThat(allList).isNotEmpty();
        Assertions.assertThat(allList.get(0).getParent().getId()).isEqualTo(13L);

        Sort sortByDescription = Sort.by(Sort.Order.desc("description"));
        List<DocumentRepository.OnlyParent> allListSorted = documentRepository.findAll(
                where, DocumentRepository.OnlyParent.class, sortByDescription
        );
        Assertions.assertThat(allListSorted).isNotEmpty();
        Assertions.assertThat(allListSorted.get(0).getParent().getId()).isEqualTo(13L);
    }

    @Test
    public void findAllListUnsorted() {
        Specification<Document> where = Specification.where(DocumentSpecs.descriptionLike("description1%"));

        List<DocumentRepository.DocumentWithoutChild> all = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutChild.class
        );
        Assertions.assertThat(all)
                .hasSize(3)
                .extracting("description")
                .containsExactlyInAnyOrder("description10", "description11", "description12");
    }

    @Test
    public void findAllListSorted() {
        Specification<Document> where = Specification.where(DocumentSpecs.descriptionLike("description1%"));

        Sort sortByDescription = Sort.by(Sort.Order.desc("description"));
        List<DocumentRepository.DocumentWithoutChild> all = documentRepository.findAll(
                where, DocumentRepository.DocumentWithoutChild.class, sortByDescription
        );
        Assertions.assertThat(all)
                .hasSize(3)
                .extracting("description")
                .containsExactly("description12", "description11", "description10");
    }

    @Test
    public void findSinglePage() {
        Specification<Document> where = Specification.where(DocumentSpecs.idEq(24L));
        Page<DocumentRepository.OnlyParent> all = documentRepository.findAll(where, DocumentRepository.OnlyParent.class, PageRequest.of(0, 10));
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(all.getTotalPages()).isEqualTo(1);
    }

    @Test
    public void findAllPage() {
        Specification<Document> where = Specification.where(null);
        Page<DocumentRepository.OnlyParent> all = documentRepository.findAll(where, DocumentRepository.OnlyParent.class, PageRequest.of(0, 10));
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
    public void findBydId() {
        Optional<DocumentRepository.DocumentWithoutParent> one = documentRepository.findById(1L, DocumentRepository.DocumentWithoutParent.class);
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
        Page<DocumentRepository.OpenProjection> page = documentRepository.findAll(where, DocumentRepository.OpenProjection.class, PageRequest.of(0, 10));
        Assertions.assertThat(page.getContent().get(0).getDescriptionString()).isEqualTo("descriptiontest");
    }

}
