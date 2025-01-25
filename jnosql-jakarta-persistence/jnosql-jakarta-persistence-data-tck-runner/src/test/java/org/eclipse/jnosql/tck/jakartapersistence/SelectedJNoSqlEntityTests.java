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

import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ee.jakarta.tck.data.standalone.entity.EntityTests;

/**
 *
 * @author Ondro Mihalyi
 */
@Tag("development")
@DisplayName(value = "Selected EntityTests tests")
@EnableAutoWeld
@ExtendWith(value = TransactionExtension.class)
class SelectedJNoSqlEntityTests extends EntityTests {

    @Override
    public void testVarargsSort() {
        super.testVarargsSort();
    }

    @Override
    public void testUpdateQueryWithWhereClause() {
        super.testUpdateQueryWithWhereClause();
    }

    @Override
    public void testUpdateQueryWithoutWhereClause() {
        super.testUpdateQueryWithoutWhereClause();
    }

    @Override
    public void testTrue() {
        super.testTrue();
    }

    @Override
    public void testThirdAndFourthSlicesOf5() {
        super.testThirdAndFourthSlicesOf5();
    }

    @Override
    public void testThirdAndFourthPagesOf10() {
        super.testThirdAndFourthPagesOf10();
    }

    @Override
    @Test
    public void testStreamsFromList() {
        super.testStreamsFromList();
    }

    @Override
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
    public void testStaticMetamodelAscendingSortsPreGenerated() {
        super.testStaticMetamodelAscendingSortsPreGenerated();
    }

    @Override
    public void testStaticMetamodelAscendingSorts() {
        super.testStaticMetamodelAscendingSorts();
    }

    @Override
    @Test
    public void testSliceOfNothing() {
        super.testSliceOfNothing();
    }

    @Override
    public void testSingleEntity() {
        super.testSingleEntity();
    }

    @Override
    public void testQueryWithParenthesis() {
        super.testQueryWithParenthesis();
    }

    @Override
    public void testQueryWithOr() {
        super.testQueryWithOr();
    }

    @Override
    public void testQueryWithNull() {
        super.testQueryWithNull();
    }

    @Override
    public void testQueryWithNot() {
        super.testQueryWithNot();
    }

    @Override
    @Test
    public void testPrimaryEntityClassDeterminedByLifeCycleMethods() {
        super.testPrimaryEntityClassDeterminedByLifeCycleMethods();
    }

    @Override
    public void testPartialQuerySelectAndOrderBy() {
        super.testPartialQuerySelectAndOrderBy();
    }

    @Override
    @Test
    public void testPartialQueryOrderBy() {
        super.testPartialQueryOrderBy();
    }

    @Override
    public void testPageOfNothing() {
        super.testPageOfNothing();
    }

    @Override
    public void testOrderByHasPrecedenceOverSorts() {
        super.testOrderByHasPrecedenceOverSorts();
    }

    @Override
    public void testOrderByHasPrecedenceOverPageRequestSorts() {
        super.testOrderByHasPrecedenceOverPageRequestSorts();
    }

    @Override
    @Test
    public void testOr() {
        super.testOr();
    }

    @Override
    public void testNot() {
        super.testNot();
    }

    @Override
    @Test
    public void testNonUniqueResultException() {
        super.testNonUniqueResultException();
    }

    @Override
    public void testMixedSort() {
        super.testMixedSort();
    }

    @Override
    public void testLiteralTrue() {
        super.testLiteralTrue();
    }

    @Override
    public void testLiteralString() {
        super.testLiteralString();
    }

    @Override
    public void testLiteralInteger() {
        super.testLiteralInteger();
    }

    @Override
    public void testLiteralEnumAndLiteralFalse() {
        super.testLiteralEnumAndLiteralFalse();
    }

    @Override
    public void testLimitToOneResult() {
        super.testLimitToOneResult();
    }

    @Override
    public void testLimitedRange() {
        super.testLimitedRange();
    }

    @Override
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
    public void testIgnoreCase() {
        super.testIgnoreCase();
    }

    @Override
    public void testIn() {
        super.testIn();
    }

    @Override
    @Test
    public void testGreaterThanEqualExists() {
        super.testGreaterThanEqualExists();
    }

    @Override
    public void testFirstSliceOf5() {
        super.testFirstSliceOf5();
    }

    @Override
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
    public void testFindList() {
        super.testFindList();
    }

    @Override
    public void testFindFirst3() {
        super.testFindFirst3();
    }

    @Override
    public void testFindFirst() {
        super.testFindFirst();
    }

    @Override
    public void testFindAllWithPagination() {
        super.testFindAllWithPagination();
    }

    @Override
    public void testFinalSliceOfUpTo5() {
        super.testFinalSliceOfUpTo5();
    }

    @Override
    public void testFinalPageOfUpTo10() {
        super.testFinalPageOfUpTo10();
    }

    @Override
    @Test
    public void testFalse() {
        super.testFalse();
    }

    @Override
    public void testEmptyResultException() {
        super.testEmptyResultException();
    }

    @Override
    public void testEmptyQuery() {
        super.testEmptyQuery();
    }

    @Override
    public void testDescendingSort() {
        super.testDescendingSort();
    }

    @Override
    public void testDefaultMethod() {
        super.testDefaultMethod();
    }

    @Override
    public void testDataRepository() {
        super.testDataRepository();
    }

    @Override
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
    public void testBeyondFinalSlice() {
        super.testBeyondFinalSlice();
    }

    @Override
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
    public void testBasicRepository() {
        super.testBasicRepository();
    }

}
