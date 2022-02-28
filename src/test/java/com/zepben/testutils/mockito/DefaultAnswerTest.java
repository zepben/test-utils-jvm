/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.mockito;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class DefaultAnswerTest {

    @Test
    public void changesDefaultAnswers() {
        TestObject testObject = mock(TestObject.class);
        validateMock(testObject, equalTo(0), equalTo(0));

        testObject = mock(TestObject.class, DefaultAnswer.of(int.class, 100));
        validateMock(testObject, equalTo(100), equalTo(0));

        testObject = mock(TestObject.class, DefaultAnswer.of(Integer.class, 100));
        validateMock(testObject, equalTo(0), equalTo(100));

        testObject = mock(TestObject.class,
            DefaultAnswer.of(Integer.class, 100)
                .and(List.class, Arrays.asList(100, 200)));
        validateMock(testObject, equalTo(0), equalTo(100), 100, 200);

        testObject = mock(TestObject.class,
            DefaultAnswer.of(Integer.class, 100)
                .and(Integer.class, 200));
        validateMock(testObject, equalTo(0), equalTo(200));
    }

    private interface TestObject {
        int intFunc1();

        int intFunc2();

        Integer integerFunction1();

        Integer integerFunction2();

        List<Integer> integerListFunc1();

        List<Integer> integerListFunc2();
    }

    private void validateMock(TestObject testObject,
                              Matcher<Object> intMatcher,
                              Matcher<Object> integerMatcher) {
        assertThat(testObject.intFunc1(), intMatcher);
        assertThat(testObject.intFunc2(), intMatcher);
        assertThat(testObject.integerFunction1(), integerMatcher);
        assertThat(testObject.integerFunction2(), integerMatcher);
        assertThat(testObject.integerListFunc1(), empty());
        assertThat(testObject.integerListFunc2(), empty());
    }

    private void validateMock(TestObject testObject,
                              Matcher<Object> intMatcher,
                              Matcher<Object> integerMatcher,
                              Integer... listValues) {
        assertThat(testObject.intFunc1(), intMatcher);
        assertThat(testObject.intFunc2(), intMatcher);
        assertThat(testObject.integerFunction1(), integerMatcher);
        assertThat(testObject.integerFunction2(), integerMatcher);
        assertThat(testObject.integerListFunc1(), contains(listValues));
        assertThat(testObject.integerListFunc2(), contains(listValues));
    }

}
