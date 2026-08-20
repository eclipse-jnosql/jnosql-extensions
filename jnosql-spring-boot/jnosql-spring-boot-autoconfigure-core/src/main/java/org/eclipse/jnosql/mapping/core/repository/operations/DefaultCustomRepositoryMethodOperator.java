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
package org.eclipse.jnosql.mapping.core.repository.operations;

import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryResolver;

import java.lang.reflect.Method;

/**
 * Custom repository method operator implementation using a {@link CustomRepositoryResolver}
 * instead of CDI {@code CDI.current().select(...)}.
 *
 * <p>
 * This class is a copy of the CDI-based {@code DefaultCustomRepositoryMethodOperator} from
 * {@code jnosql-mapping-core}, adapted to work outside a CDI container.
 *
 * @see CustomRepositoryResolver
 */
class DefaultCustomRepositoryMethodOperator implements CustomRepositoryMethodOperator {

    private final CustomRepositoryResolver resolver;

    DefaultCustomRepositoryMethodOperator(CustomRepositoryResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Object invokeCustomRepository(Method method, Object[] params) throws Exception {
        Object customRepository = resolver.resolve(method.getDeclaringClass());
        return method.invoke(customRepository, params);
    }
}