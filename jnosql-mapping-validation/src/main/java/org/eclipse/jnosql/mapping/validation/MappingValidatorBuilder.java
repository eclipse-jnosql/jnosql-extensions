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
package org.eclipse.jnosql.mapping.validation;

import jakarta.validation.Validator;

import java.util.Objects;

/**
 * Step builder for creating {@link MappingValidator} instances outside a CDI container.
 */
public sealed interface MappingValidatorBuilder permits MappingValidatorBuilder.ValidatorStep,
        MappingValidatorBuilder.TerminalStep {

    /**
     * Returns a new builder starting from {@link ValidatorStep}.
     *
     * @return the first step of the builder
     */
    static ValidatorStep builder() {
        return new ValidatorStep();
    }

    /**
     * Step of the builder that requires a {@link Validator}.
     */
    record ValidatorStep() implements MappingValidatorBuilder {
        /**
         * Provides the {@link Validator}.
         *
         * @param validator the validator to be used
         * @return a {@link TerminalStep}
         * @throws NullPointerException if {@code validator} is null
         */
        public TerminalStep withValidator(Validator validator) {
            Objects.requireNonNull(validator, "validator is required");
            return new TerminalStep(validator);
        }
    }

    /**
     * Final step of the builder.
     */
    record TerminalStep(Validator validator) implements MappingValidatorBuilder {
        /**
         * Builds a {@link MappingValidator} instance.
         *
         * @return a new {@link MappingValidator} instance
         */
        public MappingValidator build() {
            return new MappingValidator(validator);
        }
    }
}
