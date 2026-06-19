/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.converter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Converter;
import jakarta.nosql.MappingException;
import org.eclipse.jnosql.lite.mapping.metadata.AutoApplyConverterMetadata;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

@SupportedAnnotationTypes("jakarta.nosql.Converter")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AutoApplyConverterProcessor extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(AutoApplyConverterProcessor.class.getName());
    private static final String AUTO_APPLY_CONVERTERS
            = "org.eclipse.jnosql.lite.mapping.converter.AutoApplyConverters";
    private static final String TEMPLATE = "auto_apply_converter.mustache";

    private final Mustache template;

    public AutoApplyConverterProcessor() {
        this.template = createTemplate();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<String> services = new HashSet<>();

        roundEnv.getElementsAnnotatedWith(Converter.class)
                .stream()
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .filter(this::isAutoApply)
                .forEach(e -> generateMetadata(e, services));

        if (roundEnv.processingOver() && !services.isEmpty()) {
            generateServiceFile(services);
        }
        return false;
    }

    private void generateMetadata(TypeElement converter, Set<String> services) {

        DeclaredType attributeConverter =
                attributeConverter(converter);

        if (attributeConverter == null) {
            LOGGER.warning(() ->
                    "Ignoring converter "
                            + converter.getQualifiedName()
                            + " because it does not implement "
                            + AttributeConverter.class.getName());
            return;
        }

        var attributeType = attributeType(attributeConverter);
        var converterType = converter.getQualifiedName().toString();
        var className = converter.getSimpleName() + "AutoApplyMetadata";

        var model = new AutoApplyConverterModel(className, attributeType, converterType);

        generateMetadataClass(model);
        services.add(model.getQualified());
    }

    private void generateMetadataClass(AutoApplyConverterModel model) {

        try {
            var filer = processingEnv.getFiler();
            var fileObject = filer.createSourceFile(model.getQualified());

            try (var writer = fileObject.openWriter()) {
                template.execute(writer, model);
            }

        } catch (IOException e) {
            throw new MappingException(
                    "Error creating auto apply metadata",
                    e);
        }
    }

    private void generateServiceFile(Set<String> services) {

        try {

            var resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                                    "",
                                    "META-INF/services/" + AutoApplyConverterMetadata.class.getName());

            try (Writer writer = resource.openWriter()) {

                for (String service : services) {
                    writer.write(service);
                    writer.write(System.lineSeparator());
                }
            }

        } catch (IOException e) {
            throw new MappingException(
                    "Error creating service file",
                    e);
        }
    }

    private boolean isAutoApply(TypeElement converter) {
        Converter annotation = converter.getAnnotation(Converter.class);
        return annotation != null && annotation.autoApply();
    }

    private DeclaredType attributeConverter(TypeElement converter) {

        for (TypeMirror mirror : converter.getInterfaces()) {

            if (!(mirror instanceof DeclaredType declaredType)) {
                continue;
            }

            Element element = declaredType.asElement();

            if (element instanceof TypeElement typeElement
                    && typeElement.getQualifiedName()
                    .contentEquals(AttributeConverter.class.getName())) {
                return declaredType;
            }
        }

        return null;
    }

    private String attributeType(DeclaredType converter) {
        return processingEnv.getTypeUtils()
                .erasure(converter.getTypeArguments().get(0))
                .toString();
    }

    private Mustache createTemplate() {
        MustacheFactory factory = new DefaultMustacheFactory();
        return factory.compile(TEMPLATE);
    }

}
