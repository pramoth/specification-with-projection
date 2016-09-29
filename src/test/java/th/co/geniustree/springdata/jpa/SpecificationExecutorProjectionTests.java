package th.co.geniustree.springdata.jpa;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.test.context.junit4.SpringRunner;
import th.co.geniustree.springdata.jpa.domain.Document;
import th.co.geniustree.springdata.jpa.repository.DocumentRepository;
import th.co.geniustree.springdata.jpa.specification.DocumentSpecs;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,classes = DemoApplication.class)
public class SpecificationExecutorProjectionTests {
    @Autowired
    private DocumentRepository documentRepository;

    @Test
    public void specificationWithProjection() {
        Specifications<Document> where = Specifications.where(DocumentSpecs.idEq(1L));
        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(where, DocumentRepository.DocumentWithoutParent.class, null);
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.getContent().get(0).getDocumentType()).isEqualTo("ต้นฉบับ");
    }


}
