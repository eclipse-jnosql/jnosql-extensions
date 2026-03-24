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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
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

@ApplicationScoped
@Typed(SqlRepositoryOperationProvider.class)
class SqlRepositoryOperationProvider implements RepositoryOperationProvider {

    private final InsertOperation insertOperation;
    private final UpdateOperation updateOperation;
    private final SaveOperation saveOperation;
    private final ProviderOperation providerOperation;

    SqlRepositoryOperationProvider(InsertOperation insertOperation,
                                   UpdateOperation updateOperation,
                                   SaveOperation saveOperation,
                                   ProviderOperation providerOperation) {
        this.insertOperation = insertOperation;
        this.updateOperation = updateOperation;
        this.saveOperation = saveOperation;
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
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public SaveOperation saveOperation() {
        return saveOperation;
    }

    @Override
    public FindByOperation findByOperation() {
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public FindAllOperation findAllOperation() {
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public CountByOperation countByOperation() {
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public CountAllOperation countAllOperation() {
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public ExistsByOperation existsByOperation() {
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public DeleteByOperation deleteByOperation() {
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public ParameterBasedOperation parameterBasedOperation() {
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public CursorPaginationOperation cursorPaginationOperation() {
        throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public QueryOperation queryOperation() {
       throw new UnsupportedOperationException("There is not support on query yet");
    }

    @Override
    public ProviderOperation providerOperation() {
        return providerOperation;
    }
}
