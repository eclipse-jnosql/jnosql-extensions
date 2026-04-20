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

import jakarta.data.expression.Expression;
import jakarta.data.metamodel.BasicAttribute;
import jakarta.data.spi.expression.literal.Literal;
import org.eclipse.jnosql.communication.Value;

import java.util.function.Supplier;

enum SqlValueConverter {
    INSTANCE;

    static Object of(Supplier<Expression<?, ?>> supplier, BasicAttribute<?, ?> basicAttribute) {
        var expression = supplier.get();
        var literal = getLiteral(expression);
        return Value.of(literal.value()).get(basicAttribute.type());
    }

    private static Literal<?> getLiteral(Expression<?, ?> expression) {
        if(expression instanceof Literal<?> literal) {
            return literal;
        } else {
            throw new UnsupportedOperationException("Currently only Literal values are supported for EqualTo constraints, but got: " + expression);
        }
    }

}
