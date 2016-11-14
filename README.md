# specification-with-projection
Support Projections with `JpaSpecificationExecutor.findAll(Specification,Pageable)` for Spring Data JPA

## How to use
* add annotation `@EnableJpaRepositories(repositoryBaseClass = JpaSpecificationExecutorWithProjectionImpl.class)` on Application class (Spring Boot)
* create your repository and extends `JpaSpecificationExecutorWithProjection`
```java
public interface DocumentRepository extends JpaRepository<Document,Integer>,JpaSpecificationExecutorWithProjection<Document,Integer> {
    /**
    * projection interface
    **/
    public static interface DocumentWithoutParent{
        Long getId();
        String getDescription();
        String getDocumentType();
        String getDocumentCategory();
        List<DocumentWithoutParent> getChild();
    }
}
```
* use it

  ```java
      @Test
    public void specificationWithProjection() {
        Specifications<Document> where = Specifications.where(DocumentSpecs.idEq(1L));
        Page<DocumentRepository.DocumentWithoutParent> all = documentRepository.findAll(where, DocumentRepository.DocumentWithoutParent.class, new PageRequest(0,10));
        Assertions.assertThat(all).isNotEmpty();
    }
  ```
  * version 1.0.3 RELEASE add support for @NamedEntityGraph and AD-HOC entity graph (via JpaEntityGraph)
http://docs.spring.io/spring-data/jpa/docs/1.10.4.RELEASE/reference/html/#jpa.entity-graph
```java
JpaEntityGraph jpaEntityGraph = new JpaEntityGraph(
    "birth.sistRecvTm",
    EntityGraph.EntityGraphType.FETCH,
    new String[]{"sistRecvTm","birth","transfer","merger"}
  );
  BirthWithoutChild birth = birthRepository.findAll(createSpecBirth(searchData, type.toUpperCase()), BirthWithoutChild.class,jpaEntityGraph,pageable);
```
