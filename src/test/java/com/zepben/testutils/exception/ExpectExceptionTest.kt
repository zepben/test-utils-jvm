/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.exception

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.exception.ExpectExceptionError.Companion.formatForJunit
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.sameInstance
import org.junit.jupiter.api.Test
import java.io.IOException
import java.security.InvalidKeyException
import java.util.regex.Pattern

class ExpectExceptionTest {

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
        val expectException = expect { this.funcThatThrows() }

        expectException.toThrow<IOException>()
        expectException.toThrow<Exception>()
        expectException.toThrow<Throwable>()
    }

    @Test
    fun givesFluentAccessForCheckingException() {
        expect { this.funcThatThrows() }
            .toThrowAny()
            .withMessage("my test")

        expect { funcThatThrowsRuntime() }
            .toThrow<RuntimeException>()
            .withMessage("my runtime test")
            .withCause<Exception>()

        expect { funcThatThrowsRuntime() }
            .toThrow<RuntimeException>()
            .withMessage("my runtime test")
            .withAnyCause()

        expect { funcThatThrowsRuntime() }
            .toThrow<RuntimeException>()
            .withCause<Exception>()
            .withMessage("my runtime test")

        expect { funcThatThrowsRuntime() }
            .toThrow<RuntimeException>()
            .withAnyCause()
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
        assertThat(expect { throw exception }.toThrowAny().exception, sameInstance(exception))
    }

    @Test
    fun doesntMatchMissingExceptionsAll() {
        expect { expect { funcThatDoesntThrow() }.toThrowAny() }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("Throwable", ""))
    }

    @Test
    fun doesntMatchMissingExceptionsExplicit() {
        expect { expect { funcThatDoesntThrow() }.toThrow<IOException>() }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("IOException", ""))
    }

    @Test
    fun doesntMatchIncorrectExceptions() {
        expect { expect { this.funcThatThrows() }.toThrow<IllegalArgumentException>() }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("IllegalArgumentException", "IOException"))
    }

    @Test
    fun doesntMatchIncorrectMessages() {
        expect { expect { this.funcThatThrows() }.toThrowAny().withMessage("I am wrong") }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("I am wrong", "my test"))
    }

    @Test
    fun doesntMatchMissingMessages() {
        expect { expect { funcThatThrowsError() }.toThrowAny().withMessage("message") }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("message", "null"))
        expect { expect { funcThatThrowsBlank() }.toThrowAny().withMessage("message") }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("message", ""))
    }

    @Test
    fun doesntMatchAdditionalMessages() {
        expect { expect { this.funcThatThrows() }.toThrowAny().withoutMessage() }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("", "my test"))
    }

    @Test
    fun doesntMatchIncorrectCauses() {
        expect { expect { funcThatThrowsRuntime() }.toThrowAny().withCause<AssertionError>() }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("AssertionError", "Exception"))
    }

    @Test
    fun doesntMatchMissingCauses() {
        expect { expect { this.funcThatThrows() }.toThrowAny().withCause<Exception>() }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("Exception", ""))
        expect { expect { this.funcThatThrows() }.toThrowAny().withAnyCause() }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("Throwable", ""))
    }

    @Test
    fun doesntMatchAdditionalCauses() {
        expect { expect { funcThatThrowsRuntime() }.toThrowAny().withoutCause() }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("", "Exception"))
    }

    @Test
    fun matchesPattern() {
        expect { this.funcThatThrows() }
            .toThrow<IOException>()
            .withMessage(Pattern.compile("[\\w ]+"))
    }

    @Test
    fun doesntMatchPattern() {
        expect { expect { this.funcThatThrows() }.toThrowAny().withMessage(Pattern.compile("[bad]+")) }
            .toThrow<ExpectExceptionError>()
            .withMessage(formatForJunit("[bad]+", "my test"))
    }

    @Test
    fun `Java interop`() {
        expect { funcThatThrows() }.toThrow(IOException::class.java)
        expect { funcThatThrowsRuntime() }.toThrow(RuntimeException::class.java).withCause(Exception::class.java)
    }

    private fun funcThatDoesntThrow() {}
    private fun funcThatThrows(): Unit = throw IOException("my test")
    private fun funcThatThrows(message: String): Unit = throw IOException(message)
    private fun funcThatThrowsRuntime(): Unit = throw RuntimeException("my runtime test", Exception("runtime cause"))
    private fun funcThatThrowsError(): Unit = throw AssertionError()
    private fun funcThatThrowsBlank(): Unit = throw IOException("")

}
