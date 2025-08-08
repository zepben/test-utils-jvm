/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.mockito

import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class DefaultAnswerTest {

    @Test
    fun changesDefaultAnswers() {
        mock<TestObject>().also {
            validateMock(it, equalTo(0), equalTo(0))
        }

        // Kotlin `of` helper does not support primitive ints.
        mock<TestObject>(DefaultAnswer.of<Int>(100)).also {
            validateMock(it, equalTo(0), equalTo(100))
        }

        mock<TestObject>(DefaultAnswer.of<Int>(1).and<MutableList<Int>>(mutableListOf(100, 200))).also {
            validateMock(it, equalTo(0), equalTo(1), 100, 200)
        }

        // Replaces the default answer if it already exists.
        mock<TestObject>(DefaultAnswer.of<Int>(100).and<Int>(200)).also {
            validateMock(it, equalTo(0), equalTo(200))
        }
    }

    @Test
    fun `Java interop`() {
        // Should only provide defaults for primitive ints.
        mock<TestObject>(DefaultAnswer.of(Int::class.javaPrimitiveType!!, 100)).also {
            validateMock(it, equalTo(100), equalTo(0))
        }

        // Should only provide defaults for boxed ints.
        mock<TestObject>(DefaultAnswer.of(Int::class.javaObjectType, 100)).also {
            validateMock(it, equalTo(0), equalTo(100))
        }

        // Should only provide defaults for both primitive and boxed ints.
        mock(
            TestObject::class.java,
            DefaultAnswer.of(Int::class.javaPrimitiveType!!, 100).and(Int::class.javaObjectType, 200)
        ).also {
            validateMock(it, equalTo(100), equalTo(200))
        }
    }

    @Test
    internal fun `allows nullable default answers`() {
        // This test is in place because the original Kotlin implementation threw a NullPointerException when the
        // underlying default answers returned null.

        // Create a default answer replacing something we are not using so our calls use the underlying default answer.
        mock<TestObject>(DefaultAnswer.of<Double>(0.0)).apply {
            assertThat(nullableFunc(), nullValue())
        }
    }


    private fun validateMock(
        testObject: TestObject,
        intMatcher: Matcher<Any>,
        integerMatcher: Matcher<Any>,
        vararg listValues: Int
    ) {
        assertThat(testObject.intFunc1(), intMatcher)
        assertThat(testObject.intFunc2(), intMatcher)

        assertThat(testObject.integerFunction1(), integerMatcher)
        assertThat(testObject.integerFunction2(), integerMatcher)

        if (listValues.isNotEmpty()) {
            assertThat(testObject.integerListFunc1(), contains(*listValues.toTypedArray()))
            assertThat(testObject.integerListFunc2(), contains(*listValues.toTypedArray()))
        } else {
            assertThat(testObject.integerListFunc1(), empty())
            assertThat(testObject.integerListFunc2(), empty())
        }
    }

    private inline fun <reified T> mock() = mock(T::class.java)
    private inline fun <reified T> mock(defaultAnswer: DefaultAnswer) = mock(T::class.java, defaultAnswer)

}
