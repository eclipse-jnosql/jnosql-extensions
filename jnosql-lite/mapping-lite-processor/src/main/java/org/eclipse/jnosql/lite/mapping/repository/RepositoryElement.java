/*
 *  Copyright (c) 2021 Otávio Santana and others
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.lite.mapping.repository;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import org.eclipse.jnosql.lite.mapping.ValidationException;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.NoSQLRepository;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.eclipse.jnosql.lite.mapping.ProcessorUtil.isTypeElement;
import static org.eclipse.jnosql.lite.mapping.repository.RepositoryUtil.findRepository;

class RepositoryElement {

    private final TypeElement element;
    private final String entityType;
    private final String keyType;
    private final String repositoryInterface;
    private final DatabaseType type;
    private final boolean isNoSQLRepository;
    private final boolean isCrudRepository;
    private final List<MethodMetadata> methods;
    private final List<MethodSignatureKeyConstant> methodSignatureKeys;

    public RepositoryElement(TypeElement element,
                             String entityType, String keyType,
                             String repositoryInterface,
                             DatabaseType type,
                             boolean isNoSQLRepository,
                             boolean isCrudRepository,
                             List<MethodMetadata> methods,
                             List<MethodSignatureKeyConstant> methodSignatureKeys) {
        this.element = element;
        this.entityType = entityType;
        this.keyType = keyType;
        this.repositoryInterface = repositoryInterface;
        this.type = type;
        this.isNoSQLRepository = isNoSQLRepository;
        this.isCrudRepository = isCrudRepository;
        this.methods = methods;
        this.methodSignatureKeys = methodSignatureKeys;
    }

    public String getClassName() {
        return element.toString();
    }

    public DatabaseType getType() {
        return type;
    }

    public RepositoryMetadata getMetadata(DatabaseType type) {
        return switch (type) {
            case DOCUMENT -> new SemiStructureRepositoryMetadata(this, "Document");
            case COLUMN -> new SemiStructureRepositoryMetadata(this, "Column");
            case GRAPH -> new SemiStructureRepositoryMetadata(this, "Graph");
            case KEY_VALUE -> new KeyValueRepositoryMetadata(this);
            default -> throw new UnsupportedOperationException("There is not template to this database type: " + type);
        };
    }

    public String getEntityType() {
        return entityType;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getSimpleName() {
        return this.element.getSimpleName().toString();
    }

    public String getPackage() {
        String qualifiedName = this.element.getQualifiedName().toString();
        int index = qualifiedName.lastIndexOf('.');
        return qualifiedName.substring(0, index);
    }

    public String getRepository() {
        return repositoryInterface;
    }

    public List<MethodMetadata> getMethods() {
        return this.methods;
    }

    public List<MethodSignatureKeyConstant> getMethodSignatureKeys() {
        return methodSignatureKeys;
    }


    static RepositoryElement of(Element element, ProcessingEnvironment processingEnv, DatabaseType type) {
        if (isTypeElement(element)) {
            TypeElement typeElement = (TypeElement) element;
            Optional<TypeMirror> mirror = findRepository(typeElement.getInterfaces(), processingEnv);
            if (mirror.isPresent()) {

                boolean isNoSQLRepository = RepositoryUtil.isNoSQLRepository(typeElement.getInterfaces(), processingEnv);
                boolean isCrudRepository = RepositoryUtil.isCrudRepository(typeElement.getInterfaces(), processingEnv);

                TypeMirror typeMirror = mirror.get();
                List<String> parameters = RepositoryUtil.findParameters(typeMirror);
                List<MethodSignatureKeyConstant> methodSignatureKeys =
                        MethodSignatureKeyExtractor.extract(typeElement.getEnclosedElements(), processingEnv);
                String entityType = parameters.get(0);
                String keyType = parameters.get(1);
                String repositoryInterface = typeElement.getQualifiedName().toString();
                List<MethodMetadata> methods = typeElement.getEnclosedElements()
                        .stream()
                        .map(e -> MethodMetadata.of(e, entityType, type, processingEnv))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                return new RepositoryElement(typeElement,
                        entityType,
                        keyType,
                        repositoryInterface,
                        type,
                        isNoSQLRepository,
                        isCrudRepository,
                        methods,
                        methodSignatureKeys);
            }
        }
        throw new ValidationException("The interface %s must extends %s"
                .formatted(element.toString(), String.join(" or ",
                        BasicRepository.class.getName(), CrudRepository.class.getName(), NoSQLRepository.class.getName())));
    }

    public boolean isNoSQLRepository() {
        return isNoSQLRepository;
    }

    public boolean isCrudRepository() {
        return isCrudRepository;
    }

}
