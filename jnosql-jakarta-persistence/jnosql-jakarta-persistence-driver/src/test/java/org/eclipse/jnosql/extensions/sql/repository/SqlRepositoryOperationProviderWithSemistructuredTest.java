/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Arjan Tijms
 */
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.reflection.FieldReader;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredCountAllOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredExistsByOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredParameterBasedOperation;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryOperationProvider;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * An application that bundles this driver also bundles jnosql-mapping-semistructured, so bean discovery
 * finds the repository operations of both.
 */
@EnableWeld
class SqlRepositoryOperationProviderWithSemistructuredTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.of(
            WeldInitiator.createWeld()
                    .addBeanClasses(
                            ProjectorConverter.class,
                            Converters.class
                    )
                    .addPackages(true, CoreDeleteOperation.class)
                    .addPackages(true, FieldReader.class)
                    .addPackages(true, SqlRepositoryOperationProvider.class)
                    .addPackages(SemistructuredRepositoryOperationProvider.class)
                    .addExtensions(ReflectionEntityMetadataExtension.class)
    );

    @Inject
    private SqlRepositoryOperationProvider sqlProvider;

    @Inject
    private SemistructuredRepositoryOperationProvider semistructuredProvider;

    @Test
    @DisplayName("Should give each provider its own operations")
    void shouldGiveEachProviderItsOwnOperations() {

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(sqlProvider.countAllOperation()).isInstanceOf(SqlCountAllOperation.class);
            softly.assertThat(sqlProvider.existsByOperation()).isInstanceOf(SqlExistsByOperation.class);
            softly.assertThat(sqlProvider.parameterBasedOperation()).isInstanceOf(SqlParameterBasedOperation.class);

            softly.assertThat(semistructuredProvider.countAllOperation())
                    .isInstanceOf(SemistructuredCountAllOperation.class);
            softly.assertThat(semistructuredProvider.existsByOperation())
                    .isInstanceOf(SemistructuredExistsByOperation.class);
            softly.assertThat(semistructuredProvider.parameterBasedOperation())
                    .isInstanceOf(SemistructuredParameterBasedOperation.class);
        });
    }
}
