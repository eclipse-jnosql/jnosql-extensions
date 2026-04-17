package org.eclipse.jnosql.spring.boot.autoconfigure;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Optional;

public class JNoSQLDatabaseProviderConditionCheck implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        if(!metadata.getAnnotations().isPresent(JNoSQLDatabaseProviderCondition.class))
            return true;

        JNoSQLDatabaseProviderCondition databaseProviderCondition = metadata.getAnnotations().get(JNoSQLDatabaseProviderCondition.class).synthesize();

        Optional<String> documentProvider =
                Optional.ofNullable(context.getEnvironment().getProperty(databaseProviderCondition.propertyName().get()));
        if (documentProvider.isPresent()) {
            return documentProvider
                    .filter(p -> p.equals(databaseProviderCondition.providerClass().getName()))
                    .isPresent();
        }
        return true;
    }
}