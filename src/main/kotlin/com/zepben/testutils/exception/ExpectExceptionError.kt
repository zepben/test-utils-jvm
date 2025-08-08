/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.exception

/**
 * An error class used to describe a captured exception that did not match the expected exception.
 *
 * @param expected The class name of the expected exception.
 * @param actual The class name of the actual exception, or a blank string if no exception was captured.
 * @param cause The exception that caused this error, if any.
 */
class ExpectExceptionError @JvmOverloads constructor(
    expected: String?,
    actual: String?,
    cause: Throwable? = null
) : AssertionError(formatForJunit(expected, actual), cause) {

    companion object {

        @JvmStatic
        fun formatForJunit(expected: String?, actual: String?): String {
            return String.format("\nExpected: %s\n     but: was %s", expected, actual)
        }

    }

}
