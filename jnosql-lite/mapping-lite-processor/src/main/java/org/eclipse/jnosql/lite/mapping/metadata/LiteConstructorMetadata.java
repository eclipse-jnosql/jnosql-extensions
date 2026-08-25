/*
 *  Copyright (c) 2024 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.metadata;

import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;

/**
 * A lightweight, reflection-free extension of {@link ConstructorMetadata}.
 * <p>
 * This interface is used by JNoSQL Lite to instantiate objects without relying
 * on Java reflection, enabling better performance and compatibility with
 * AOT and native-image environments.
 */
public interface LiteConstructorMetadata extends ConstructorMetadata, LiteConstructorInvoker {

}
