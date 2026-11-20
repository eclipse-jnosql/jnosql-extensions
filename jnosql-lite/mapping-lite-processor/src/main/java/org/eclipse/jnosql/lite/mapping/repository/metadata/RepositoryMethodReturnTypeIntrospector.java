/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository.metadata;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

final class RepositoryMethodReturnTypeIntrospector {

    private static final String OPTIONAL_EMPTY = "Optional.empty()";
    private static final String OPTIONAL_CLASS_MASK = "Optional.of(%s.class)";
    private static final String INDENT = "        ";
    private static final int MAX_LINE_LENGTH = 150;
    private static final String COMPLETION_STAGE = "java.util.concurrent.CompletionStage";
    private static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";

    private final ProcessingEnvironment processingEnv;

    RepositoryMethodReturnTypeIntrospector(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    ReturnTypeMetadata introspect(ExecutableElement method) {
        return new ReturnTypeMetadata(returnType(method), elementType(method), returnDescriptor(method));
    }

    /**
     * Describes the declared return type with its type arguments, as {@code ReturnType.of(...)} calls.
     * It is empty when the type has no arguments, because the default
     * {@code RepositoryMethod.returnDescriptor()} already describes such a type from {@code returnType()}.
     */
    private String returnDescriptor(ExecutableElement method) {
        if (method.getReturnType() instanceof DeclaredType declaredType
                && !declaredType.getTypeArguments().isEmpty()) {
            return "Optional.of(" + describe(declaredType, 1) + ")";
        }
        return "";
    }

    /**
     * Renders a {@code ReturnType.of(...)} expression. Nested arguments that do not fit in
     * {@link #MAX_LINE_LENGTH} columns are placed on their own lines, so the generated source stays readable.
     */
    private String describe(TypeMirror type, int depth) {
        return switch (type) {
            case DeclaredType declared -> describe(declared, depth);
            case WildcardType wildcard -> describe(wildcard.getExtendsBound() == null
                    ? objectType() : wildcard.getExtendsBound(), depth);
            case TypeVariable variable -> describe(variable.getUpperBound(), depth);
            default -> "ReturnType.of(%s.class)".formatted(
                    processingEnv.getTypeUtils().erasure(type).toString());
        };
    }

    private String describe(DeclaredType declared, int depth) {
        String raw = rawName(declared);
        var arguments = declared.getTypeArguments();
        if (arguments.isEmpty()) {
            return "ReturnType.of(%s.class)".formatted(raw);
        }
        String inline = "ReturnType.of(%s.class, %s)".formatted(raw, arguments.stream()
                .map(argument -> describe(argument, depth + 1))
                .collect(Collectors.joining(", ")));
        if (inline.length() + INDENT.length() * (depth + 1) <= MAX_LINE_LENGTH && inline.indexOf('\n') < 0) {
            return inline;
        }
        String margin = "\n" + INDENT.repeat(depth + 1);
        return "ReturnType.of(%s.class,%s%s)".formatted(raw, margin, arguments.stream()
                .map(argument -> describe(argument, depth + 1))
                .collect(Collectors.joining("," + margin)));
    }

    private String rawName(DeclaredType declared) {
        TypeElement element = (TypeElement) declared.asElement();
        return element.getQualifiedName().toString();
    }

    private TypeMirror objectType() {
        return processingEnv.getElementUtils().getTypeElement("java.lang.Object").asType();
    }

    private boolean isAsync(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        String name = rawName((DeclaredType) type);
        return COMPLETION_STAGE.equals(name) || COMPLETABLE_FUTURE.equals(name);
    }

    private String returnType(ExecutableElement method) {
        TypeElement returnElement =
                (TypeElement) processingEnv.getTypeUtils().asElement(method.getReturnType());
        return ofNullable(returnElement)
                .map(Object::toString)
                .map(OPTIONAL_CLASS_MASK::formatted)
                .orElse(OPTIONAL_CLASS_MASK.formatted(method.getReturnType().toString()));
    }

    /**
     * Resolves the element type as the reflection mapping does. For an asynchronous return type
     * ({@code CompletionStage} or {@code CompletableFuture}) it is resolved from the type argument, so
     * {@code CompletionStage<Optional<Product>>}, {@code CompletionStage<List<Product>>} and
     * {@code CompletionStage<Product>} all have {@code Product} as element type.
     */
    private String elementType(ExecutableElement method) {
        TypeMirror declared = method.getReturnType();
        if (isAsync(declared)) {
            var arguments = ((DeclaredType) declared).getTypeArguments();
            if (arguments.isEmpty()) {
                return OPTIONAL_EMPTY;
            }
            TypeMirror argument = arguments.get(0);
            String nested = elementOf(argument);
            return nested == null ? OPTIONAL_CLASS_MASK.formatted(erasedName(argument)) : nested;
        }
        return syncElementType(method);
    }

    private String elementOf(TypeMirror type) {
        if (type instanceof DeclaredType declaredType && !declaredType.getTypeArguments().isEmpty()) {
            return OPTIONAL_CLASS_MASK.formatted(declaredType.getTypeArguments().get(0));
        }
        if (type instanceof ArrayType arrayType) {
            return OPTIONAL_CLASS_MASK.formatted(arrayType.getComponentType());
        }
        return null;
    }

    private String erasedName(TypeMirror type) {
        return processingEnv.getTypeUtils().erasure(type).toString();
    }

    private static String syncElementType(ExecutableElement method) {
        if (method.getReturnType() instanceof DeclaredType declaredType) {
            return declaredType.getTypeArguments().stream()
                    .map(Object::toString)
                    .findFirst()
                    .map(OPTIONAL_CLASS_MASK::formatted)
                    .orElse(OPTIONAL_EMPTY);
        }
        if (method.getReturnType() instanceof ArrayType arrayType) {
            return OPTIONAL_CLASS_MASK.formatted(arrayType.getComponentType().toString());
        }
        return OPTIONAL_EMPTY;
    }

    record ReturnTypeMetadata(String returnType, String elementType, String returnDescriptor) {
    }
}
