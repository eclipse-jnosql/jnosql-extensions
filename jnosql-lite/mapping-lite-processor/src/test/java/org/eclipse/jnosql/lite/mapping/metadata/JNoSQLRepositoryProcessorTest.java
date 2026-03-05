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
package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.nosql.Template;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.MethodSignatureKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
@DisplayName("JNoSQLRepositoryProcessor tests")
class JNoSQLRepositoryProcessorTest {

    @Mock
    private Template template;

    @Mock
    private EntityMetadata entityMetadata;

    @Mock
    private RepositoryMetadata repositoryMetadata;

    @Mock
    private RepositoryOperationProvider repositoryOperationProvider;

    @Mock
    private MethodSignatureKey methodSignatureKey;

    @Mock
    private RepositoryMethod repositoryMethod;

    @Mock
    private RepositoryOperation operation;

    @Mock
    private InsertOperation insertOperation;

    @Mock
    private FindByOperation findByOperation;

    @Mock
    private DeleteOperation deleteOperation;

    @Nested
    @DisplayName("WhenCreateANewInstance")
    class WhenCreateANewInstance {

        @Test
        @DisplayName("Should throw error when template is null")
        void shouldReturnErrorWhenTemplateIsNull() {

            Assertions.assertThatThrownBy(() ->
                    JNoSQLRepositoryProcessor.of(null, entityMetadata, repositoryMetadata, repositoryOperationProvider))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("template is required");
        }

        @Test
        @DisplayName("Should throw error when entity metadata is null")
        void shouldReturnErrorWhenEntityMetadataIsNull() {

            Assertions.assertThatThrownBy(() ->
                    JNoSQLRepositoryProcessor.of(template, null, repositoryMetadata, repositoryOperationProvider))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entityMetadata is required");
        }

        @Test
        @DisplayName("Should throw error when repository metadata is null")
        void shouldReturnErrorWhenRepositoryMetadataIsNull() {

            Assertions.assertThatThrownBy(() ->
                    JNoSQLRepositoryProcessor.of(template, entityMetadata, null, repositoryOperationProvider))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("repositoryMetadata is required");
        }

        @Test
        @DisplayName("Should throw error when repository operation provider is null")
        void shouldReturnErrorWhenRepositoryOperationProviderIsNull() {

            Assertions.assertThatThrownBy(() ->
                    JNoSQLRepositoryProcessor.of(template, entityMetadata, repositoryMetadata, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("repositoryOperationProvider is required");
        }

        @Test
        @DisplayName("Should create processor instance")
        void shouldCreateInstance() {

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            Assertions.assertThat(processor).isNotNull();
        }
    }

    @Nested
    @DisplayName("WhenInvokeRepositoryMethod")
    class WhenInvokeRepositoryMethod {

        @Test
        @DisplayName("Should throw error when method signature key is null")
        void shouldReturnErrorWhenMethodSignatureKeyIsNull() {

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            Assertions.assertThatThrownBy(() ->
                    processor.invokeRepositoryMethod(null, new Object[0]))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("methodSignatureKey is required");
        }

        @Test
        @DisplayName("Should throw error when repository method is not found")
        void shouldReturnErrorWhenRepositoryMethodIsNotFound() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.empty());

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            Assertions.assertThatThrownBy(() ->
                    processor.invokeRepositoryMethod(methodSignatureKey, new Object[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Method not found");
        }

        @Test
        @DisplayName("Should dispatch execution to insert operation")
        void shouldInvokeInsertOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.INSERT);

            Mockito.when(repositoryOperationProvider.insertOperation()).thenReturn(insertOperation);
            Mockito.when(insertOperation.execute(ArgumentMatchers.any())).thenReturn("result");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"entity"});

            Assertions.assertThat(result).isEqualTo("result");

            Mockito.verify(repositoryOperationProvider).insertOperation();
            Mockito.verify(insertOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to findBy operation")
        void shouldInvokeFindByOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.FIND_BY);

            Mockito.when(repositoryOperationProvider.findByOperation()).thenReturn(findByOperation);
            Mockito.when(findByOperation.execute(ArgumentMatchers.any())).thenReturn("entity");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"value"});

            Assertions.assertThat(result).isEqualTo("entity");

            Mockito.verify(repositoryOperationProvider).findByOperation();
            Mockito.verify(findByOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to delete operation")
        void shouldInvokeDeleteOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.DELETE);

            Mockito.when(repositoryOperationProvider.deleteOperation()).thenReturn(deleteOperation);

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            processor.invokeRepositoryVoidMethod(methodSignatureKey, new Object[]{"value"});

            Mockito.verify(repositoryOperationProvider).deleteOperation();
            Mockito.verify(deleteOperation).execute(ArgumentMatchers.any());
        }
    }


}