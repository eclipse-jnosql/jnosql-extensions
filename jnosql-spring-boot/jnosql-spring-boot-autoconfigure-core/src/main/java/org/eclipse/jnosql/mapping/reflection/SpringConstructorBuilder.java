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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.data.exceptions.MappingException;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class SpringConstructorBuilder implements ConstructorBuilder {

    private final List<Object> values = new ArrayList<>();

    private final ConstructorMetadata metadata;

    private SpringConstructorBuilder(ConstructorMetadata metadata) {
        this.metadata = metadata;
    }


    @Override
    public List<ParameterMetaData> parameters() {
        return this.metadata.parameters();
    }

    @Override
    public void add(Object value) {
        this.values.add(value);
    }

    @Override
    public void addEmptyParameter() {
        Constructor<?> constructor = ((DefaultConstructorMetadata) this.metadata).constructor();
        Class<?> type = constructor.getParameterTypes()[this.values.size()];
        if (boolean.class.equals(type)) {
            this.values.add(Boolean.FALSE);
        } else if (char.class.equals(type)) {
            this.values.add((char) 0);
        } else if (type.isPrimitive()) {
            this.values.add((byte) 0);
        } else {
            this.values.add(null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T build() {
        Constructor<?> constructor = ((DefaultConstructorMetadata) metadata).constructor();
        try {
            publishEventIfPossible(constructor);
            return (T) constructor.newInstance(values.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new MappingException("There is an issue to create a new instance of this class" +
                    " using this constructor: " + constructor, e);
        }
    }

    private void publishEventIfPossible(Constructor<?> constructor) {
        Optional.ofNullable(JNoSQLSpringContext
                        .getContext())
                .map(ctx -> ctx.getBeansOfType(ApplicationEventPublisher.class).values())
                .flatMap(publishers -> publishers.stream().findFirst())
                .ifPresent(ev ->
                        ev.publishEvent(ConstructorEvent.of(constructor, values.toArray()))
                );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpringConstructorBuilder that = (SpringConstructorBuilder) o;
        return Objects.equals(values, that.values) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, metadata);
    }

    @Override
    public String toString() {
        return "ConstructorBuilder{" +
                "values=" + values +
                ", metadata=" + metadata +
                '}';
    }

    /**
     * Create ConstructorBuilder from the method factory
     *
     * @param metadata the metadata
     * @return the builder instance
     * @throws NullPointerException when metadata is null
     */
    static ConstructorBuilder of(ConstructorMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata is required");
        return new SpringConstructorBuilder(metadata);
    }
}
