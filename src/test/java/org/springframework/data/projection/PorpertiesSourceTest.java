package org.springframework.data.projection;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import th.co.geniustree.springdata.jpa.repository.DocumentRepository;

public class PorpertiesSourceTest {
    @Test
    public void test() {
        DefaultProjectionInformation projectionInformation = new DefaultProjectionInformation(DocumentRepository.DocumentWithoutParent.class);
        projectionInformation.getInputProperties().forEach(e -> {
            System.out.println(e.getName());
        });
        Assertions.assertThat(projectionInformation.isClosed()).isTrue();
    }
}
