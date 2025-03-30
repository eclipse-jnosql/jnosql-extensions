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
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.nosql.Convert;
import jakarta.nosql.Embeddable;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class FieldAnalyzer implements Supplier<String> {
    private static final Logger LOGGER = Logger.getLogger(FieldAnalyzer.class.getName());
    private static final String DEFAULT_TEMPLATE = "field_metadata.mustache";
    private static final String COLLECTION_TEMPLATE = "field_collection_metadata.mustache";
    private static final String MAP_TEMPLATE = "field_map_metadata.mustache";
    private static final String ARRAY_TEMPLATE = "field_array_metadata.mustache";
    private static final Predicate<Element> IS_METHOD = el -> el.getKind() == ElementKind.METHOD;
    private static final Function<Element, String> ELEMENT_TO_STRING = el -> el.getSimpleName().toString();
    private static final String NULL = "null";
    private static final Mustache MUSTACHE_DEFAULT_TEMPLATE;
    private static  final Mustache MUSTACHE_COLLECTION_TEMPLATE;
    private static  final Mustache MUSTACHE_MAP_TEMPLATE;
    private static  final Mustache MUSTACHE_ARRAY_TEMPLATE;

    private final Element field;
    private final ProcessingEnvironment processingEnv;
    private final TypeElement entity;


    static {
        MUSTACHE_DEFAULT_TEMPLATE = createTemplate(DEFAULT_TEMPLATE);
        MUSTACHE_COLLECTION_TEMPLATE = createTemplate(COLLECTION_TEMPLATE);
        MUSTACHE_MAP_TEMPLATE = createTemplate(MAP_TEMPLATE);
        MUSTACHE_ARRAY_TEMPLATE = createTemplate(ARRAY_TEMPLATE);
    }

    FieldAnalyzer(Element field, ProcessingEnvironment processingEnv,
                  TypeElement entity) {
        this.field = field;
        this.processingEnv = processingEnv;
        this.entity = entity;

    }

    @Override
    public String get() {
        FieldModel metadata = getMetaData();
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = getFileObject(metadata, filer);
        try (Writer writer = fileObject.openWriter()) {
            if (NULL.equals(metadata.getElementType())) {
                MUSTACHE_DEFAULT_TEMPLATE.execute(writer, metadata);
            } else if (metadata.getType().contains("Map")) {
                MUSTACHE_MAP_TEMPLATE.execute(writer, metadata);
            } else if(metadata.getType().contains("[]")) {
                MUSTACHE_ARRAY_TEMPLATE.execute(writer, metadata);
            } else {
                MUSTACHE_COLLECTION_TEMPLATE.execute(writer, metadata);
            }
        } catch (IOException exception) {
            throw new ValidationException("An error to compile the class: " +
                    metadata.getQualified(), exception);
        }
        return metadata.getQualified();
    }

    private JavaFileObject getFileObject(FieldModel metadata, Filer filer) {
        try {
            return filer.createSourceFile(metadata.getQualified(), entity);
        } catch (IOException exception) {
            throw new ValidationException("An error to create the class: " +
                    metadata.getQualified(), exception);
        }

    }

    private FieldModel getMetaData() {
        final String fieldName = field.getSimpleName().toString();
        LOGGER.finest("Processing the field: " + fieldName);
        final Predicate<Element> validName = el -> el.getSimpleName().toString()
                .contains(ProcessorUtil.capitalize(fieldName));
        final Predicate<String> hasGetterName = el -> el.equals("get" + ProcessorUtil.capitalize(fieldName));
        final Predicate<String> hasRecordStyleName = el -> el.equals(fieldName);
        final Predicate<String> hasSetterName = el -> el.equals("set" + ProcessorUtil.capitalize(fieldName));
        final Predicate<String> hasIsName = el -> el.equals("is" + ProcessorUtil.capitalize(fieldName));

        final List<Element> accessors = processingEnv.getElementUtils()
                .getAllMembers(entity).stream()
                .filter(validName.and(IS_METHOD).and(EntityProcessor.HAS_ACCESS))
                .collect(Collectors.toList());

        final TypeMirror typeMirror = field.asType();
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
            embeddable = isEmbeddable(declaredType);
            collectionInstance = CollectionUtil.INSTANCE.apply(className);
            elementType = elementType(declaredType);
            valuetype = valuetype(declaredType);
            mappingType = of(element, collectionInstance);

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

        var column = field.getAnnotation(Column.class);
        Id id = field.getAnnotation(Id.class);
        var convert = field.getAnnotation(Convert.class);

        List<ValueAnnotationModel> valueAnnotationModels = new ArrayList<>();
        for (AnnotationMirror annotationMirror : field.getAnnotationMirrors()) {
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
                String key = annotationType.toString() + ".class";
                String value = e.getValue().getValue().toString();
                ValueAnnotationModel valueAnnotationModel = new ValueAnnotationModel(key, value);
                valueAnnotationModels.add(valueAnnotationModel);
            });
        }

        final boolean isId = id != null;
        final String packageName = ProcessorUtil.getPackageName(entity);
        final String entityName = ProcessorUtil.getSimpleNameAsString(this.entity);
        final String name = getName(fieldName, column, id);
        final String udt = column != null ? column.udt() : null;

        var isRecord =  !this.entity.getRecordComponents().isEmpty();
        final String getMethod;

        if(isRecord) {
            getMethod = fieldName;
        } else {
            getMethod = accessors.stream()
                .map(ELEMENT_TO_STRING)
                .filter(hasGetterName.or(hasRecordStyleName))
                .findFirst().orElseThrow(generateGetterError(fieldName, packageName, entityName,
                        "There is not valid getter method to the field: "));
        }

        final String setMethod = accessors.stream()
                .map(ELEMENT_TO_STRING)
                .filter(hasSetterName.or(hasIsName))
                .findFirst().orElse(null);

        return FieldModel.builder()
                .packageName(packageName)
                .name(name)
                .type(className)
                .entity(entityName)
                .reader(getMethod)
                .writer(setMethod)
                .fieldName(fieldName)
                .udt(udt)
                .id(isId)
                .elementType(elementType)
                .valueType(valuetype)
                .converter(convert)
                .embeddable(embeddable)
                .mappingType("MappingType." + mappingType.name())
                .valueByAnnotation(valueAnnotationModels)
                .collectionInstance(collectionInstance)
                .supplierElement(supplierElement)
                .newArrayInstance(newArrayInstance)
                .arrayElement(arrayElement)
                .build();
    }

    private String getName(String fieldName, Column column, Id id) {
        if (id == null) {
            return column.value().isBlank() ? fieldName : column.value();
        } else {
            return id.value().isBlank() ? fieldName : id.value();
        }
    }

    private Supplier<ValidationException> generateGetterError(String fieldName, String packageName, String entity, String s) {
        return () -> new ValidationException(s + fieldName + " in the class: " + packageName + "." + entity);
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

    private String valuetype(DeclaredType declaredType) {
        Optional<? extends TypeMirror> genericMirrorOptional = declaredType.getTypeArguments().stream().skip(1L).findFirst();
        if (genericMirrorOptional.isPresent()) {
            TypeMirror genericMirror = genericMirrorOptional.get();
            return genericMirror + ".class";
        } else {
            return NULL;
        }
    }

    private boolean isEmbeddable(DeclaredType declaredType) {
        return declaredType.getTypeArguments().stream()
                .filter(DeclaredType.class::isInstance).map(DeclaredType.class::cast)
                .map(DeclaredType::asElement).findFirst().map(e -> e.getAnnotation(Embeddable.class) != null ||
                        e.getAnnotation(Entity.class) != null).orElse(false);
    }

}
