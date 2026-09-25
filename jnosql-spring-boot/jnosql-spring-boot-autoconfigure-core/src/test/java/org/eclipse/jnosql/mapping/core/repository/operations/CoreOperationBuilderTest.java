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

import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.DefaultMethodOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoreOperationBuilderTest {

    @Test
    @DisplayName("Should build InsertOperation successfully")
    void shouldBuildInsertOperation() {
        InsertOperation result = CoreInsertOperationBuilder.build();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(InsertOperation.class);
    }

    @Test
    @DisplayName("Should build UpdateOperation successfully")
    void shouldBuildUpdateOperation() {
        UpdateOperation result = CoreUpdateOperationBuilder.build();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UpdateOperation.class);
    }

    @Test
    @DisplayName("Should build SaveOperation successfully")
    void shouldBuildSaveOperation() {
        SaveOperation result = CoreSaveOperationBuilder.build();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SaveOperation.class);
    }

    @Test
    @DisplayName("Should build DeleteOperation successfully")
    void shouldBuildDeleteOperation() {
        DeleteOperation result = CoreDeleteOperationBuilder.build();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(DeleteOperation.class);
    }

    @Test
    @DisplayName("Should build BuiltInMethodOperator successfully")
    void shouldBuildBuiltInMethodOperator() {
        BuiltInMethodOperator result = DefaultBuiltInMethodOperatorBuilder.build();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(BuiltInMethodOperator.class);
    }

    @Test
    @DisplayName("Should build ObjectMethodOperator successfully")
    void shouldBuildObjectMethodOperator() {
        ObjectMethodOperator result = DefaultObjectMethodOperatorBuilder.build();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(ObjectMethodOperator.class);
    }

    @Test
    @DisplayName("Should build DefaultMethodOperator successfully")
    void shouldBuildDefaultMethodOperator() {
        DefaultMethodOperator result = CoreDefaultMethodOperatorBuilder.build();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(DefaultMethodOperator.class);
    }
}