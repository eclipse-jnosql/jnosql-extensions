/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.lite.mapping.entities;

import jakarta.data.exceptions.MappingException;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.EmailNotification;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.LargeProject;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.Notification;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.NotificationReader;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.Project;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.ProjectManager;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.SmallProject;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.SmsNotification;
import org.eclipse.jnosql.lite.mapping.entities.inheritance.SocialMediaNotification;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, DocumentTemplate.class})
@AddExtensions({DocumentExtension.class})
@AddPackages(LiteEntitiesMetadata.class)
class DocumentEntityConverterInheritanceTest {

    @Inject
    private EntityConverter converter;

    @Nested
    @DisplayName("When converting inherited entities")
    class WhenTheConversion {

        @Test
        @DisplayName("Should convert project to small project")
        void shouldConvertProjectToSmallProject() {
            var entity = CommunicationEntity.of("Project");
            entity.add("_id", "Small Project");
            entity.add("investor", "Otavio Santana");
            entity.add("size", "Small");
            Project project = converter.toEntity(entity);
            assertThat(project.getName()).isEqualTo("Small Project");
            assertThat(project.getClass()).isEqualTo(SmallProject.class);
            SmallProject smallProject = (SmallProject) project;
            assertThat(smallProject.getInvestor()).isEqualTo("Otavio Santana");
        }

        @Test
        @DisplayName("Should convert project to large project")
        void shouldConvertProjectToLargeProject() {
            var entity = CommunicationEntity.of("Project");
            entity.add("_id", "Large Project");
            entity.add("budget", BigDecimal.TEN);
            entity.add("size", "Large");
            Project project = converter.toEntity(entity);
            assertThat(project.getName()).isEqualTo("Large Project");
            assertThat(project.getClass()).isEqualTo(LargeProject.class);
            LargeProject smallProject = (LargeProject) project;
            assertThat(smallProject.getBudget()).isEqualTo(BigDecimal.TEN);
        }

        @Test
        @DisplayName("Should convert large project to communication entity")
        void shouldConvertLargeProjectToCommunicationEntity() {
            LargeProject project = new LargeProject();
            project.setName("Large Project");
            project.setBudget(BigDecimal.TEN);
            var entity = converter.toCommunication(project);
            assertThat(entity).isNotNull();
            assertThat(entity.name()).isEqualTo("Project");
            assertThat(entity.find("_id", String.class).get()).isEqualTo(project.getName());
            assertThat(entity.find("budget", BigDecimal.class).get()).isEqualTo(project.getBudget());
            assertThat(entity.find("size", String.class).get()).isEqualTo("Large");
        }

        @Test
        @DisplayName("Should convert small project to communication entity")
        void shouldConvertSmallProjectToCommunicationEntity() {
            SmallProject project = new SmallProject();
            project.setName("Small Project");
            project.setInvestor("Otavio Santana");
            var entity = converter.toCommunication(project);
            assertThat(entity).isNotNull();
            assertThat(entity.name()).isEqualTo("Project");
            assertThat(entity.find("_id", String.class).get()).isEqualTo(project.getName());
            assertThat(entity.find("investor", String.class).get()).isEqualTo(project.getInvestor());
            assertThat(entity.find("size", String.class).get()).isEqualTo("Small");
        }

        @Test
        @DisplayName("Should convert project")
        void shouldConvertProject() {
            var entity = CommunicationEntity.of("Project");
            entity.add("_id", "Project");
            entity.add("size", "Project");
            Project project = converter.toEntity(entity);
            assertThat(project.getName()).isEqualTo("Project");
        }

        @Test
        @DisplayName("Should convert project to communication entity")
        void shouldConvertProjectToCommunicationEntity() {
            Project project = new Project();
            project.setName("Large Project");
            var entity = converter.toCommunication(project);
            assertThat(entity).isNotNull();
            assertThat(entity.name()).isEqualTo("Project");
            assertThat(entity.find("_id", String.class).get()).isEqualTo(project.getName());
            assertThat(entity.find("size", String.class).get()).isEqualTo("Project");
        }

        @Test
        @DisplayName("Should convert document entity to social media notification")
        void shouldConvertDocumentEntityToSocialMedia(){
            LocalDate date = LocalDate.now();
            var entity = CommunicationEntity.of("Notification");
            entity.add("_id", 100L);
            entity.add("name", "Social Media");
            entity.add("nickname", "otaviojava");
            entity.add("createdOn",date);
            entity.add("dtype", SocialMediaNotification.class.getSimpleName());
            SocialMediaNotification notification = converter.toEntity(entity);
            assertThat(notification.getId()).isEqualTo(100L);
            assertThat(notification.getName()).isEqualTo("Social Media");
            assertThat(notification.getNickname()).isEqualTo("otaviojava");
            assertThat(notification.getCreatedOn()).isEqualTo(date);
        }

        @Test
        @DisplayName("Should convert document entity to SMS notification")
        void shouldConvertDocumentEntityToSms(){
            LocalDate date = LocalDate.now();
            var entity = CommunicationEntity.of("Notification");
            entity.add("_id", 100L);
            entity.add("name", "SMS Notification");
            entity.add("phone", "+351987654123");
            entity.add("createdOn", date);
            entity.add("dtype", "SMS");
            SmsNotification notification = converter.toEntity(entity);
            assertThat(notification.getId()).isEqualTo(100L);
            assertThat(notification.getName()).isEqualTo("SMS Notification");
            assertThat(notification.getPhone()).isEqualTo("+351987654123");
            assertThat(notification.getCreatedOn()).isEqualTo(date);
        }

        @Test
        @DisplayName("Should convert document entity to email notification")
        void shouldConvertDocumentEntityToEmail(){
            LocalDate date = LocalDate.now();
            var entity = CommunicationEntity.of("Notification");
            entity.add("_id", 100L);
            entity.add("name", "Email Notification");
            entity.add("email", "otavio@otavio.test");
            entity.add("createdOn", date);
            entity.add("dtype", "Email");
            EmailNotification notification = converter.toEntity(entity);
            assertThat(notification.getId()).isEqualTo(100L);
            assertThat(notification.getName()).isEqualTo("Email Notification");
            assertThat(notification.getEmail()).isEqualTo("otavio@otavio.test");
            assertThat(notification.getCreatedOn()).isEqualTo(date);
        }

        @Test
        @DisplayName("Should convert social media notification to communication entity")
        void shouldConvertSocialMediaToCommunicationEntity(){
            SocialMediaNotification notification = new SocialMediaNotification();
            notification.setId(100L);
            notification.setName("Social Media");
            notification.setCreatedOn(LocalDate.now());
            notification.setNickname("otaviojava");
            var entity = converter.toCommunication(notification);
            assertThat(entity).isNotNull();
            assertThat(entity.name()).isEqualTo("Notification");
            assertThat(entity.find("_id", Long.class).get()).isEqualTo(notification.getId());
            assertThat(entity.find("name", String.class).get()).isEqualTo(notification.getName());
            assertThat(entity.find("nickname", String.class).get()).isEqualTo(notification.getNickname());
            assertThat(entity.find("createdOn", LocalDate.class).get()).isEqualTo(notification.getCreatedOn());
        }

        @Test
        @DisplayName("Should convert SMS notification to communication entity")
        void shouldConvertSmsToCommunicationEntity(){
            SmsNotification notification = new SmsNotification();
            notification.setId(100L);
            notification.setName("SMS");
            notification.setCreatedOn(LocalDate.now());
            notification.setPhone("+351123456987");
            var entity = converter.toCommunication(notification);
            assertThat(entity).isNotNull();
            assertThat(entity.name()).isEqualTo("Notification");
            assertThat(entity.find("_id", Long.class).get()).isEqualTo(notification.getId());
            assertThat(entity.find("name", String.class).get()).isEqualTo(notification.getName());
            assertThat(entity.find("phone", String.class).get()).isEqualTo(notification.getPhone());
            assertThat(entity.find("createdOn", LocalDate.class).get()).isEqualTo(notification.getCreatedOn());
        }

        @Test
        @DisplayName("Should convert email notification to communication entity")
        void shouldConvertEmailToCommunicationEntity(){
            EmailNotification notification = new EmailNotification();
            notification.setId(100L);
            notification.setName("Email Media");
            notification.setCreatedOn(LocalDate.now());
            notification.setEmail("otavio@otavio.test.com");
            var entity = converter.toCommunication(notification);
            assertThat(entity).isNotNull();
            assertThat(entity.name()).isEqualTo("Notification");
            assertThat(entity.find("_id", Long.class).get()).isEqualTo(notification.getId());
            assertThat(entity.find("name", String.class).get()).isEqualTo(notification.getName());
            assertThat(entity.find("email", String.class).get()).isEqualTo(notification.getEmail());
            assertThat(entity.find("createdOn", LocalDate.class).get()).isEqualTo(notification.getCreatedOn());
        }

        @Test
        @DisplayName("Should return an error when document discriminator is missing")
        void shouldReturnErrorWhenConvertMissingDocument(){
            LocalDate date = LocalDate.now();
            var entity = CommunicationEntity.of("Notification");
            entity.add("_id", 100L);
            entity.add("name", "SMS Notification");
            entity.add("phone", "+351987654123");
            entity.add("createdOn", date);
            assertThatExceptionOfType(MappingException.class).isThrownBy(() -> converter.toEntity(entity));
        }

        @Test
        @DisplayName("Should return an error when discriminator does not match")
        void shouldReturnErrorWhenMismatchField() {
            LocalDate date = LocalDate.now();
            var entity = CommunicationEntity.of("Notification");
            entity.add("_id", 100L);
            entity.add("name", "Email Notification");
            entity.add("email", "otavio@otavio.test");
            entity.add("createdOn", date);
            entity.add("dtype", "Wrong");
            assertThatExceptionOfType(MappingException.class).isThrownBy(() -> converter.toEntity(entity));
        }



        @Test
        @DisplayName("Should convert notification reader with email notification")
        void shouldConvertCommunicationNotificationReaderEmail() {
            var entity = CommunicationEntity.of("NotificationReader");
            entity.add("_id", "poli");
            entity.add("name", "Poliana Santana");
            entity.add("notification", Arrays.asList(
                    Element.of("_id", 10L),
                    Element.of("name", "News"),
                    Element.of("email", "otavio@email.com"),
                    Element.of("_id", LocalDate.now()),
                    Element.of("dtype", "Email")
            ));

            NotificationReader notificationReader = converter.toEntity(entity);
            assertThat(notificationReader).isNotNull();
            assertThat(notificationReader.getNickname()).isEqualTo("poli");
            assertThat(notificationReader.getName()).isEqualTo("Poliana Santana");
            Notification notification = notificationReader.getNotification();
            assertThat(notification).isNotNull();
            assertThat(notification.getClass()).isEqualTo(EmailNotification.class);
            EmailNotification email = (EmailNotification) notification;
            assertThat(email.getId()).isEqualTo(10L);
            assertThat(email.getName()).isEqualTo("News");
            assertThat(email.getEmail()).isEqualTo("otavio@email.com");
        }

        @Test
        @DisplayName("Should convert notification reader with SMS notification")
        void shouldConvertCommunicationNotificationReaderSms() {
            var entity = CommunicationEntity.of("NotificationReader");
            entity.add("_id", "poli");
            entity.add("name", "Poliana Santana");
            entity.add("notification", Arrays.asList(
                    Element.of("_id", 10L),
                    Element.of("name", "News"),
                    Element.of("phone", "123456789"),
                    Element.of("_id", LocalDate.now()),
                    Element.of("dtype", "SMS")
            ));

            NotificationReader notificationReader = converter.toEntity(entity);
            assertThat(notificationReader).isNotNull();
            assertThat(notificationReader.getNickname()).isEqualTo("poli");
            assertThat(notificationReader.getName()).isEqualTo("Poliana Santana");
            Notification notification = notificationReader.getNotification();
            assertThat(notification).isNotNull();
            assertThat(notification.getClass()).isEqualTo(SmsNotification.class);
            SmsNotification sms = (SmsNotification) notification;
            assertThat(sms.getId()).isEqualTo(10L);
            assertThat(sms.getName()).isEqualTo("News");
            assertThat(sms.getPhone()).isEqualTo("123456789");
        }

        @Test
        @DisplayName("Should convert notification reader with social notification")
        void shouldConvertCommunicationNotificationReaderSocial() {
            var entity = CommunicationEntity.of("NotificationReader");
            entity.add("_id", "poli");
            entity.add("name", "Poliana Santana");
            entity.add("notification", Arrays.asList(
                    Element.of("_id", 10L),
                    Element.of("name", "News"),
                    Element.of("nickname", "123456789"),
                    Element.of("_id", LocalDate.now()),
                    Element.of("dtype", "SocialMediaNotification")
            ));

            NotificationReader notificationReader = converter.toEntity(entity);
            assertThat(notificationReader).isNotNull();
            assertThat(notificationReader.getNickname()).isEqualTo("poli");
            assertThat(notificationReader.getName()).isEqualTo("Poliana Santana");
            Notification notification = notificationReader.getNotification();
            assertThat(notification).isNotNull();
            assertThat(notification.getClass()).isEqualTo(SocialMediaNotification.class);
            SocialMediaNotification social = (SocialMediaNotification) notification;
            assertThat(social.getId()).isEqualTo(10L);
            assertThat(social.getName()).isEqualTo("News");
            assertThat(social.getNickname()).isEqualTo("123456789");
        }

        @Test
        @DisplayName("Should convert social notification reader to communication entity")
        void shouldConvertSocialNotificationReaderToCommunication() {
            SocialMediaNotification notification = new SocialMediaNotification();
            notification.setId(10L);
            notification.setName("Ada");
            notification.setNickname("ada.lovelace");
            NotificationReader reader = new NotificationReader("otavio", "Otavio", notification);

            var entity = converter.toCommunication(reader);
            assertThat(entity).isNotNull();

            assertThat(entity.name()).isEqualTo("NotificationReader");
            assertThat(entity.find("_id", String.class).get()).isEqualTo("otavio");
            assertThat(entity.find("name", String.class).get()).isEqualTo("Otavio");
            List<Element> documents = entity.find("notification", new TypeReference<List<Element>>() {
            }).get();

            assertThat(documents).contains(Element.of("_id", 10L),
                    Element.of("name", "Ada"),
                    Element.of("dtype", "SocialMediaNotification"),
                    Element.of("nickname", "ada.lovelace"));
        }

        @Test
        @DisplayName("Should convert project manager to communication entity")
        void shouldConvertProjectManagerToCommunication() {
            LargeProject large = new LargeProject();
            large.setBudget(BigDecimal.TEN);
            large.setName("large");

            SmallProject small = new SmallProject();
            small.setInvestor("new investor");
            small.setName("Start up");

            List<Project> projects = new ArrayList<>();
            projects.add(large);
            projects.add(small);

            ProjectManager manager = ProjectManager.of(10L, "manager", projects);
            var entity = converter.toCommunication(manager);
            assertThat(entity).isNotNull();

            assertThat(entity.name()).isEqualTo("ProjectManager");
            assertThat(entity.find("_id", Long.class).get()).isEqualTo(10L);
            assertThat(entity.find("name", String.class).get()).isEqualTo("manager");

            List<List<Element>> documents = (List<List<Element>>) entity.find("projects").get().get();

            List<Element> largeCommunication = documents.get(0);
            List<Element> smallCommunication = documents.get(1);
            assertThat(largeCommunication).contains(
                    Element.of("_id", "large"),
                    Element.of("size", "Large"),
                    Element.of("budget", BigDecimal.TEN)
            );

            assertThat(smallCommunication).contains(
                    Element.of("size", "Small"),
                    Element.of("investor", "new investor"),
                    Element.of("_id", "Start up")
            );

        }

        @Test
        @DisplayName("Should convert communication entity to project manager")
        void shouldConvertCommunicationToProjectManager() {
            var communication = CommunicationEntity.of("ProjectManager");
            communication.add("_id", 10L);
            communication.add("name", "manager");
            List<List<Element>> documents = new ArrayList<>();
            documents.add(Arrays.asList(
                    Element.of("_id","small-project"),
                    Element.of("size","Small"),
                    Element.of("investor","investor")
            ));
            documents.add(Arrays.asList(
                    Element.of("_id","large-project"),
                    Element.of("size","Large"),
                    Element.of("budget",BigDecimal.TEN)
            ));
            communication.add("projects", documents);

            ProjectManager manager = converter.toEntity(communication);
            assertThat(manager).isNotNull();

            assertThat(manager.getId()).isEqualTo(10L);
            assertThat(manager.getName()).isEqualTo("manager");

            List<Project> projects = manager.getProjects();
            assertThat(projects.size()).isEqualTo(2);
            SmallProject small = (SmallProject) projects.get(0);
            LargeProject large = (LargeProject) projects.get(1);
            assertThat(small).isNotNull();
            assertThat(small.getName()).isEqualTo("small-project");
            assertThat(small.getInvestor()).isEqualTo("investor");

            assertThat(large).isNotNull();
            assertThat(large.getName()).isEqualTo("large-project");
            assertThat(large.getBudget()).isEqualTo(BigDecimal.TEN);

        }
    }
}
