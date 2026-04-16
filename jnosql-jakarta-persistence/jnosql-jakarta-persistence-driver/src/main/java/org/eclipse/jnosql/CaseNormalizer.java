/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 */
package org.eclipse.jnosql;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;


interface CaseNormalizer {

    Expression<String> field(Expression<String> field, CriteriaBuilder cb);
    String value(String value);

    static CaseNormalizer identity() {
        return new CaseNormalizer() {
            public Expression<String> field(Expression<String> field, CriteriaBuilder cb) {
                return field;
            }
            public String value(String value) {
                return value;
            }
        };
    }

    static CaseNormalizer upper() {
        return new CaseNormalizer() {
            public Expression<String> field(Expression<String> field, CriteriaBuilder cb) {
                return cb.upper(field);
            }
            public String value(String value) {
                return value.toUpperCase();
            }
        };
    }
}