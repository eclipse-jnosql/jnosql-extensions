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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
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

/**
 * Semistructured repository operation provider implementation.
 *
 * <p>
 * This class is a copy of the CDI-based {@code SemistructuredRepositoryOperationProvider} from
 * {@code jnosql-mapping-semistructured}, adapted to work outside a CDI container.
 */
@ApplicationScoped
public class SemistructuredRepositoryOperationProvider implements RepositoryOperationProvider {

    private final InsertOperation insertOperation;
    private final UpdateOperation updateOperation;
    private final DeleteOperation deleteOperation;
    private final SaveOperation saveOperation;
    private final FindByOperation findByOperation;
    private final FindAllOperation findAllOperation;
    private final CountByOperation countByOperation;
    private final CountAllOperation countAllOperation;
    private final ExistsByOperation existsByOperation;
    private final DeleteByOperation deleteByOperation;
    private final ParameterBasedOperation parameterBasedOperation;
    private final CursorPaginationOperation cursorPaginationOperation;
    private final QueryOperation queryOperation;
    private final ProviderOperation providerOperation;

    /**
     * Don't use it! It's required by CDI
     */
    SemistructuredRepositoryOperationProvider() {
        this(null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Inject
    SemistructuredRepositoryOperationProvider(InsertOperation insertOperation,
                                              UpdateOperation updateOperation,
                                              DeleteOperation deleteOperation,
                                              SaveOperation saveOperation,
                                              FindByOperation findByOperation,
                                              FindAllOperation findAllOperation,
                                              CountByOperation countByOperation,
                                              CountAllOperation countAllOperation,
                                              ExistsByOperation existsByOperation,
                                              DeleteByOperation deleteByOperation,
                                              QueryOperation queryOperation,
                                              ParameterBasedOperation parameterBasedOperation,
                                              CursorPaginationOperation cursorPaginationOperation,
                                              ProviderOperation providerOperation) {
        this.insertOperation = insertOperation;
        this.updateOperation = updateOperation;
        this.deleteOperation = deleteOperation;
        this.saveOperation = saveOperation;
        this.findByOperation = findByOperation;
        this.findAllOperation = findAllOperation;
        this.countByOperation = countByOperation;
        this.countAllOperation = countAllOperation;
        this.existsByOperation = existsByOperation;
        this.deleteByOperation = deleteByOperation;
        this.queryOperation = queryOperation;
        this.parameterBasedOperation = parameterBasedOperation;
        this.cursorPaginationOperation = cursorPaginationOperation;
        this.providerOperation = providerOperation;
    }

    @Override
    public InsertOperation insertOperation() {
        return insertOperation;
    }

    @Override
    public UpdateOperation updateOperation() {
        return updateOperation;
    }

    @Override
    public DeleteOperation deleteOperation() {
        return deleteOperation;
    }

    @Override
    public SaveOperation saveOperation() {
        return saveOperation;
    }

    @Override
    public FindByOperation findByOperation() {
        return findByOperation;
    }

    @Override
    public FindAllOperation findAllOperation() {
        return findAllOperation;
    }

    @Override
    public CountByOperation countByOperation() {
        return countByOperation;
    }

    @Override
    public CountAllOperation countAllOperation() {
        return countAllOperation;
    }

    @Override
    public ExistsByOperation existsByOperation() {
        return existsByOperation;
    }

    @Override
    public DeleteByOperation deleteByOperation() {
        return deleteByOperation;
    }

    @Override
    public ParameterBasedOperation parameterBasedOperation() {
        return parameterBasedOperation;
    }

    @Override
    public CursorPaginationOperation cursorPaginationOperation() {
        return cursorPaginationOperation;
    }

    @Override
    public QueryOperation queryOperation() {
        return queryOperation;
    }

    @Override
    public ProviderOperation providerOperation() {
        return providerOperation;
    }
}