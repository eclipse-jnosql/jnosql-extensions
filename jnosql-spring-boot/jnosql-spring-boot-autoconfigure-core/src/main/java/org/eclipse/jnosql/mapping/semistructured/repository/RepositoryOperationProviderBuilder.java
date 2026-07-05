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
package org.eclipse.jnosql.mapping.semistructured.repository;

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

import java.util.Objects;

/**
 * Step builder for creating {@link SemistructuredRepositoryOperationProvider} instances.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * RepositoryOperationProvider provider = RepositoryOperationProviderBuilder.builder()
 *         .withInsert(insertOp)
 *         .withUpdate(updateOp)
 *         .withDelete(deleteOp)
 *         .withSave(saveOp)
 *         .withFindBy(findByOp)
 *         .withFindAll(findAllOp)
 *         .withCountBy(countByOp)
 *         .withCountAll(countAllOp)
 *         .withExistsBy(existsByOp)
 *         .withDeleteBy(deleteByOp)
 *         .withQuery(queryOp)
 *         .withParamBased(paramBasedOp)
 *         .withCursorPagination(cursorPaginationOp)
 *         .withProvider(providerOp)
 *         .build();
 * }</pre>

 *
 * <p>
 * Each step is immutable. Calling {@code withXxx()} returns a new
 * record with the updated value, leaving the original step unchanged.

 */
public sealed interface RepositoryOperationProviderBuilder
        permits RepositoryOperationProviderBuilder.InsertStep,
        RepositoryOperationProviderBuilder.UpdateStep,
        RepositoryOperationProviderBuilder.DeleteStep,
        RepositoryOperationProviderBuilder.SaveStep,
        RepositoryOperationProviderBuilder.FindByStep,
        RepositoryOperationProviderBuilder.FindAllStep,
        RepositoryOperationProviderBuilder.CountByStep,
        RepositoryOperationProviderBuilder.CountAllStep,
        RepositoryOperationProviderBuilder.ExistsByStep,
        RepositoryOperationProviderBuilder.DeleteByStep,
        RepositoryOperationProviderBuilder.QueryStep,
        RepositoryOperationProviderBuilder.ParamBasedStep,
        RepositoryOperationProviderBuilder.CursorPaginationStep,
        RepositoryOperationProviderBuilder.ProviderStep,
        RepositoryOperationProviderBuilder.FinalStep {

    static InsertStep builder() {
        return new InsertStep(null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }

    record InsertStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public UpdateStep withInsert(InsertOperation op) {
            Objects.requireNonNull(op, "insertOp is required");
            return new UpdateStep(op, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record UpdateStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public UpdateStep withInsert(InsertOperation op) {
            Objects.requireNonNull(op, "insertOp is required");
            return new UpdateStep(op, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public DeleteStep withUpdate(UpdateOperation op) {
            Objects.requireNonNull(op, "updateOp is required");
            return new DeleteStep(insertOp, op, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record DeleteStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public DeleteStep withInsert(InsertOperation op) {
            Objects.requireNonNull(op, "insertOp is required");
            return new DeleteStep(op, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public DeleteStep withUpdate(UpdateOperation op) {
            Objects.requireNonNull(op, "updateOp is required");
            return new DeleteStep(insertOp, op, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public SaveStep withDelete(DeleteOperation op) {
            Objects.requireNonNull(op, "deleteOp is required");
            return new SaveStep(insertOp, updateOp, op, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record SaveStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public SaveStep withInsert(InsertOperation op) {
            Objects.requireNonNull(op, "insertOp is required");
            return new SaveStep(op, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public SaveStep withUpdate(UpdateOperation op) {
            Objects.requireNonNull(op, "updateOp is required");
            return new SaveStep(insertOp, op, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public SaveStep withDelete(DeleteOperation op) {
            Objects.requireNonNull(op, "deleteOp is required");
            return new SaveStep(insertOp, updateOp, op, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindByStep withSave(SaveOperation op) {
            Objects.requireNonNull(op, "saveOp is required");
            return new FindByStep(insertOp, updateOp, deleteOp, op, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record FindByStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public FindByStep withInsert(InsertOperation op) {
            Objects.requireNonNull(op, "insertOp is required");
            return new FindByStep(op, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindByStep withUpdate(UpdateOperation op) {
            Objects.requireNonNull(op, "updateOp is required");
            return new FindByStep(insertOp, op, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindByStep withDelete(DeleteOperation op) {
            Objects.requireNonNull(op, "deleteOp is required");
            return new FindByStep(insertOp, updateOp, op, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindByStep withSave(SaveOperation op) {
            Objects.requireNonNull(op, "saveOp is required");
            return new FindByStep(insertOp, updateOp, deleteOp, op, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindAllStep withFindBy(FindByOperation op) {
            Objects.requireNonNull(op, "findByOp is required");
            return new FindAllStep(insertOp, updateOp, deleteOp, saveOp, op,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record FindAllStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public FindAllStep withInsert(InsertOperation op) {
            Objects.requireNonNull(op, "insertOp is required");
            return new FindAllStep(op, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindAllStep withUpdate(UpdateOperation op) {
            Objects.requireNonNull(op, "updateOp is required");
            return new FindAllStep(insertOp, op, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindAllStep withDelete(DeleteOperation op) {
            Objects.requireNonNull(op, "deleteOp is required");
            return new FindAllStep(insertOp, updateOp, op, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindAllStep withSave(SaveOperation op) {
            Objects.requireNonNull(op, "saveOp is required");
            return new FindAllStep(insertOp, updateOp, deleteOp, op, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public FindAllStep withFindBy(FindByOperation op) {
            Objects.requireNonNull(op, "findByOp is required");
            return new FindAllStep(insertOp, updateOp, deleteOp, saveOp, op,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public CountByStep withFindAll(FindAllOperation op) {
            Objects.requireNonNull(op, "findAllOp is required");
            return new CountByStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    op, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record CountByStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public CountByStep withInsert(InsertOperation op) {
            Objects.requireNonNull(op, "insertOp is required");
            return new CountByStep(op, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public CountByStep withUpdate(UpdateOperation op) {
            Objects.requireNonNull(op, "updateOp is required");
            return new CountByStep(insertOp, op, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public CountByStep withDelete(DeleteOperation op) {
            Objects.requireNonNull(op, "deleteOp is required");
            return new CountByStep(insertOp, updateOp, op, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public CountByStep withSave(SaveOperation op) {
            Objects.requireNonNull(op, "saveOp is required");
            return new CountByStep(insertOp, updateOp, deleteOp, op, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public CountByStep withFindBy(FindByOperation op) {
            Objects.requireNonNull(op, "findByOp is required");
            return new CountByStep(insertOp, updateOp, deleteOp, saveOp, op,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public CountByStep withFindAll(FindAllOperation op) {
            Objects.requireNonNull(op, "findAllOp is required");
            return new CountByStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    op, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }

        public CountAllStep withCountBy(CountByOperation op) {
            Objects.requireNonNull(op, "countByOp is required");
            return new CountAllStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, op, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record CountAllStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public ExistsByStep withCountAll(CountAllOperation op) {
            Objects.requireNonNull(op, "countAllOp is required");
            return new ExistsByStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, op, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record ExistsByStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public DeleteByStep withExistsBy(ExistsByOperation op) {
            Objects.requireNonNull(op, "existsByOp is required");
            return new DeleteByStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, op, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record DeleteByStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public QueryStep withDeleteBy(DeleteByOperation op) {
            Objects.requireNonNull(op, "deleteByOp is required");
            return new QueryStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, op,
                    queryOp, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record QueryStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public ParamBasedStep withQuery(QueryOperation op) {
            Objects.requireNonNull(op, "queryOp is required");
            return new ParamBasedStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    op, paramBasedOp, cursorPaginationOp, providerOp);
        }
    }

    record ParamBasedStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public CursorPaginationStep withParamBased(ParameterBasedOperation op) {
            Objects.requireNonNull(op, "paramBasedOp is required");
            return new CursorPaginationStep(insertOp, updateOp, deleteOp, saveOp,
                    findByOp, findAllOp, countByOp, countAllOp, existsByOp,
                    deleteByOp, queryOp, op, cursorPaginationOp, providerOp);
        }
    }

    record CursorPaginationStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public ProviderStep withCursorPagination(CursorPaginationOperation op) {
            Objects.requireNonNull(op, "cursorPaginationOp is required");
            return new ProviderStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, op, providerOp);
        }
    }

    record ProviderStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public FinalStep withProvider(ProviderOperation op) {
            Objects.requireNonNull(op, "providerOp is required");
            return new FinalStep(insertOp, updateOp, deleteOp, saveOp, findByOp,
                    findAllOp, countByOp, countAllOp, existsByOp, deleteByOp,
                    queryOp, paramBasedOp, cursorPaginationOp, op);
        }
    }

    record FinalStep(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) implements RepositoryOperationProviderBuilder {

        public SemistructuredRepositoryOperationProvider build() {
            Objects.requireNonNull(insertOp, "insertOp is required");
            Objects.requireNonNull(updateOp, "updateOp is required");
            Objects.requireNonNull(deleteOp, "deleteOp is required");
            Objects.requireNonNull(saveOp, "saveOp is required");
            Objects.requireNonNull(findByOp, "findByOp is required");
            Objects.requireNonNull(findAllOp, "findAllOp is required");
            Objects.requireNonNull(countByOp, "countByOp is required");
            Objects.requireNonNull(countAllOp, "countAllOp is required");
            Objects.requireNonNull(existsByOp, "existsByOp is required");
            Objects.requireNonNull(deleteByOp, "deleteByOp is required");
            Objects.requireNonNull(queryOp, "queryOp is required");
            Objects.requireNonNull(paramBasedOp, "paramBasedOp is required");
            Objects.requireNonNull(cursorPaginationOp, "cursorPaginationOp is required");
            Objects.requireNonNull(providerOp, "providerOp is required");
            return new SemistructuredRepositoryOperationProvider(insertOp, updateOp,
                    deleteOp, saveOp, findByOp, findAllOp, countByOp, countAllOp,
                    existsByOp, deleteByOp, queryOp, paramBasedOp, cursorPaginationOp,
                    providerOp);
        }
    }
}
