/*
 *  Copyright (c) 2026 Otávio Santana and others
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
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProcessorUtilTest {

    @Nested
    class WhenGenerateClassName {

        @Test
        void shouldKeepTheOriginalStructureWhenThereIsOneComponent() {
            String className = ProcessorUtil.generateClassName("PersonRepository");
            Assertions.assertThat(className).isEqualTo("PersonRepository");
        }

        @Test
        void shouldConcatWhenThereIsTwoComponents() {
            String className = ProcessorUtil.generateClassName("PersonRepository", "findByName");
            Assertions.assertThat(className).containsIgnoringCase("findByName");
        }

        @Test
        void shouldCapitalizeTheTheSecondComponent() {
            String className = ProcessorUtil.generateClassName("PersonRepository", "findByName");
            Assertions.assertThat(className).isEqualTo("PersonRepositoryFindByName");
        }

        @Test
        void shouldConcatThreeOrMoreComponents() {
            String className = ProcessorUtil.generateClassName("PersonRepository", "findByName", "Name");
            Assertions.assertThat(className).isEqualTo("PersonRepositoryFindByNameName");
        }
    }
}