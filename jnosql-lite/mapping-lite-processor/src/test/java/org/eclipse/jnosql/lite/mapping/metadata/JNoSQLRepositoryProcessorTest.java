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
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CursorPaginationOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ExistsByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ParameterBasedOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;
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
    private UpdateOperation updateOperation;

    @Mock
    private SaveOperation saveOperation;

    @Mock
    private DeleteByOperation deleteByOperation;

    @Mock
    private FindAllOperation findAllOperation;

    @Mock
    private CountAllOperation countAllOperation;

    @Mock
    private CountByOperation countByOperation;

    @Mock
    private CursorPaginationOperation cursorPaginationOperation;

    @Mock
    private ParameterBasedOperation parameterBasedOperation;

    @Mock
    private ExistsByOperation existsByOperation;

    @Mock
    private QueryOperation queryOperation;

    @Mock
    private InsertOperation insertOperation;

    @Mock
    private DeleteOperation deleteOperation;

    @Mock
    private FindByOperation findByOperation;

    @Mock
    private ProviderOperation providerOperation;

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
        @DisplayName("Should dispatch execution to update operation")
        void shouldInvokeUpdateOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.UPDATE);

            Mockito.when(repositoryOperationProvider.updateOperation()).thenReturn(updateOperation);
            Mockito.when(updateOperation.execute(ArgumentMatchers.any())).thenReturn("result");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"entity"});

            Assertions.assertThat(result).isEqualTo("result");

            Mockito.verify(repositoryOperationProvider).updateOperation();
            Mockito.verify(updateOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to save operation")
        void shouldInvokeSaveOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.SAVE);

            Mockito.when(repositoryOperationProvider.saveOperation()).thenReturn(saveOperation);
            Mockito.when(saveOperation.execute(ArgumentMatchers.any())).thenReturn("result");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"entity"});

            Assertions.assertThat(result).isEqualTo("result");

            Mockito.verify(repositoryOperationProvider).saveOperation();
            Mockito.verify(saveOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to deleteBy operation")
        void shouldInvokeDeleteByOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.DELETE_BY);

            Mockito.when(repositoryOperationProvider.deleteByOperation()).thenReturn(deleteByOperation);
            Mockito.when(deleteByOperation.execute(ArgumentMatchers.any())).thenReturn("result");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"entity"});

            Assertions.assertThat(result).isEqualTo("result");

            Mockito.verify(repositoryOperationProvider).deleteByOperation();
            Mockito.verify(deleteByOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to countAll operation")
        void shouldInvokeCountAllOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.COUNT_ALL);

            Mockito.when(repositoryOperationProvider.countAllOperation()).thenReturn(countAllOperation);
            Mockito.when(countAllOperation.execute(ArgumentMatchers.any())).thenReturn(10L);

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{});

            Assertions.assertThat(result).isEqualTo(10L);

            Mockito.verify(repositoryOperationProvider).countAllOperation();
            Mockito.verify(countAllOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to countBy operation")
        void shouldInvokeCountByOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.COUNT_BY);

            Mockito.when(repositoryOperationProvider.countByOperation()).thenReturn(countByOperation);
            Mockito.when(countByOperation.execute(ArgumentMatchers.any())).thenReturn(5L);

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"value"});

            Assertions.assertThat(result).isEqualTo(5L);

            Mockito.verify(repositoryOperationProvider).countByOperation();
            Mockito.verify(countByOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to cursor pagination operation")
        void shouldInvokeCursorPaginationOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.CURSOR_PAGINATION);

            Mockito.when(repositoryOperationProvider.cursorPaginationOperation()).thenReturn(cursorPaginationOperation);
            Mockito.when(cursorPaginationOperation.execute(ArgumentMatchers.any())).thenReturn("cursor");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"cursor"});

            Assertions.assertThat(result).isEqualTo("cursor");

            Mockito.verify(repositoryOperationProvider).cursorPaginationOperation();
            Mockito.verify(cursorPaginationOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to parameter based operation")
        void shouldInvokeParameterBasedOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.PARAMETER_BASED);

            Mockito.when(repositoryOperationProvider.parameterBasedOperation()).thenReturn(parameterBasedOperation);
            Mockito.when(parameterBasedOperation.execute(ArgumentMatchers.any())).thenReturn("result");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"param"});

            Assertions.assertThat(result).isEqualTo("result");

            Mockito.verify(repositoryOperationProvider).parameterBasedOperation();
            Mockito.verify(parameterBasedOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to existsBy operation")
        void shouldInvokeExistsByOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.EXISTS_BY);

            Mockito.when(repositoryOperationProvider.existsByOperation()).thenReturn(existsByOperation);
            Mockito.when(existsByOperation.execute(ArgumentMatchers.any())).thenReturn(true);

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"value"});

            Assertions.assertThat(result).isEqualTo(true);

            Mockito.verify(repositoryOperationProvider).existsByOperation();
            Mockito.verify(existsByOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to findAll operation")
        void shouldInvokeFindAllOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.FIND_ALL);

            Mockito.when(repositoryOperationProvider.findAllOperation()).thenReturn(findAllOperation);
            Mockito.when(findAllOperation.execute(ArgumentMatchers.any())).thenReturn("all");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{});

            Assertions.assertThat(result).isEqualTo("all");

            Mockito.verify(repositoryOperationProvider).findAllOperation();
            Mockito.verify(findAllOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to query operation")
        void shouldInvokeQueryOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.QUERY);

            Mockito.when(repositoryOperationProvider.queryOperation()).thenReturn(queryOperation);
            Mockito.when(queryOperation.execute(ArgumentMatchers.any())).thenReturn("query");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"query"});

            Assertions.assertThat(result).isEqualTo("query");

            Mockito.verify(repositoryOperationProvider).queryOperation();
            Mockito.verify(queryOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to provider operation")
        void shouldInvokeProviderOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.PROVIDER_OPERATION);

            Mockito.when(repositoryOperationProvider.providerOperation()).thenReturn(providerOperation);
            Mockito.when(providerOperation.execute(ArgumentMatchers.any())).thenReturn("provider");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"provider"});

            Assertions.assertThat(result).isEqualTo("provider");

            Mockito.verify(repositoryOperationProvider).providerOperation();
            Mockito.verify(providerOperation).execute(ArgumentMatchers.any());
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
        @DisplayName("Should dispatch execution to delete operation")
        void shouldInvokeDeleteOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.DELETE);

            Mockito.when(repositoryOperationProvider.deleteOperation()).thenReturn(deleteOperation);
            Mockito.when(deleteOperation.execute(ArgumentMatchers.any())).thenReturn("result");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"entity"});

            Assertions.assertThat(result).isEqualTo("result");

            Mockito.verify(repositoryOperationProvider).deleteOperation();
            Mockito.verify(deleteOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should dispatch execution to findBy operation")
        void shouldInvokeFindByOperation() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.FIND_BY);

            Mockito.when(repositoryOperationProvider.findByOperation()).thenReturn(findByOperation);
            Mockito.when(findByOperation.execute(ArgumentMatchers.any())).thenReturn("result");

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            var result = processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{"value"});

            Assertions.assertThat(result).isEqualTo("result");

            Mockito.verify(repositoryOperationProvider).findByOperation();
            Mockito.verify(findByOperation).execute(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should throw error when operation type is unsupported")
        void shouldThrowErrorWhenOperationIsUnsupported() {

            Mockito.when(repositoryMetadata.find(methodSignatureKey)).thenReturn(Optional.of(repositoryMethod));
            Mockito.when(repositoryMethod.type()).thenReturn(RepositoryMethodType.UNKNOWN);

            var processor = JNoSQLRepositoryProcessor.of(
                    template,
                    entityMetadata,
                    repositoryMetadata,
                    repositoryOperationProvider
            );

            Assertions.assertThatThrownBy(() ->
                            processor.invokeRepositoryMethod(methodSignatureKey, new Object[]{}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported repository operation");
        }
    }


}