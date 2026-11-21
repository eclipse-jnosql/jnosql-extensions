/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Maximillian Arruda
 */
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