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
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.extensions.sql.SqlTemplateFactory;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;
import org.eclipse.jnosql.mapping.reflection.FieldReader;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@EnableWeld
@ExtendWith(MockitoExtension.class)
class SqlRepositoryOperationProviderTest {

    @SuppressWarnings("unchecked")
    @WeldSetup
    WeldInitiator weld = WeldInitiator.of(
            WeldInitiator.createWeld()
                    .addBeanClasses(
                            SqlTemplateFactory.class,
                            SqlRepositoryAdapterTest.class,
                            SqlRepositoryProducer.class,
                            ProjectorConverter.class
                    )
                    .addPackages(true, CoreDeleteOperation.class)
                    .addPackages(true, FieldReader.class)
                    .addPackages(true, SqlRepositoryOperationProvider.class)
                    .addExtensions(ReflectionEntityMetadataExtension.class)
    );

    @Inject
    private SqlRepositoryOperationProvider provider;

    @Nested
    @DisplayName("WhenProvidingOperations")
    class WhenProvidingOperations {

        @Mock
        private InsertOperation insertOperation;

        @Mock
        private UpdateOperation updateOperation;

        @Mock
        private SaveOperation saveOperation;

        @Mock
        private ProviderOperation providerOperation;

        @Mock
        private SqlFindByOperation findByOperation;

        @Mock
        private SqlDeleteOperation deleteOperation;

        @Mock
        private SqlFindAllOperation findAllOperation;

        @Mock
        private SqlCountByOperation countByOperation;

        @Mock
        private SqlCountAllOperation countAllOperation;

        @Mock
        private SqlExistsByOperation existsByOperation;

        @Mock
        private SqlDeleteByOperation deleteByOperation;

        @Mock
        private SqlParameterBasedOperation parameterBasedOperation;

        @Mock
        private SqlCursorPaginationOperation cursorPaginationOperation;

        @Mock
        private SqlQueryOperation queryOperation;

        @InjectMocks
        private SqlRepositoryOperationProvider provider;

        @Test
        @DisplayName("Should return all operations correctly")
        void shouldReturnAllOperations() {

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(provider.insertOperation()).isEqualTo(insertOperation);
                softly.assertThat(provider.updateOperation()).isEqualTo(updateOperation);
                softly.assertThat(provider.saveOperation()).isEqualTo(saveOperation);
                softly.assertThat(provider.providerOperation()).isEqualTo(providerOperation);

                softly.assertThat(provider.findByOperation()).isEqualTo(findByOperation);
                softly.assertThat(provider.deleteOperation()).isEqualTo(deleteOperation);
                softly.assertThat(provider.findAllOperation()).isEqualTo(findAllOperation);
                softly.assertThat(provider.countByOperation()).isEqualTo(countByOperation);
                softly.assertThat(provider.countAllOperation()).isEqualTo(countAllOperation);
                softly.assertThat(provider.existsByOperation()).isEqualTo(existsByOperation);
                softly.assertThat(provider.deleteByOperation()).isEqualTo(deleteByOperation);
                softly.assertThat(provider.parameterBasedOperation()).isEqualTo(parameterBasedOperation);
                softly.assertThat(provider.cursorPaginationOperation()).isEqualTo(cursorPaginationOperation);
                softly.assertThat(provider.queryOperation()).isEqualTo(queryOperation);
            });
        }

        @Test
        @DisplayName("Should return null operations when using default constructor")
        void shouldReturnNullWhenDefaultConstructorUsed() {

            var provider = new SqlRepositoryOperationProvider();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(provider.insertOperation()).isNull();
                softly.assertThat(provider.updateOperation()).isNull();
                softly.assertThat(provider.saveOperation()).isNull();
                softly.assertThat(provider.providerOperation()).isNull();

                softly.assertThat(provider.findByOperation()).isNull();
                softly.assertThat(provider.deleteOperation()).isNull();
                softly.assertThat(provider.findAllOperation()).isNull();
                softly.assertThat(provider.countByOperation()).isNull();
                softly.assertThat(provider.countAllOperation()).isNull();
                softly.assertThat(provider.existsByOperation()).isNull();
                softly.assertThat(provider.deleteByOperation()).isNull();
                softly.assertThat(provider.parameterBasedOperation()).isNull();
                softly.assertThat(provider.cursorPaginationOperation()).isNull();
                softly.assertThat(provider.queryOperation()).isNull();
            });
        }
    }
}