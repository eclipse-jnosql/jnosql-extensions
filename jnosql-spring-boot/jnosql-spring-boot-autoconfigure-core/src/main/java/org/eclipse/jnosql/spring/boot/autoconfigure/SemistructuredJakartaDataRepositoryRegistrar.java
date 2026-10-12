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

import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryProducer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

public class SemistructuredJakartaDataRepositoryRegistrar implements BeanDefinitionRegistryPostProcessor {

    private final ClassScanner classScanner;
    private final SemistructuredRepositoryProducer semistructuredRepositoryProducer;
    private final ApplicationContext applicationContext;

    public SemistructuredJakartaDataRepositoryRegistrar(
            ApplicationContext applicationContext,
            ClassScanner classScanner,
            SemistructuredRepositoryProducer semistructuredRepositoryProducer) {
        this.classScanner = classScanner;
        this.semistructuredRepositoryProducer = semistructuredRepositoryProducer;
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        classScanner.repositories().stream()
                .filter(clazz -> clazz.isAnnotationPresent(jakarta.data.repository.Repository.class))
                .forEach(repositoryType -> {
                    registerIfNotAlreadyRegistered(
                            registry,
                            repositoryType.getSimpleName(),
                            rootBeanDefinition(
                                    repositoryType,
                                    () -> semistructuredRepositoryProducer.get(repositoryType,
                                            findStructuredTemplateFor(repositoryType)))
                                    .setLazyInit(true)
                                    .getBeanDefinition());
                });
    }

    private SemiStructuredTemplate findStructuredTemplateFor(Class<?> repositoryType) {
        Database database = repositoryType.getAnnotation(Database.class);
        Map<String, SemiStructuredTemplate> beansOfType = applicationContext.getBeansOfType(SemiStructuredTemplate.class);
        return beansOfType.entrySet().stream()
                .filter(entry ->
                        database != null)
                .filter(entry -> {
                    Database anno = applicationContext.findAnnotationOnBean(entry.getKey(), Database.class);
                    return anno != null && anno.value().equals(database.value());
                })
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(() -> beansOfType.values().stream().findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("No SemiStructuredTemplate bean found for repository " + repositoryType.getName())));

    }


    public static <T> void registerIfNotAlreadyRegistered(BeanDefinitionRegistry registry,
                                                          String beanName,
                                                          BeanDefinition beanDefinition) {
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

}