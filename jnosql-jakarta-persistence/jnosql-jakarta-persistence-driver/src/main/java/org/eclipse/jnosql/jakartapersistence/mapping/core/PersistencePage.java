/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Otavio Santana
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping.core;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A Jakarta Persistence implementation of {@link  Page}
 *
 * TODO: Load eagerly if not in a global transaction - lazy loading is not possible after a transaction is committed.
 * TODO: Investigate whether lazy loading can be implemented using cursors, e.g. via Stream instead of List
 *
 * @param <T> the entity type
 */
public class PersistencePage<T> implements Page<T> {

    private final Query query;

    private final Supplier<TypedQuery<Long>> countQuerySupplier;

    private TypedQuery<Long> suppliedCountQuery;

    private final PageRequest pageRequest;

    private List<T> entities;

    private Long totalElements;

    /**
     *
     * @param query Query to retrieve entities
     * @param countQuerySupplier Supplies a query to retrieve the total number of entities.
     * {@code null} value means {@link #totalElements()} method is not supported.
     * Must be non-null if {@code pageRequest.requestTotal()} returns {@code true}.
     * @param pageRequest Defines which page to retrieve from the entities defined by {@code query}
     */
    public PersistencePage(Query query, Supplier<TypedQuery<Long>> countQuerySupplier, PageRequest pageRequest) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(pageRequest, "pageRequest is required");
        if (pageRequest.requestTotal()) {
            Objects.requireNonNull(countQuerySupplier, "countQuerySupplier is required if totals are requested");
        }
        this.query = query;
        this.countQuerySupplier = countQuerySupplier;
        this.pageRequest = pageRequest;
    }

    @Override
    public long totalElements() {
        if (countQuerySupplier == null) {
            throw new IllegalStateException("Page request did not request retrieving the total number of elements. Total number of elements is not available.");
        }
        if (suppliedCountQuery == null) {
            suppliedCountQuery = countQuerySupplier.get();
        }
        if (totalElements == null) {
            totalElements = suppliedCountQuery.getResultList().get(0);
        }
        return totalElements;
    }

    private List<T> entities() {
        if (entities == null) {
            entities = query.getResultList();
        }
        return entities;
    }

    @Override
    public long totalPages() {
        final long remainder = totalElements() % pageRequest.size();
        return totalElements() / pageRequest.size() + ((remainder > 0) ? 1 : 0);
    }

    @Override
    public List<T> content() {
        return entities();
    }

    @Override
    public boolean hasContent() {
        return !this.entities().isEmpty();
    }

    @Override
    public int numberOfElements() {
        return entities().size();
    }

    @Override
    public boolean hasNext() {
        return numberOfElements() >= pageRequest.size();
    }

    @Override
    public boolean hasPrevious() {
        return hasContent() && pageRequest.page() > 1;
    }

    @Override
    public PageRequest pageRequest() {
        return this.pageRequest;
    }

    @Override
    public PageRequest nextPageRequest() {
        final long nextPage = pageRequest.page() + 1;
        if (hasNext()) {
            return PageRequest.ofPage(nextPage, this.pageRequest.size(), this.pageRequest.requestTotal());
        } else {
            throw new NoSuchElementException("No elements for the next page number " + nextPage);
        }
    }

    @Override
    public PageRequest previousPageRequest() {
        final long previousPage = pageRequest.page() - 1;
        if (hasPrevious()) {
            return PageRequest.ofPage(previousPage, this.pageRequest.size(), this.pageRequest.requestTotal());
        } else {
            throw new NoSuchElementException("No elements for the previous page number " + previousPage);
        }
    }

    @Override
    public boolean hasTotals() {
        return countQuerySupplier != null;
    }

    @Override
    public Iterator<T> iterator() {
        return this.entities().iterator();
    }

    @Override
    public boolean equals(Object o
    ) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PersistencePage<?> otherPage = (PersistencePage<?>) o;
        totalElementsEqual(otherPage);
        return Objects.equals(entities(), otherPage.entities())
                && Objects.equals(query, otherPage.query)
                && Objects.equals(pageRequest, otherPage.pageRequest)
                && (countQueriesBothNull(otherPage)
                    || (countQueriesBothNotNull(otherPage) && totalElementsEqual(otherPage))
                );
    }

    private boolean totalElementsEqual(PersistencePage<?> otherPage) {
        return totalElements() == otherPage.totalElements();
    }

    private boolean countQueriesBothNotNull(PersistencePage<?> otherPage) {
        return countQuerySupplier != null && otherPage.countQuerySupplier != null;
    }

    private boolean countQueriesBothNull(PersistencePage<?> otherPage) {
        return countQuerySupplier == null && otherPage.countQuerySupplier == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, pageRequest);
    }

    @Override
    public String toString() {
        return "PersistencePage{"
                + "query=" + query
                + ", pageRequest=" + pageRequest
                + '}';
    }

    /**
     * Create skip formula from pageRequest instance
     *
     * @param pageRequest the pageRequest
     * @param <T> the entity type
     * @return the skip
     * @throws NullPointerException when parameter is null
     */
    public static <T> long skip(PageRequest pageRequest) {
        Objects.requireNonNull(pageRequest, "pageRequest is required");
        return pageRequest.size() * (pageRequest.page() - 1);
    }
}
