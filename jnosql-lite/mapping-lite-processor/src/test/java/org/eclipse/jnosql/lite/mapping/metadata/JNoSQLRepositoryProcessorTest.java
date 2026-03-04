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
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.MethodSignatureKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


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

    @Nested
    @DisplayName("WhenCreateANewInstance")
    class WhenCreateANewInstance {

        @Test
        @DisplayName("Should throw exception when template is null during processor creation")
        void shouldReturnErrorWhenTemplateIsNull() {

            assertThatThrownBy(() ->
                    JNoSQLRepositoryProcessor.of(null, entityMetadata, repositoryMetadata, repositoryOperationProvider))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("template is required");
        }

        @Test
        @DisplayName("Should throw exception when entity metadata is null during processor creation")
        void shouldReturnErrorWhenEntityMetadataIsNull() {

            assertThatThrownBy(() ->
                    JNoSQLRepositoryProcessor.of(template, null, repositoryMetadata, repositoryOperationProvider))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entityMetadata is required");
        }

        @Test
        @DisplayName("Should throw exception when repository metadata is null during processor creation")
        void shouldReturnErrorWhenRepositoryMetadataIsNull() {

            assertThatThrownBy(() ->
                    JNoSQLRepositoryProcessor.of(template, entityMetadata, null, repositoryOperationProvider))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("repositoryMetadata is required");
        }

        @Test
        @DisplayName("Should throw exception when repository operation provider is null during processor creation")
        void shouldReturnErrorWhenRepositoryOperationProviderIsNull() {

            assertThatThrownBy(() ->
                    JNoSQLRepositoryProcessor.of(template, entityMetadata, repositoryMetadata, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("repositoryOperationProvider is required");
        }

        @Test
        @DisplayName("Should successfully create processor instance when all dependencies are provided")
        void shouldCreateInstance() {

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            assertThat(processor).isNotNull();
        }
    }

    @Nested
    @DisplayName("WhenInvokeRepositoryMethod")
    class WhenInvokeRepositoryMethod {

        @Test
        @DisplayName("Should throw exception when method signature key is null")
        void shouldReturnErrorWhenMethodSignatureKeyIsNull() {

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            assertThatThrownBy(() ->
                    processor.invokeRepositoryMethod(null, new Object[0]))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("methodSignatureKey is required");
        }

        @Test
        @DisplayName("Should throw exception when parameters array is null")
        void shouldReturnErrorWhenParamsIsNull() {

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            assertThatThrownBy(() ->
                    processor.invokeRepositoryMethod(methodSignatureKey, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("params is required");
        }

        @Test
        @DisplayName("Should throw exception when repository method cannot be resolved from metadata")
        void shouldReturnErrorWhenRepositoryMethodIsNotFound() {

            when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.empty());

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            assertThatThrownBy(() ->
                    processor.invokeRepositoryMethod(methodSignatureKey, new Object[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Method not found");
        }

        @Test
        @DisplayName("Should dispatch to insert operation when repository method type is INSERT")
        void shouldInvokeInsertOperation() {

            when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            when(repositoryMethod.type()).thenReturn(RepositoryMethodType.INSERT);

            when(repositoryOperationProvider.insertOperation()).thenReturn(operation);
            when(operation.execute(any(RepositoryInvocationContext.class))).thenReturn("result");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            Object result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"entity"});

            assertThat(result).isEqualTo("result");

            verify(repositoryOperationProvider).insertOperation();
            verify(operation).execute(any(RepositoryInvocationContext.class));
        }

        @Test
        @DisplayName("Should dispatch to findBy operation when repository method type is FIND_BY")
        void shouldInvokeFindByOperation() {

            when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            when(repositoryMethod.type()).thenReturn(RepositoryMethodType.FIND_BY);

            when(repositoryOperationProvider.findByOperation()).thenReturn(operation);
            when(operation.execute(any(RepositoryInvocationContext.class))).thenReturn("entity");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            Object result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"value"});

            assertThat(result).isEqualTo("entity");

            verify(repositoryOperationProvider).findByOperation();
            verify(operation).execute(any(RepositoryInvocationContext.class));
        }
    }

    @Nested
    @DisplayName("WhenInvokeRepositoryVoidMethod")
    class WhenInvokeRepositoryVoidMethod {

        @Test
        @DisplayName("Should delegate execution to invokeRepositoryMethod when repository method returns void")
        void shouldDelegateToInvokeRepositoryMethod() {

            when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            when(repositoryMethod.type()).thenReturn(RepositoryMethodType.DELETE);

            when(repositoryOperationProvider.deleteOperation()).thenReturn(operation);

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            processor.invokeRepositoryVoidMethod(methodSignatureKey, new Object[]{"value"});

            verify(repositoryOperationProvider).deleteOperation();
            verify(operation).execute(any(RepositoryInvocationContext.class));
        }
    }


}