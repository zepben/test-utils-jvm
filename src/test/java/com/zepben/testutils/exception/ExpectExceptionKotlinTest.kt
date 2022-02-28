/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.exception

import com.zepben.testutils.exception.ExpectException.Companion.expect
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.io.IOException
import java.security.InvalidKeyException
import java.util.regex.Pattern

class ExpectExceptionKotlinTest {
    @Test
    fun catchesAny() {
        expect { funcThatThrows() }.toThrowAny()
        expect { funcThatThrowsRuntime() }.toThrowAny()
        expect { funcThatThrowsError() }.toThrowAny()
        expect { funcThatThrows("test with params") }.toThrowAny()
        expect { funcThatThrows("test with different params") }.toThrowAny()
    }

    @Test
    fun matchesExceptionClassAndAncestors() {
        expect { funcThatThrows() }.toThrow<IOException>()
        expect { funcThatThrows() }.toThrow<Exception>()
        expect { funcThatThrows() }.toThrow<Throwable>()
        expect { funcThatThrowsRuntime() }.toThrow<RuntimeException>()
        expect { funcThatThrowsRuntime() }.toThrow<Exception>()
        expect { funcThatThrowsRuntime() }.toThrow<Throwable>()
        expect { funcThatThrowsError() }.toThrow<AssertionError>()
        expect { funcThatThrowsError() }.toThrow<Error>()
        expect { funcThatThrowsError() }.toThrow<Throwable>()
    }

    @Test
    fun canBeReused() {
        val expectException = expect { funcThatThrows() }
        expectException.toThrow<IOException>()
        expectException.toThrow<Exception>()
        expectException.toThrow<Throwable>()
    }

    @Test
    fun givesFluentAccessForCheckingException() {
        expect { funcThatThrows() }
            .toThrowAny()
            .withMessage("my test")
        expect { funcThatThrowsRuntime() }
            .toThrow<RuntimeException>()
            .withMessage("my runtime test")
            .withCause<Exception>()
        expect { funcThatThrowsRuntime() }
            .toThrow<RuntimeException>()
            .withCause<Exception>()
            .withMessage("my runtime test")
        expect { funcThatThrowsError() }
            .toThrowAny()
            .withoutCause()
            .withoutMessage()
        expect { funcThatThrowsError() }
            .toThrowAny()
            .withoutMessage()
            .withoutCause()
    }

    @Test
    fun blankMessagesDontCount() {
        expect { funcThatThrowsBlank() }.toThrowAny()
            .withoutMessage()
    }

    @Test
    fun givesAccessToTheOriginalException() {
        val exception = InvalidKeyException()
        assertThat(expect { throw exception }.toThrowAny().exception, equalTo(exception))
    }

    @Test
    fun doesntMatchMissingExceptionsAll() {
        expect { expect { funcThatDoesntThrow() }.toThrowAny() }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("Throwable", ""))
    }

    @Test
    fun doesntMatchMissingExceptionsExplicit() {
        expect {
            expect { funcThatDoesntThrow() }.toThrow<IOException>()
        }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("IOException", ""))
    }

    @Test
    fun doesntMatchIncorrectExceptions() {
        expect {
            expect { funcThatThrows() }.toThrow<IllegalArgumentException>()
        }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("IllegalArgumentException", "IOException"))
    }

    @Test
    fun doesntMatchIncorrectMessages() {
        expect { expect { funcThatThrows() }.toThrowAny().withMessage("I am wrong") }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("I am wrong", "my test"))
    }

    @Test
    fun doesntMatchMissingMessages() {
        expect { expect { funcThatThrowsError() }.toThrowAny().withMessage("message") }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("message", "null"))
        expect { expect { funcThatThrowsBlank() }.toThrowAny().withMessage("message") }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("message", ""))
    }

    @Test
    fun doesntMatchAdditionalMessages() {
        expect { expect { funcThatThrows() }.toThrowAny().withoutMessage() }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("", "my test"))
    }

    @Test
    fun doesntMatchIncorrectCauses() {
        expect {
            expect { funcThatThrowsRuntime() }.toThrowAny().withCause<AssertionError>()
        }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("AssertionError", "Exception"))
    }

    @Test
    fun doesntMatchMissingCauses() {
        expect {
            expect { funcThatThrows() }.toThrowAny().withCause<Exception>()
        }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("Exception", ""))
    }

    @Test
    fun doesntMatchAdditionalCauses() {
        expect { expect { funcThatThrowsRuntime() }.toThrowAny().withoutCause() }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("", "Exception"))
    }

    @Test
    fun matchesPattern() {
        expect { funcThatThrows() }
            .toThrow<IOException>()
            .withMessage(Pattern.compile("[\\w ]+"))
    }

    @Test
    fun doesntMatchPattern() {
        expect { expect { funcThatThrows() }.toThrowAny().withMessage(Pattern.compile("[bad]+")) }
            .toThrow<ExpectExceptionError>()
            .withMessage(ExpectExceptionError.formatForJunit("[bad]+", "my test"))
    }

    private fun funcThatDoesntThrow() {}

    @Throws(IOException::class)
    private fun funcThatThrows() {
        throw IOException("my test")
    }

    @Throws(IOException::class)
    private fun funcThatThrows(message: String) {
        throw IOException(message)
    }

    private fun funcThatThrowsRuntime() {
        throw RuntimeException("my runtime test", Exception("runtime cause"))
    }

    private fun funcThatThrowsError() {
        throw AssertionError()
    }

    @Throws(IOException::class)
    private fun funcThatThrowsBlank() {
        throw IOException("")
    }
}
