/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.exception

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
    inline fun <reified U : Throwable> withCause(): ExceptionMatcher<T> {
        val actual = exception.cause ?: throw ExpectExceptionError(U::class.simpleName, "")
        if (actual is U) return this
        throw ExpectExceptionError(U::class.simpleName, actual.javaClass.simpleName)
    }

    /**
     * Java-interoperable version of withCause()
     */
    @Throws(ExpectExceptionError::class)
    fun <U : Throwable> withCause(clazz: Class<U>): ExceptionMatcher<T> {
        val actual = exception.cause ?: throw ExpectExceptionError(clazz.simpleName, "")
        if (clazz.isInstance(actual)) return this
        throw ExpectExceptionError(clazz.simpleName, actual.javaClass.simpleName)
    }

    @Throws(ExpectExceptionError::class)
    fun withAnyCause(): ExceptionMatcher<T> = withCause<Throwable>()

    @Throws(ExpectExceptionError::class)
    fun withoutCause(): ExceptionMatcher<T> {
        val cause = exception.cause ?: return this
        throw ExpectExceptionError("", cause.javaClass.simpleName)
    }
}
