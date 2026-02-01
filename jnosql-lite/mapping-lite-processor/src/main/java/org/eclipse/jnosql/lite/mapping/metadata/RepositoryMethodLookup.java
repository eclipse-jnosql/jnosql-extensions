package org.eclipse.jnosql.lite.mapping.metadata;

import org.eclipse.jnosql.mapping.metadata.repository.MethodKey;
import org.eclipse.jnosql.mapping.metadata.repository.MethodSignatureKey;
import org.eclipse.jnosql.mapping.metadata.repository.NameKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;

/**
 * Describes the lookup representations available for a single repository method.
 * <p>
 * This interface exposes the supported {@link org.eclipse.jnosql.mapping.metadata.repository.MethodKey} variants that can be
 * used to locate this repository method via
 * {@link RepositoryMetadata#find(MethodKey)}.
 * Implementations typically use these lookups to build internal indexes or maps
 * for efficient method resolution.
 */
public interface RepositoryMethodLookup extends RepositoryMethod {

    /**
     * Returns a signature-based lookup key composed of the method name and
     * erased parameter types.
     *
     * @return the signature-based {@link MethodSignatureKey}
     */
    MethodSignatureKey bySignature();

    /**
     * Returns a name-based lookup key that matches methods solely by name.
     * <p>
     * This lookup is intended for simplified or best-effort resolution scenarios
     * and may be ambiguous when method overloading is present.
     *
     * @return the name-based {@link NameKey}
     */
    NameKey byName();
}

