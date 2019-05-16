package org.springframework.data.repository.query;

import org.springframework.data.projection.ProjectionFactory;

public class ReturnTypeWarpper {
    public static ReturnedType of(Class<?> returnedType, Class<?> domainType, ProjectionFactory factory){
       return  ReturnedType.of(returnedType, domainType,factory);
    }
}
