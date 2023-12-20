/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.hamcrest

import com.zepben.testutils.hamcrest.InputStreamMatcher.Companion.matchesContent
import org.hamcrest.Description
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

class InputStreamMatcherTest {

    private val matcher1 = matchesContent("matches one")
    private val matcher2 = matchesContent("matches two")

    @Test
    fun matchesSafely() {
        assertThat(matcher1.matchesSafely(inputStreamOf("matches one")), equalTo(true))
        assertThat(matcher2.matchesSafely(inputStreamOf("matches two")), equalTo(true))

        assertThat(matcher1.matchesSafely(inputStreamOf("one")), equalTo(false))
        assertThat(matcher2.matchesSafely(inputStreamOf("two")), equalTo(false))

        assertThat(matcher1.matchesSafely(inputStreamOf("mismatches one")), equalTo(false))
        assertThat(matcher2.matchesSafely(inputStreamOf("mismatches two")), equalTo(false))
    }

    @Test
    fun describeTo() {
        val description = mock(Description::class.java, RETURNS_SELF)

        matcher1.describeTo(description)

        verify(description, times(1)).appendText(ArgumentMatchers.any())
        verify(description, times(1)).appendText("InputStream containing ")

        verify(description, times(1)).appendValue(ArgumentMatchers.any())
        verify(description, times(1)).appendValue("matches one")
    }

    @Test
    fun describeMismatchSafely() {
        val description = mock(Description::class.java, RETURNS_SELF)

        matcher1.describeMismatchSafely(inputStreamOf("other stream"), description)

        verify(description, times(1)).appendText(ArgumentMatchers.any())
        verify(description, times(1)).appendText(" was ")

        verify(description, times(1)).appendValue(ArgumentMatchers.any())
        verify(description, times(1)).appendValue("other stream")
    }

    private fun inputStreamOf(value: String): InputStream =
        ByteArrayInputStream(value.toByteArray(StandardCharsets.UTF_8))

}
