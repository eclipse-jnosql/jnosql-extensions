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
package org.eclipse.jnosql.spring.boot.autoconfigure;

import org.eclipse.jnosql.communication.SettingsBuilder;

/**
 * Customize a {@link SettingsBuilder} instance before its usage.
 */
public interface SettingsBuilderCustomizer {
    /**
     * Customize the given {@link SettingsBuilder} instance.
     * @param builder the {@link SettingsBuilder} instance to customize
     */
    void customize(SettingsBuilder builder);
}
