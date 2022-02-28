/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.exception;

import javax.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class ExpectExceptionError extends AssertionError {

    public ExpectExceptionError(String expected, String actual) {
        this(expected, actual, null);
    }

    public ExpectExceptionError(String expected, String actual, @Nullable Throwable cause) {
        super(formatForJunit(expected, actual), cause);
    }

    static String formatForJunit(String expected, String actual) {
        return String.format("\nExpected: %s\n     but: was %s", expected, actual);
    }

}
