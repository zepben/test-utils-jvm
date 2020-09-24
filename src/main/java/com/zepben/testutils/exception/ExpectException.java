/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.exception;

@SuppressWarnings("WeakerAccess")
public class ExpectException {

    @FunctionalInterface
    public interface RunWithException {
        void run() throws Throwable;
    }

    private RunWithException func;

    public static ExpectException expect(RunWithException func) {
        return new ExpectException(func);
    }

    private ExpectException(RunWithException func) {
        this.func = func;
    }

    public ExceptionMatcher<Throwable> toThrow() throws ExpectExceptionError {
        try {
            func.run();
        } catch (Throwable e) {
            return new ExceptionMatcher<>(e);
        }
        throw new ExpectExceptionError("Throwable", "");
    }

    public <T extends Throwable> ExceptionMatcher<T> toThrow(Class<T> clazz) throws ExpectExceptionError {
        try {
            func.run();
        } catch (Throwable e) {
            if (clazz.isInstance(e))
                return new ExceptionMatcher<>(clazz.cast(e));

            throw new ExpectExceptionError(clazz.getSimpleName(), e.getClass().getSimpleName(), e);
        }
        throw new ExpectExceptionError(clazz.getSimpleName(), "");
    }

}
