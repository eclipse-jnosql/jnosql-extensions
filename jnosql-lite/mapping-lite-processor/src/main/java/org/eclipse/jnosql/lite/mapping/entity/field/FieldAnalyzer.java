/*
 *  Copyright (c) 2020 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entity.field;

import org.eclipse.jnosql.lite.mapping.processing.ProcessorUtils;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class FieldAnalyzer implements Supplier<String> {

    private static final Logger LOGGER = Logger.getLogger(FieldAnalyzer.class.getName());

    private final Element field;
    private final TypeElement entity;
    private final FieldTypeIntrospector typeIntrospector;
    private final FieldAccessorIntrospector accessorIntrospector;
    private final FieldAnnotationIntrospector annotationIntrospector;
    private final FieldSourceGenerator sourceGenerator;

    public FieldAnalyzer(Element field, ProcessingEnvironment processingEnv,
                         TypeElement entity) {
        this.field = field;
        this.entity = entity;
        this.typeIntrospector = new FieldTypeIntrospector(field);
        this.accessorIntrospector = new FieldAccessorIntrospector(field, processingEnv, entity);
        this.annotationIntrospector = new FieldAnnotationIntrospector(field);
        this.sourceGenerator = new FieldSourceGenerator(processingEnv);
    }

    @Override
    public String get() {
        FieldModel metadata = metadata();
        sourceGenerator.generate(entity, metadata);
        return metadata.getQualified();
    }

    private FieldModel metadata() {
        final String fieldName = field.getSimpleName().toString();
        LOGGER.finest("Processing the field: " + fieldName);
        var typeMetadata = typeIntrospector.introspect();
        var accessorMetadata = accessorIntrospector.introspect();
        var annotationMetadata = annotationIntrospector.introspect(fieldName);
        final String packageName = ProcessorUtils.packageName(entity);
        final String entityName = ProcessorUtils.simpleName(this.entity);

        return FieldModel.builder()
                .packageName(packageName)
                .name(annotationMetadata.name())
                .type(typeMetadata.type())
                .entity(entityName)
                .reader(accessorMetadata.reader())
                .writer(accessorMetadata.writer())
                .fieldName(fieldName)
                .udt(annotationMetadata.udt())
                .id(annotationMetadata.id())
                .elementType(typeMetadata.elementType())
                .valueType(typeMetadata.valueType())
                .converter(annotationMetadata.converter())
                .embeddable(typeMetadata.embeddable())
                .mappingType("MappingType." + typeMetadata.mappingType().name())
                .valueByAnnotation(annotationMetadata.valueAnnotations())
                .collectionInstance(typeMetadata.collectionInstance())
                .supplierElement(typeMetadata.supplierElement())
                .newArrayInstance(typeMetadata.newArrayInstance())
                .arrayElement(typeMetadata.arrayElement())
                .build();
    }
}
