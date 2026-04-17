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
package org.eclipse.jnosql.mapping.core.repository.operations;

import org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.DefaultMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator;

import java.util.Objects;

/**
 * Step builder for creating {@link InfrastructureOperatorProvider} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * InfrastructureOperatorProvider provider = InfrastructureOperatorProviderBuilder.builder()
 *         .withBuiltIn(builtIn)
 *         .withObject(object)
 *         .withCustom(custom)
 *         .withDefault(defaultOp)
 *         .build();
 * }</pre>
 * </p>
 *
 * <p>
 * Each step is immutable. Calling {@code withXxx()} returns a new
 * record with the updated value, leaving the original step unchanged.
 * </p>
 */
public sealed interface InfrastructureOperatorProviderBuilder
        permits InfrastructureOperatorProviderBuilder.Step1,
        InfrastructureOperatorProviderBuilder.Step2,
        InfrastructureOperatorProviderBuilder.Step3,
        InfrastructureOperatorProviderBuilder.Step4 {

    static Step1 builder() {
        return new Step1(null, null, null, null);
    }

    record Step1(BuiltInMethodOperator builtIn,
                 ObjectMethodOperator objectOp,
                 CustomRepositoryMethodOperator custom,
                 DefaultMethodOperator defaultOp) implements InfrastructureOperatorProviderBuilder {

        public Step2 withBuiltIn(BuiltInMethodOperator builtIn) {
            Objects.requireNonNull(builtIn, "builtIn is required");
            return new Step2(builtIn, objectOp, custom, defaultOp);
        }
    }

    record Step2(BuiltInMethodOperator builtIn,
                 ObjectMethodOperator objectOp,
                 CustomRepositoryMethodOperator custom,
                 DefaultMethodOperator defaultOp) implements InfrastructureOperatorProviderBuilder {

        public Step2 withBuiltIn(BuiltInMethodOperator builtIn) {
            Objects.requireNonNull(builtIn, "builtIn is required");
            return new Step2(builtIn, objectOp, custom, defaultOp);
        }

        public Step3 withObject(ObjectMethodOperator objectOp) {
            Objects.requireNonNull(objectOp, "objectOp is required");
            return new Step3(builtIn, objectOp, custom, defaultOp);
        }
    }

    record Step3(BuiltInMethodOperator builtIn,
                 ObjectMethodOperator objectOp,
                 CustomRepositoryMethodOperator custom,
                 DefaultMethodOperator defaultOp) implements InfrastructureOperatorProviderBuilder {

        public Step2 withBuiltIn(BuiltInMethodOperator builtIn) {
            Objects.requireNonNull(builtIn, "builtIn is required");
            return new Step2(builtIn, objectOp, custom, defaultOp);
        }

        public Step3 withObject(ObjectMethodOperator objectOp) {
            Objects.requireNonNull(objectOp, "objectOp is required");
            return new Step3(builtIn, objectOp, custom, defaultOp);
        }

        public Step4 withCustom(CustomRepositoryMethodOperator custom) {
            Objects.requireNonNull(custom, "custom is required");
            return new Step4(builtIn, objectOp, custom, defaultOp);
        }
    }

    record Step4(BuiltInMethodOperator builtIn,
                 ObjectMethodOperator objectOp,
                 CustomRepositoryMethodOperator custom,
                 DefaultMethodOperator defaultOp) implements InfrastructureOperatorProviderBuilder {

        public Step2 withBuiltIn(BuiltInMethodOperator builtIn) {
            Objects.requireNonNull(builtIn, "builtIn is required");
            return new Step2(builtIn, objectOp, custom, defaultOp);
        }

        public Step3 withObject(ObjectMethodOperator objectOp) {
            Objects.requireNonNull(objectOp, "objectOp is required");
            return new Step3(builtIn, objectOp, custom, defaultOp);
        }

        public Step4 withCustom(CustomRepositoryMethodOperator custom) {
            Objects.requireNonNull(custom, "custom is required");
            return new Step4(builtIn, objectOp, custom, defaultOp);
        }

        public Step4 withDefault(DefaultMethodOperator defaultOp) {
            Objects.requireNonNull(defaultOp, "defaultOp is required");
            return new Step4(builtIn, objectOp, custom, defaultOp);
        }

        public InfrastructureOperatorProvider build() {
            Objects.requireNonNull(builtIn, "builtIn is required");
            Objects.requireNonNull(objectOp, "objectOp is required");
            Objects.requireNonNull(custom, "custom is required");
            Objects.requireNonNull(defaultOp, "defaultOp is required");
            return new DefaultInfrastructureOperatorProvider(builtIn, objectOp, custom, defaultOp);
        }
    }
}