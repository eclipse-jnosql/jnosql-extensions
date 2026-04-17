package org.eclipse.jnosql.spring.boot.autoconfigure;

import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.mapping.core.config.MappingConfigurations;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(JNoSQLDatabaseProviderConditionCheck.class)
public @interface JNoSQLDatabaseProviderCondition {

    MappingConfigurations propertyName();
    Class<? extends DatabaseConfiguration> providerClass();

}
