/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.exception

import kotlin.Throws
import java.util.regex.Pattern

class ExceptionMatcher<T : Throwable>(val exception: T) {

    @Throws(ExpectExceptionError::class)
    fun withMessage(expected: String): ExceptionMatcher<T> {
        if (expected == exception.message) return this
        throw ExpectExceptionError(expected, exception.message)
    }

    @Throws(ExpectExceptionError::class)
    fun withMessage(expected: Pattern): ExceptionMatcher<T> {
        val message = exception.message ?: throw ExpectExceptionError(expected.pattern(), "")
        if (expected.matcher(message).matches()) return this
        throw ExpectExceptionError(expected.pattern(), message)
    }

    @Throws(ExpectExceptionError::class)
    fun withoutMessage(): ExceptionMatcher<T> {
        if (exception.message == null || exception.message == "") return this
        throw ExpectExceptionError("", exception.message)
    }

    @Throws(ExpectExceptionError::class)
    inline fun <reified R : Throwable> withCause(): ExceptionMatcher<T> {
        val actual = exception.cause ?: throw ExpectExceptionError(R::class.simpleName, "")
        if (R::class.simpleName == actual.javaClass.simpleName) return this
        throw ExpectExceptionError(R::class.simpleName, actual.javaClass.simpleName)
    }

    @Throws(ExpectExceptionError::class)
    fun <R: Throwable> withCause(expected: Class<R>): ExceptionMatcher<T> = withCause<Throwable>()

    @Throws(ExpectExceptionError::class)
    fun withoutCause(): ExceptionMatcher<T> {
        val cause = exception.cause ?: return this
        throw ExpectExceptionError("", cause.javaClass.simpleName)
    }
}