package th.co.geniustree.springdata.jpa;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import th.co.geniustree.springdata.jpa.domain.Document;
import th.co.geniustree.springdata.jpa.repository.DocumentRepository;
import th.co.geniustree.springdata.jpa.repository.DocumentRepository2;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,classes = DemoApplication.class)
public class DemoApplicationTests {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentRepository2 documentRepository2;

    /**
     * Hibernate generate incorrect sql when use projection in query method.
     */
    @Test
    public void findWithProjection() {
        List<DocumentRepository.DocumentWithoutParent> byParentIsNull = documentRepository.findByParentIsNull();
        Assertions.assertThat(byParentIsNull).isNotEmpty();
    }

    /**
     * Hibernate generate correct sql when not use projection.
     */
    @Test
    public void findWithOutProjection() {
        List<Document> byParentIsNull = documentRepository2.findByParentIsNull();
        Assertions.assertThat(byParentIsNull).isNotEmpty();
    }

}
