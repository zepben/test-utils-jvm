/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.exception;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.regex.Pattern;

import static com.zepben.testutils.exception.ExpectException.expect;
import static com.zepben.testutils.exception.ExpectExceptionError.formatForJunit;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ExpectExceptionTest {

    @Test
    public void catchesAny() {
        expect(this::funcThatThrows).toThrow();
        expect(this::funcThatThrowsRuntime).toThrow();
        expect(this::funcThatThrowsError).toThrow();
        expect(() -> funcThatThrows("test with params")).toThrow();
        expect(() -> funcThatThrows("test with different params")).toThrow();
    }

    @Test
    public void matchesExceptionClassAndAncestors() {
        expect(this::funcThatThrows).toThrow(IOException.class);
        expect(this::funcThatThrows).toThrow(Exception.class);
        expect(this::funcThatThrows).toThrow(Throwable.class);
        expect(this::funcThatThrowsRuntime).toThrow(RuntimeException.class);
        expect(this::funcThatThrowsRuntime).toThrow(Exception.class);
        expect(this::funcThatThrowsRuntime).toThrow(Throwable.class);
        expect(this::funcThatThrowsError).toThrow(AssertionError.class);
        expect(this::funcThatThrowsError).toThrow(Error.class);
        expect(this::funcThatThrowsError).toThrow(Throwable.class);
    }

    @Test
    public void canBeReused() {
        ExpectException expectException = expect(this::funcThatThrows);

        expectException.toThrow(IOException.class);
        expectException.toThrow(Exception.class);
        expectException.toThrow(Throwable.class);
    }

    @Test
    public void givesFluentAccessForCheckingException() {
        expect(this::funcThatThrows)
            .toThrow()
            .withMessage("my test");

        expect(this::funcThatThrowsRuntime)
            .toThrow(RuntimeException.class)
            .withMessage("my runtime test")
            .withCause(Exception.class);

        expect(this::funcThatThrowsRuntime)
            .toThrow(RuntimeException.class)
            .withCause(Exception.class)
            .withMessage("my runtime test");

        expect(this::funcThatThrowsError)
            .toThrow()
            .withoutCause()
            .withoutMessage();

        expect(this::funcThatThrowsError)
            .toThrow()
            .withoutMessage()
            .withoutCause();
    }

    @Test
    public void blankMessagesDontCount() {
        expect(this::funcThatThrowsBlank).toThrow()
            .withoutMessage();
    }

    @Test
    public void givesAccessToTheOriginalException() {
        InvalidKeyException exception = new InvalidKeyException();
        assertThat(expect(() -> {
            throw exception;
        }).toThrow().exception(), equalTo(exception));
    }

    @Test
    public void doesntMatchMissingExceptionsAll() {
        expect(() -> expect(this::funcThatDoesntThrow).toThrow())
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("Throwable", ""));
    }

    @Test
    public void doesntMatchMissingExceptionsExplicit() {
        expect(() -> expect(this::funcThatDoesntThrow).toThrow(IOException.class))
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("IOException", ""));
    }

    @Test
    public void doesntMatchIncorrectExceptions() {
        expect(() -> expect(this::funcThatThrows).toThrow(IllegalArgumentException.class))
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("IllegalArgumentException", "IOException"));
    }

    @Test
    public void doesntMatchIncorrectMessages() {
        expect(() -> expect(this::funcThatThrows).toThrow().withMessage("I am wrong"))
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("I am wrong", "my test"));
    }

    @Test
    public void doesntMatchMissingMessages() {
        expect(() -> expect(this::funcThatThrowsError).toThrow().withMessage("message"))
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("message", "null"));
        expect(() -> expect(this::funcThatThrowsBlank).toThrow().withMessage("message"))
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("message", ""));
    }

    @Test
    public void doesntMatchAdditionalMessages() {
        expect(() -> expect(this::funcThatThrows).toThrow().withoutMessage())
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("", "my test"));
    }

    @Test
    public void doesntMatchIncorrectCauses() {
        expect(() -> expect(this::funcThatThrowsRuntime).toThrow().withCause(AssertionError.class))
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("AssertionError", "Exception"));
    }

    @Test
    public void doesntMatchMissingCauses() {
        expect(() -> expect(this::funcThatThrows).toThrow().withCause(Exception.class))
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("Exception", ""));
    }

    @Test
    public void doesntMatchAdditionalCauses() {
        expect(() -> expect(this::funcThatThrowsRuntime).toThrow().withoutCause())
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("", "Exception"));
    }

    @Test
    public void matchesPattern() {
        expect(this::funcThatThrows)
            .toThrow(IOException.class)
            .withMessage(Pattern.compile("[\\w ]+"));
    }

    @Test
    public void doesntMatchPattern() {
        expect(() -> expect(this::funcThatThrows).toThrow().withMessage(Pattern.compile("[bad]+")))
            .toThrow(ExpectExceptionError.class)
            .withMessage(formatForJunit("[bad]+", "my test"));
    }

    private void funcThatDoesntThrow() {
    }

    private void funcThatThrows() throws IOException {
        throw new IOException("my test");
    }

    private void funcThatThrows(String message) throws IOException {
        throw new IOException(message);
    }

    private void funcThatThrowsRuntime() {
        throw new RuntimeException("my runtime test", new Exception("runtime cause"));
    }

    private void funcThatThrowsError() {
        throw new AssertionError();
    }

    private void funcThatThrowsBlank() throws IOException {
        throw new IOException("");
    }

}
