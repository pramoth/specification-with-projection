/*
 * This annotation serves to determine if the projection should load the child property when you use distinct in a query

EntityParent {
	String name;
	EntityChild child;
}

EntityChild {
	String name;
}

When you create the query without the EntityGraph you get:

Select distinct p From EntityParent p inner join EntityChild c order by c.name

You get error because EntityChild was not returned, you need to load EntityChild

With annotation:

@LoadEntityGraph(paths="child")
interface ProjectEntityParent{
  EntityChildInfo getEntityChild();
  
  interface EntityChildInfo {
  	String name;
  }
}

You get query

Select distinct p, c From EntityParent p inner join EntityChild c order by c.name
 */
package th.co.geniustree.springdata.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author thesivis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.TYPE })
public @interface LoadEntityGraph {
  String[] paths() default {};
}

