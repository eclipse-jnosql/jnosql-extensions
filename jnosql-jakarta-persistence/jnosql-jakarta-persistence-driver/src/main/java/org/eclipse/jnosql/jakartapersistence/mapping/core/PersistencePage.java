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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A Jakarta Persistence implementation of {@link  Page}
 *
 * TODO: Support lazy loading of entities, e.g. via stream. Needs to know total
 * elements upfront, e.g. retrieved with another count(*) query, if
 * totalElements requested in paging
 *
 * @param <T> the entity type
 */
public class PersistencePage<T> implements Page<T> {

    private final List<T> entities;

    private final PageRequest pageRequest;

    private final Long totalElements;

    private PersistencePage(List<T> entities, Long totalElements, PageRequest pageRequest) {
        this.entities = entities;
        this.totalElements = totalElements;
        this.pageRequest = pageRequest;
    }

    @Override
    public long totalElements() {
        if (totalElements == null) {
            throw new IllegalStateException("Page request did not request to retrieve total number elements");
        }
        return totalElements;
    }

    @Override
    public long totalPages() {
        final long remainder = totalElements() % pageRequest.size();
        return totalElements() / pageRequest.size() + ((remainder > 0) ? 1 : 0);
    }

    @Override
    public List<T> content() {
        return Collections.unmodifiableList(entities);
    }

    @Override
    public boolean hasContent() {
        return !this.entities.isEmpty();
    }

    @Override
    public int numberOfElements() {
        return entities.size();
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
        return false; // TODO support pageRequest.requestTotal
    }

    @Override
    public Iterator<T> iterator() {
        return this.entities.iterator();
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
        PersistencePage<?> persistencePage = (PersistencePage<?>) o;
        return Objects.equals(entities, persistencePage.entities) && Objects.equals(pageRequest, persistencePage.pageRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entities, pageRequest);
    }

    @Override
    public String toString() {
        return "PersistencePage{"
                + "entities=" + entities
                + ", totalElements=" + totalElements
                + ", pageRequest=" + pageRequest
                + '}';
    }

    /**
     * Creates a {@link  Page} implementation from entities and a PageRequest
     *
     * @param entities the entities
     * @param pageRequest the PageRequest
     * @return a {@link Page} instance
     * @param <T> the entity type
     */
    public static <T> Page<T> of(List<T> entities, PageRequest pageRequest) {
        Objects.requireNonNull(entities, "entities is required");
        Objects.requireNonNull(pageRequest, "pageRequest is required");
        return new PersistencePage<>(entities, null, pageRequest);
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
