/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.exception;

import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class ExceptionMatcher<T extends Throwable> {

    private final T exception;

    public ExceptionMatcher(T exception) {
        this.exception = exception;
    }

    public T exception() {
        return exception;
    }

    public ExceptionMatcher<T> withMessage(String expected) throws ExpectExceptionError {
        if (expected.equals(exception.getMessage()))
            return this;

        throw new ExpectExceptionError(expected, exception.getMessage());
    }

    public ExceptionMatcher<T> withMessage(Pattern expected) throws ExpectExceptionError {
        if (expected.matcher(exception.getMessage()).matches())
            return this;

        throw new ExpectExceptionError(expected.pattern(), exception.getMessage());
    }

    public ExceptionMatcher<T> withoutMessage() throws ExpectExceptionError {
        if ((exception.getMessage() == null) || exception.getMessage().equals(""))
            return this;

        throw new ExpectExceptionError("", exception.getMessage());
    }

    public <R extends Throwable> ExceptionMatcher<T> withCause(Class<R> expected) throws ExpectExceptionError {
        Throwable actual = exception.getCause();
        if (actual == null)
            throw new ExpectExceptionError(expected.getSimpleName(), "");

        if (expected.getSimpleName().equals(actual.getClass().getSimpleName()))
            return this;

        throw new ExpectExceptionError(expected.getSimpleName(), actual.getClass().getSimpleName());
    }

    public ExceptionMatcher<T> withoutCause() throws ExpectExceptionError {
        if (exception.getCause() == null)
            return this;

        throw new ExpectExceptionError("", exception.getCause().getClass().getSimpleName());
    }

}
