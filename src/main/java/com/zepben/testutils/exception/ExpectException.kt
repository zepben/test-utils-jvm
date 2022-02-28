/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.exception

class ExpectException private constructor(val func: RunWithException) {
    fun interface RunWithException {
        @Throws(Throwable::class)
        fun run()
    }

    @Throws(ExpectExceptionError::class)
    inline fun <reified T : Throwable> toThrow(): ExceptionMatcher<T> {
        try {
            func.run()
        } catch (e: Throwable) {
            if (e is T) return ExceptionMatcher(e)
            throw ExpectExceptionError(T::class.simpleName, e.javaClass.simpleName, e)
        }
        throw ExpectExceptionError(T::class.simpleName, "")
    }

    /**
     * Java-interoperable version of toThrow()
     */
    @Throws(ExpectExceptionError::class)
    fun <T : Throwable> toThrow(clazz: Class<T>): ExceptionMatcher<T> {
        try {
            func.run()
        } catch (e: Throwable) {
            if (clazz.isInstance(e)) return ExceptionMatcher(clazz.cast(e))
            throw ExpectExceptionError(clazz.simpleName, e.javaClass.simpleName, e)
        }
        throw ExpectExceptionError(clazz.simpleName, "")
    }

    @Throws(ExpectExceptionError::class)
    fun toThrowAny(): ExceptionMatcher<Throwable> = toThrow()

    companion object {
        @JvmStatic
        fun expect(func: RunWithException): ExpectException {
            return ExpectException(func)
        }
    }
}
