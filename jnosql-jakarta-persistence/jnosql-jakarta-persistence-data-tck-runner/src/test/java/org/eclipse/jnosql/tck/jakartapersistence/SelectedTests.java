/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.tck.jakartapersistence;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.EntityMetadataExtension;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.document.DocumentTemplateProducer;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ee.jakarta.tck.data.standalone.entity.EntityTests;

// selected failing tests that can be worked on next
@Tag("development")
public class SelectedTests {

    @Nested
    @DisplayName("Selected EntityTests tests")
    @EnableAutoWeld
    @AddPackages(value = {Converters.class, EntityConverter.class, DocumentTemplate.class})
    @AddPackages(DocumentTemplateProducer.class)
    @AddPackages(Reflections.class)
    @AddExtensions({EntityMetadataExtension.class, JakartaPersistenceExtension.class})
    @AddPackages({PersistenceDocumentTemplate.class, PersistenceDatabaseManager.class})
    @AddPackages({JNoSqlEntitySelectedTests.class, EntityTests.class})
    @ExtendWith(TransactionExtension.class)
    class JNoSqlEntitySelectedTests extends EntityTests {

        @Override
//    assertion failed
//    @Test
        public void testVarargsSort() {
            super.testVarargsSort();
        }

        @Override
//    assertion failed
//    @Test
        public void testUpdateQueryWithWhereClause() {
            super.testUpdateQueryWithWhereClause();
        }

        @Override
//    assertion failed
//    @Test
        public void testUpdateQueryWithoutWhereClause() {
            super.testUpdateQueryWithoutWhereClause();
        }

        @Override
//    assertion failed
//    @Test
        public void testTrue() {
            super.testTrue();
        }

        @Override
//    assertion failed
//    @Test
        public void testThirdAndFourthSlicesOf5() {
            super.testThirdAndFourthSlicesOf5();
        }

        @Override
//    assertion failed
//    @Test
        public void testThirdAndFourthPagesOf10() {
            super.testThirdAndFourthPagesOf10();
        }

        @Override
        @Test
        public void testStreamsFromList() {
            super.testStreamsFromList();
        }

        @Override
//    EndsWith
//    @Test
        public void testStaticMetamodelDescendingSortsPreGenerated() {
            super.testStaticMetamodelDescendingSortsPreGenerated();
        }

        @Override
        @Test
        public void testStaticMetamodelDescendingSorts() {
            super.testStaticMetamodelDescendingSorts();
        }

        @Override
        @Test
        public void testStaticMetamodelAttributeNamesPreGenerated() {
            super.testStaticMetamodelAttributeNamesPreGenerated();
        }

        @Override
        @Test
        public void testStaticMetamodelAttributeNames() {
            super.testStaticMetamodelAttributeNames();
        }

        @Override
//    EndsWith not supported
//    @Test
        public void testStaticMetamodelAscendingSortsPreGenerated() {
            super.testStaticMetamodelAscendingSortsPreGenerated();
        }

        @Override
//    assertion failed
//    @Test
        public void testStaticMetamodelAscendingSorts() {
            super.testStaticMetamodelAscendingSorts();
        }

        @Override
        @Test
        public void testSliceOfNothing() {
            super.testSliceOfNothing();
        }

        @Override
//    IgnoreCase
//    @Test
        public void testSingleEntity() {
            super.testSingleEntity();
        }

        @Override
//    Query not supported
//    @Test
        public void testQueryWithParenthesis() {
            super.testQueryWithParenthesis();
        }

        @Override
//    syntax error
//    @Test
        public void testQueryWithOr() {
            super.testQueryWithOr();
        }

        @Override
//    query not supported
//    @Test
        public void testQueryWithNull() {
            super.testQueryWithNull();
        }

        @Override
//    Query not supported
//    @Test
        public void testQueryWithNot() {
            super.testQueryWithNot();
        }

        @Override
        @Test
        // NullPointerException - Not supported by JNoSQL yet?
        public void testPrimaryEntityClassDeterminedByLifeCycleMethods() {
            super.testPrimaryEntityClassDeterminedByLifeCycleMethods();
        }

        @Override
//    Query not supported
//    @Test
        public void testPartialQuerySelectAndOrderBy() {
            super.testPartialQuerySelectAndOrderBy();
        }

        @Override
        @Test
        public void testPartialQueryOrderBy() {
            super.testPartialQueryOrderBy();
        }

        @Override
//    Not supported by JNoSQL yet
//    @Test
        public void testPageOfNothing() {
            super.testPageOfNothing();
        }

        @Override
//    assertion failed
//    @Test
        public void testOrderByHasPrecedenceOverSorts() {
            super.testOrderByHasPrecedenceOverSorts();
        }

        @Override
//    assertion failed
//    @Test
        public void testOrderByHasPrecedenceOverPageRequestSorts() {
            super.testOrderByHasPrecedenceOverPageRequestSorts();
        }

        @Override
        @Test
        public void testOr() {
            super.testOr();
        }

        @Override
//    ClassCastException
//    @Test
        public void testNot() {
            super.testNot();
        }

        @Override
//    non unique result
//    @Test
        public void testNonUniqueResultException() {
            super.testNonUniqueResultException();
        }

        @Override
//    ClassCastException
//    @Test
        public void testMixedSort() {
            super.testMixedSort();
        }

        @Override
//    Query not supported
//    @Test
        public void testLiteralTrue() {
            super.testLiteralTrue();
        }

        @Override
//    Query not supported
//    @Test
        public void testLiteralString() {
            super.testLiteralString();
        }

        @Override
//    Query not supported
//    @Test
        public void testLiteralInteger() {
            super.testLiteralInteger();
        }

        @Override
//    Query not supported
//    @Test
        public void testLiteralEnumAndLiteralFalse() {
            super.testLiteralEnumAndLiteralFalse();
        }

        @Override
//    assertion failed
//    @Test
        public void testLimitToOneResult() {
            super.testLimitToOneResult();
        }

        @Override
//    assertion failed
//    @Test
        public void testLimitedRange() {
            super.testLimitedRange();
        }

        @Override
//    assertion failed
//    @Test
        public void testLimit() {
            super.testLimit();
        }

        @Override
        @Test
        public void testLessThanWithCount() {
            super.testLessThanWithCount();
        }

        @Override
        @Test
        public void testCursoredPageWithoutTotalOfNothing() {
            super.testCursoredPageWithoutTotalOfNothing();
        }

        @Override
        @Test
        public void testCursoredPageWithoutTotalOf9FromCursor() {
            super.testCursoredPageWithoutTotalOf9FromCursor();
        }

        @Override
        @Test
        public void testCursoredPageOfNothing() {
            super.testCursoredPageOfNothing();
        }

        @Override
        @Test
        public void testCursoredPageOf7FromCursor() {
            super.testCursoredPageOf7FromCursor();
        }

        @Override
//    IgnoreCase not supported
//    @Test
        public void testIgnoreCase() {
            super.testIgnoreCase();
        }

        @Override
//    assertion failed
//    @Test
        public void testIn() {
            super.testIn();
        }

        @Override
        @Test
        public void testGreaterThanEqualExists() {
            super.testGreaterThanEqualExists();
        }

        @Override
//    assertion failed
//    @Test
        public void testFirstSliceOf5() {
            super.testFirstSliceOf5();
        }

        @Override
//    assertion failed
//    @Test
        public void testFirstPageOf10() {
            super.testFirstPageOf10();
        }

        @Override
        @Test
        public void testFirstCursoredPageWithoutTotalOf6AndNextPages() {
            super.testFirstCursoredPageWithoutTotalOf6AndNextPages();
        }

        @Override
        @Test
        public void testFirstCursoredPageOf8AndNextPages() {
            super.testFirstCursoredPageOf8AndNextPages();
        }

        @Override
//    assertion failed
//    @Test
        public void testFindPage() {
            super.testFindPage();
        }

        @Override
        @Test
        public void testFindOptional() {
            super.testFindOptional();
        }

        @Override
        @Test
        public void testFindOne() {
            super.testFindOne();
        }

        @Override
//    assertion failed
//    @Test
        public void testFindList() {
            super.testFindList();
        }

        @Override
//    EndsWith not supported
//    @Test
        public void testFindFirst3() {
            super.testFindFirst3();
        }

        @Override
//    Syntax
//    @Test
        public void testFindFirst() {
            super.testFindFirst();
        }

        @Override
//    assertion failed
//    @Test
        public void testFindAllWithPagination() {
            super.testFindAllWithPagination();
        }

        @Override
//    assertion failed
//    @Test
        public void testFinalSliceOfUpTo5() {
            super.testFinalSliceOfUpTo5();
        }

        @Override
//    assertion failed
//    @Test
        public void testFinalPageOfUpTo10() {
            super.testFinalPageOfUpTo10();
        }

        @Override
        @Test
        public void testFalse() {
            super.testFalse();
        }

        @Override
//    IgnoreCase not supported
//    @Test
        public void testEmptyResultException() {
            super.testEmptyResultException();
        }

        @Override
//    Query not supported
//    @Test
        public void testEmptyQuery() {
            super.testEmptyQuery();
        }

        @Override
//    assertion failed
//    @Test
        public void testDescendingSort() {
            super.testDescendingSort();
        }

        @Override
//    assertion failed
//    @Test
        public void testDefaultMethod() {
            super.testDefaultMethod();
        }

        @Override
//    IgnoreCase
//    @Test
        public void testDataRepository() {
            super.testDataRepository();
        }

        @Override
//    Contains not supported by JNoSQL
//    @Test
        public void testContainsInString() {
            super.testContainsInString();
        }

        @Override
        @Test
        public void testCommonInterfaceQueries() {
            super.testCommonInterfaceQueries();
        }

        @Override
        @Test
        public void testBy() {
            super.testBy();
        }

        @Override
//    assertion failed
//    @Test
        public void testBeyondFinalSlice() {
            super.testBeyondFinalSlice();
        }

        @Override
//    assertion failed
//    @Test
        public void testBeyondFinalPage() {
            super.testBeyondFinalPage();
        }

        @Override
        @Test
        public void testBasicRepositoryMethods() {
            super.testBasicRepositoryMethods();
        }

        @Override
        @Test
        public void testBasicRepositoryBuiltInMethods() {
            super.testBasicRepositoryBuiltInMethods();
        }

        @Override
//    assertion failed
//    @Test
        public void testBasicRepository() {
            super.testBasicRepository();
        }

    }

    @Nested
    @DisplayName("Selected PersistenceEntity tests")
    @EnableAutoWeld
    @AddPackages(value = {Converters.class, EntityConverter.class, DocumentTemplate.class})
    @AddPackages(DocumentTemplateProducer.class)
    @AddPackages(Reflections.class)
    @AddExtensions({EntityMetadataExtension.class, JakartaPersistenceExtension.class})
    @AddPackages({PersistenceDocumentTemplate.class, PersistenceDatabaseManager.class})
    @AddPackages({JNoSqlPersistenceEntityTests.class, EntityTests.class})
    @ExtendWith(TransactionExtension.class)
    public class JNoSqlPersistenceEntitySelectedTests extends JNoSqlPersistenceEntityTests {

    }

}
