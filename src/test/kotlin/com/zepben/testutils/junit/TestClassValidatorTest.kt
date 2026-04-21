/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.junit

import com.zepben.testutils.exception.ExpectException.Companion.expect
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestClassValidatorTest {

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    //
    // NOTE: The expected number of extensions detected in the `data` classes designed for testing. You might expect
    //       4 and 8, but they are only captured for classes with valid `@Test` methods, so we miss the incorrect
    //       annotation, and outer class of the non-static test.
    //
    private val expectedSystemErrInData = 3
    private val expectedSystemOutInData = 6

    @Test
    internal fun `detects all the things we are worried about`() {
        //
        // NOTE: The `WithNonStaticLogExtensionTest` uses `WithNonStaticLogExtensionTest.Inner` because it needed to be
        //       `@Nested` to avoid potential for `lateinit` errors when using the `SystemLogExtension` in a non-static
        //       context.
        //
        expect {
            TestClassValidator.validate(packageName = "com.zepben.testutils.junit.data")
        }.toThrow<AssertionError>()
            .withMessage(
                """
                    Malformed test classes detected:

                    Tests with incorrect `Test` annotations: [WithInvalidTestAnnotationTest],
                    Tests with unregistered `SystemLogExtension`: [WithUnregisteredLogExtensionTest],
                    Tests with missing `SystemLogExtension`: [WithoutLogExtensionTest],
                    Tests with extra `SystemLogExtension`: [WithMultipleLogExtensionTest],
                    Tests with non-static `SystemLogExtension`: [WithNonStaticLogExtensionTest.Inner],
                    Tests with non-final `SystemLogExtension`: [WithNonFinalLogExtensionTest],
                    Tests with incorrectly named `SystemLogExtension` variables: [WithIncorrectlyNamedErrLogExtensionTest.systemErrRule, WithIncorrectlyNamedOutLogExtensionTest.systemOutRule],

                    Should all be using the same `SystemLogExtension` type: {SystemLogExtension.SYSTEM_ERR=${expectedSystemErrInData}, SystemLogExtension.SYSTEM_OUT=${expectedSystemOutInData}}
                """.trimIndent(),
            )
    }

    @Test
    internal fun `only reports multiple log extensions when it isn't allowed`() {
        expect {
            TestClassValidator.validate(packageName = "com.zepben.testutils.junit.data", allowMultiLogExtensions = true)
        }.toThrow<AssertionError>()
            .withMessage(
                """
                    Malformed test classes detected:

                    Tests with incorrect `Test` annotations: [WithInvalidTestAnnotationTest],
                    Tests with unregistered `SystemLogExtension`: [WithUnregisteredLogExtensionTest],
                    Tests with missing `SystemLogExtension`: [WithoutLogExtensionTest],
                    Tests with extra `SystemLogExtension`: [],
                    Tests with non-static `SystemLogExtension`: [WithNonStaticLogExtensionTest.Inner],
                    Tests with non-final `SystemLogExtension`: [WithNonFinalLogExtensionTest],
                    Tests with incorrectly named `SystemLogExtension` variables: [WithIncorrectlyNamedErrLogExtensionTest.systemErrRule, WithIncorrectlyNamedOutLogExtensionTest.systemOutRule],
                """.trimIndent(),
            )
    }

    @Test
    internal fun `validate test classes`() {
        //
        // NOTE: When validating all our JUnit test classes we still expect our deliberately corrupted tests to fail,
        //       plus the `SystemLogExtensionTest` which uses both extensions to validate they are capturing properly.
        //
        //       There should be an extra use of `SystemLogExtension.SYSTEM_ERR` in `SystemLogExtensionTest`, plus
        //       another use of `SystemLogExtension.SYSTEM_OUT` in `SystemLogExtensionTest` and this class.
        //
        TestClassValidator.validate("com.zepben", excludingPackages = setOf("com.zepben.testutils.junit"))

        expect {
            TestClassValidator.validate("com.zepben.testutils.junit")
        }.toThrow<AssertionError>()
            .withMessage(
                """
                    Malformed test classes detected:

                    Tests with incorrect `Test` annotations: [WithInvalidTestAnnotationTest],
                    Tests with unregistered `SystemLogExtension`: [WithUnregisteredLogExtensionTest],
                    Tests with missing `SystemLogExtension`: [WithoutLogExtensionTest],
                    Tests with extra `SystemLogExtension`: [WithMultipleLogExtensionTest, SystemLogExtensionTest],
                    Tests with non-static `SystemLogExtension`: [WithNonStaticLogExtensionTest.Inner],
                    Tests with non-final `SystemLogExtension`: [WithNonFinalLogExtensionTest],
                    Tests with incorrectly named `SystemLogExtension` variables: [WithIncorrectlyNamedErrLogExtensionTest.systemErrRule, WithIncorrectlyNamedOutLogExtensionTest.systemOutRule],

                    Should all be using the same `SystemLogExtension` type: {SystemLogExtension.SYSTEM_ERR=${expectedSystemErrInData + 1}, SystemLogExtension.SYSTEM_OUT=${expectedSystemOutInData + 2}}
                """.trimIndent(),
            )
    }

}
