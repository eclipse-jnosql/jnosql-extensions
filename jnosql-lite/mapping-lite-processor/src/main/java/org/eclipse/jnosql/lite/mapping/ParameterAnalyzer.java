/*
 *  Copyright (c) 2020 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

class ParameterAnalyzer implements Supplier<ParameterResult> {

    private static final Predicate<VariableElement> ID_PARAMETER = v -> v.getAnnotation(Id.class) != null;
    private static final Predicate<VariableElement> COLUMN_PARAMETER = v -> v.getAnnotation(Column.class) != null;

    static final Predicate<Element> INJECT_CONSTRUCTOR = e -> {
        ExecutableElement executableElement = (ExecutableElement) e;
        return executableElement.getParameters().stream().anyMatch(ID_PARAMETER.or(COLUMN_PARAMETER));
    };

    private static final Mustache MUSTACHE_DEFAULT_TEMPLATE;
    private static final Mustache MUSTACHE_COLLECTION_TEMPLATE;
    private static final Mustache MUSTACHE_MAP_TEMPLATE;
    private static final Mustache MUSTACHE_ARRAY_TEMPLATE;
    private static final Logger LOGGER = Logger.getLogger(ParameterAnalyzer.class.getName());
    private static final String DEFAULT_TEMPLATE = "parameter_metadata.mustache";
    private static final String COLLECTION_TEMPLATE = "parameter_collection_metadata.mustache";
    private static final String MAP_TEMPLATE = "parameter_map_metadata.mustache";
    private static final String ARRAY_TEMPLATE = "parameter_array_metadata.mustache";
    private static final String NULL = "null";

    private final VariableElement parameter;
    private final ProcessingEnvironment processingEnv;
    private final TypeElement entity;

    static {
        MUSTACHE_DEFAULT_TEMPLATE = createTemplate(DEFAULT_TEMPLATE);
        MUSTACHE_COLLECTION_TEMPLATE = createTemplate(COLLECTION_TEMPLATE);
        MUSTACHE_MAP_TEMPLATE = createTemplate(MAP_TEMPLATE);
        MUSTACHE_ARRAY_TEMPLATE = createTemplate(ARRAY_TEMPLATE);
    }

    ParameterAnalyzer(VariableElement parameter, ProcessingEnvironment processingEnv,
                      TypeElement entity) {
        this.parameter = parameter;
        this.processingEnv = processingEnv;
        this.entity = entity;
    }

    @Override
    public ParameterResult get() {
        var metadata = getMetaData();
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = getFileObject(metadata, filer);
        try (Writer writer = fileObject.openWriter()) {
            if (NULL.equals(metadata.getElementType())) {
                MUSTACHE_DEFAULT_TEMPLATE.execute(writer, metadata);
            } else if (metadata.getType().contains("Map")) {
                MUSTACHE_MAP_TEMPLATE.execute(writer, metadata);
            } else if (metadata.getType().contains("[]")) {
                MUSTACHE_ARRAY_TEMPLATE.execute(writer, metadata);
            } else {
                MUSTACHE_COLLECTION_TEMPLATE.execute(writer, metadata);
            }
        } catch (IOException exception) {
            throw new ValidationException("An error to compile the class: " +
                    metadata.getQualified(), exception);
        }

        return new ParameterResult(metadata.getQualified(), metadata.getType());
    }

    private JavaFileObject getFileObject(ParameterModel metadata, Filer filer) {
        try {
            return filer.createSourceFile(metadata.getQualified(), entity);
        } catch (IOException exception) {
            throw new ValidationException("An error to create the class: " +
                    metadata.getQualified(), exception);
        }

    }

    private ParameterModel getMetaData() {
        final String fieldName = parameter.getSimpleName().toString();
        LOGGER.finest("Processing the parameter: " + fieldName);

        final TypeMirror typeMirror = parameter.asType();
        String className;
        String elementType = NULL;
        String valuetype = NULL;
        boolean embeddable = false;
        String collectionInstance = CollectionUtil.DEFAULT;
        MappingType mappingType = MappingType.DEFAULT;
        String supplierElement = null;
        String newArrayInstance = null;
        String arrayElement = null;


        if (typeMirror instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            className = element.toString();
            supplierElement = typeMirror.toString();
            collectionInstance = CollectionUtil.INSTANCE.apply(className);
            elementType = elementType(declaredType);
            valuetype = valueType(declaredType);
            mappingType = of(element, collectionInstance);
            embeddable = isEmbeddable(declaredType, mappingType);

        } else if (typeMirror instanceof ArrayType arrayType) {
            TypeMirror componentType = arrayType.getComponentType();
            mappingType = MappingType.ARRAY;
            className = typeMirror.toString();
            collectionInstance = CollectionUtil.DEFAULT;

            if (componentType instanceof DeclaredType declaredType) {
                var element = declaredType.asElement();
                supplierElement = componentType.toString();
                embeddable = element.getAnnotation(Entity.class) != null || element.getAnnotation(Embeddable.class) != null;
                elementType = element + ".class";
                arrayElement = element.toString();
                newArrayInstance = className.replace("[]", "[collection.size()]");
            } else {
                className = typeMirror.toString();
                supplierElement = typeMirror.toString();
                arrayElement = componentType.toString();
                elementType = componentType + ".class";
                newArrayInstance = className.replace("[]", "[collection.size()]");
            }
        } else {
            className = typeMirror.toString();
        }

        var column = parameter.getAnnotation(Column.class);
        var id = parameter.getAnnotation(Id.class);
        var convert = parameter.getAnnotation(Convert.class);

        for (AnnotationMirror annotationMirror : parameter.getAnnotationMirrors()) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();
            Predicate<Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> isValueMethod =
                    e -> e.getKey().toString().equals("value()");

            List<String> defaultJNoSQL = List.of(Column.class.getName(),
                    Id.class.getName(),
                    Convert.class.getName());
            Predicate<Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> isNotDefaultAnnotation = e -> !
                    defaultJNoSQL.contains(annotationType.toString());
            elementValues.entrySet().stream().filter(isNotDefaultAnnotation.and(isValueMethod)).findFirst().ifPresent(e -> {
            });
        }

        final boolean isId = id != null;
        final String packageName = ProcessorUtil.getPackageName(entity);
        final String entityName = ProcessorUtil.getSimpleNameAsString(this.entity);
        final String name = getName(fieldName, column, id);
        final String udt = column != null ? column.udt() : null;

        return ParameterModel.builder()
                .packageName(packageName)
                .name(name)
                .type(className)
                .entity(entityName)
                .fieldName(fieldName)
                .udt(udt)
                .id(isId)
                .elementType(elementType)
                .valueType(valuetype)
                .converter(convert)
                .embeddable(embeddable)
                .mappingType("MappingType." + mappingType.name())
                .collectionInstance(collectionInstance)
                .supplierElement(supplierElement)
                .newArrayInstance(newArrayInstance)
                .arrayElement(arrayElement)
                .build();
    }

    private String getName(String fieldName, Column column, Id id) {
        if (id == null) {
            if (column == null) {
                return fieldName;
            }
            return column.value().isBlank() ? fieldName : column.value();
        } else {
            return id.value().isBlank() ? fieldName : id.value();
        }
    }

    private static Mustache createTemplate(String template) {
        MustacheFactory factory = new DefaultMustacheFactory();
        return factory.compile(template);
    }

    private static MappingType of(Element element, String collection) {
        if (element.getAnnotation(Embeddable.class) != null) {
            var type = element.getAnnotation(Embeddable.class).value();
            return Embeddable.EmbeddableType.FLAT.equals(type) ? MappingType.EMBEDDED : MappingType.EMBEDDED_GROUP;
        }
        if (element.getAnnotation(Entity.class) != null) {
            return MappingType.ENTITY;
        }
        if (!collection.equals(CollectionUtil.DEFAULT)) {
            return MappingType.COLLECTION;
        }
        if (element.toString().equals("java.util.Map")) {
            return MappingType.MAP;
        }
        return MappingType.DEFAULT;
    }

    private String elementType(DeclaredType declaredType) {
        Optional<? extends TypeMirror> genericMirrorOptional = declaredType.getTypeArguments().stream().findFirst();
        if (genericMirrorOptional.isPresent()) {
            TypeMirror genericMirror = genericMirrorOptional.get();
            return genericMirror + ".class";
        } else {
            return NULL;
        }
    }

    private String valueType(DeclaredType declaredType) {
        Optional<? extends TypeMirror> genericMirrorOptional = declaredType.getTypeArguments().stream().skip(1L).findFirst();
        if (genericMirrorOptional.isPresent()) {
            TypeMirror genericMirror = genericMirrorOptional.get();
            return genericMirror + ".class";
        } else {
            return NULL;
        }
    }

    private boolean isEmbeddable(DeclaredType declaredType, MappingType mappingType) {
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

        if (typeArguments.isEmpty()) {
            return false;
        }

        if (MappingType.MAP.equals(mappingType)) {
            return typeArguments.stream().skip(1).findFirst()
                    .filter(DeclaredType.class::isInstance)
                    .map(DeclaredType.class::cast)
                    .map(DeclaredType::asElement)
                    .map(this::isAnnotatedWithEmbeddableOrEntity)
                    .orElse(false);
        }

        return typeArguments.stream()
                .filter(DeclaredType.class::isInstance)
                .map(DeclaredType.class::cast)
                .map(DeclaredType::asElement)
                .map(this::isAnnotatedWithEmbeddableOrEntity)
                .findFirst()
                .orElse(false);
    }

    private boolean isAnnotatedWithEmbeddableOrEntity(Element element) {
        return element.getAnnotation(Embeddable.class) != null ||
                element.getAnnotation(Entity.class) != null;
    }

}
