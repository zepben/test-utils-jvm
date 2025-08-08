/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.hamcrest

import org.hamcrest.Description
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class InputStreamMatcher private constructor(private val expected: String) : TypeSafeMatcher<InputStream>() {

    override fun describeTo(description: Description) {
        description.appendText("InputStream containing ").appendValue(expected)
    }

    public override fun describeMismatchSafely(item: InputStream, mismatchDescription: Description) {
        mismatchDescription.appendText(" was ").appendValue(item.asString())
    }

    public override fun matchesSafely(item: InputStream): Boolean =
        expected == item.asString()

    private fun InputStream.asString(): String =
        try {
            val size = available()
            val bytes = ByteArray(size)

            assertThat(read(bytes, 0, size), equalTo(size))

            String(bytes, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    companion object {

        @JvmStatic
        fun matchesContent(expected: String): InputStreamMatcher {
            return InputStreamMatcher(expected)
        }

    }

}
