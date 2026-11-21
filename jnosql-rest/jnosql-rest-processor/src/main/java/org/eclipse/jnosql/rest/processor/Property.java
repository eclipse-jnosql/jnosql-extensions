/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.rest.processor;

import javax.lang.model.type.TypeMirror;

/**
 * An attribute of an entity or DTO, as seen by getter, public field or record component access.
 *
 * @param name       the Java attribute name
 * @param type       the attribute type
 * @param writable   whether it can be set through a setter, a public field or a record constructor
 * @param identifier whether it is the identifier of the entity
 */
record Property(String name, TypeMirror type, boolean writable, boolean identifier) {
}
