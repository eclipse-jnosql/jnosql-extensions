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

@ApplicationScoped
@Typed(SqlRepositoryOperationProvider.class)
class SqlRepositoryOperationProvider implements RepositoryOperationProvider {

    private final InsertOperation insertOperation;
    private final UpdateOperation updateOperation;
    private final SaveOperation saveOperation;
    private final ProviderOperation providerOperation;
    private final SqlFindByOperation findByOperation;
    private final SqlDeleteOperation deleteOperation;
    private final SqlFindAllOperation findAllOperation;
    private final SqlCountByOperation countByOperation;
    private final SqlCountAllOperation countAllOperation;
    private final SqlExistsByOperation existsByOperation;
    private final SqlDeleteByOperation deleteByOperation;
    private final SqlParameterBasedOperation parameterBasedOperation;
    private final SqlCursorPaginationOperation cursorPaginationOperation;
    private final SqlQueryOperation queryOperation;

    @Inject
    SqlRepositoryOperationProvider(InsertOperation insertOperation,
                                   UpdateOperation updateOperation,
                                   SaveOperation saveOperation,
                                   ProviderOperation providerOperation,
                                   SqlFindByOperation findByOperation,
                                   SqlDeleteOperation deleteOperation,
                                   SqlFindAllOperation findAllOperation,
                                   SqlCountByOperation countByOperation,
                                   SqlCountAllOperation countAllOperation,
                                   SqlExistsByOperation existsByOperation,
                                   SqlDeleteByOperation deleteByOperation,
                                   SqlParameterBasedOperation parameterBasedOperation,
                                   SqlCursorPaginationOperation cursorPaginationOperation,
                                   SqlQueryOperation queryOperation) {
        this.insertOperation = insertOperation;
        this.updateOperation = updateOperation;
        this.saveOperation = saveOperation;
        this.providerOperation = providerOperation;
        this.findByOperation = findByOperation;
        this.deleteOperation = deleteOperation;
        this.findAllOperation = findAllOperation;
        this.countByOperation = countByOperation;
        this.countAllOperation = countAllOperation;
        this.existsByOperation = existsByOperation;
        this.deleteByOperation = deleteByOperation;
        this.parameterBasedOperation = parameterBasedOperation;
        this.cursorPaginationOperation = cursorPaginationOperation;
        this.queryOperation = queryOperation;
    }

    SqlRepositoryOperationProvider() {
        this.insertOperation = null;
        this.updateOperation = null;
        this.saveOperation = null;
        this.providerOperation = null;
        this.findByOperation = null;
        this.deleteOperation = null;
        this.findAllOperation = null;
        this.countByOperation = null;
        this.countAllOperation = null;
        this.existsByOperation = null;
        this.deleteByOperation = null;
        this.parameterBasedOperation = null;
        this.cursorPaginationOperation = null;
        this.queryOperation = null;
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
