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
import java.util.regex.Pattern

@Suppress("RegExpRepeatedSpace")
class TestClassValidatorTest {

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    //
    // NOTE: The expected number of extensions detected in the `data` classes designed for testing. You might expect
    //       2 and 8, but we can only get the field values from static members, so the non-static one is skipped.
    //
    private val expectedSystemErrInData = 2
    private val expectedSystemOutInData = 7

    @Test
    internal fun `detects all the things we are worried about`() {
        //
        // NOTE: The `WithNonStaticLogExtensionTest` uses `WithNonStaticAndMultipleLogExtensionTest` because it needed to be
        //       `@Nested` to avoid potential for `lateinit` errors when using the `SystemLogExtension` in a non-static
        //       context.
        //
        expect {
            TestClassValidator.validate(packageName = "com.zepben.testutils.junit.data")
        }.toThrow<AssertionError>()
            .withMessage(
                //
                // NOTE: We use regex to allow the order to change on different systems depending on how they load
                //       the classes. If the load order changes, so do the order of the list/map elements. This was
                //       first noticed as a difference between a Windows system and CI (Linux).
                //
                Pattern.compile(
                    """
                        Malformed test classes detected:

                        Tests with incorrect `Test` annotations: \[WithInvalidTestAnnotationTest],
                        Tests with unregistered `SystemLogExtension`: \[WithUnregisteredLogExtensionTest],
                        Tests with missing `SystemLogExtension`: \[${
                        inAnyOrder(
                            "WithoutLogExtensionTest",
                            "WithNestedClassWithoutLogExtensionTests",
                        )
                    }],
                        Tests with extra `SystemLogExtension`: \[WithNonStaticAndMultipleLogExtensionTest],
                        Tests with non-static `SystemLogExtension`: \[WithNonStaticAndMultipleLogExtensionTest],
                        Tests with non-final `SystemLogExtension`: \[WithNonFinalLogExtensionTest],
                        Tests with incorrectly named `SystemLogExtension` variables: \[${
                        inAnyOrder(
                            "WithIncorrectlyNamedErrLogExtensionTest.systemErrRule",
                            "WithIncorrectlyNamedOutLogExtensionTest.systemOutRule",
                        )
                    }],

                        Should all be using the same `SystemLogExtension` type: \{${
                        inAnyOrder(
                            "SystemLogExtension.SYSTEM_ERR=${expectedSystemErrInData}",
                            "SystemLogExtension.SYSTEM_OUT=${expectedSystemOutInData}",
                        )
                    }}
                    """.trimIndent(),
                ),
            )
    }

    @Test
    internal fun `only reports multiple log extensions when it isn't allowed`() {
        expect {
            TestClassValidator.validate(packageName = "com.zepben.testutils.junit.data", allowMultiLogExtensions = true)
        }.toThrow<AssertionError>()
            .withMessage(
                Pattern.compile(
                    """
                        Malformed test classes detected:

                        Tests with incorrect `Test` annotations: \[WithInvalidTestAnnotationTest],
                        Tests with unregistered `SystemLogExtension`: \[WithUnregisteredLogExtensionTest],
                        Tests with missing `SystemLogExtension`: \[${
                        inAnyOrder(
                            "WithoutLogExtensionTest",
                            "WithNestedClassWithoutLogExtensionTests",
                        )
                    }],
                        Tests with extra `SystemLogExtension`: \[],
                        Tests with non-static `SystemLogExtension`: \[WithNonStaticAndMultipleLogExtensionTest],
                        Tests with non-final `SystemLogExtension`: \[WithNonFinalLogExtensionTest],
                        Tests with incorrectly named `SystemLogExtension` variables: \[${
                        inAnyOrder(
                            "WithIncorrectlyNamedErrLogExtensionTest.systemErrRule",
                            "WithIncorrectlyNamedOutLogExtensionTest.systemOutRule",
                        )
                    }],
                    """.trimIndent(),
                ),
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
                //
                // NOTE: We use regex to allow the order to change on different systems depending on how they load
                //       the classes. If the load order changes, so do the order of the list/map elements. This was
                //       first noticed as a difference between a Windows system and CI (Linux).
                //
                Pattern.compile(
                    """
                        Malformed test classes detected:

                        Tests with incorrect `Test` annotations: \[WithInvalidTestAnnotationTest],
                        Tests with unregistered `SystemLogExtension`: \[WithUnregisteredLogExtensionTest],
                        Tests with missing `SystemLogExtension`: \[${
                        inAnyOrder(
                            "WithoutLogExtensionTest",
                            "WithNestedClassWithoutLogExtensionTests",
                        )
                    }],
                        Tests with extra `SystemLogExtension`: \[${
                        inAnyOrder(
                            "WithNonStaticAndMultipleLogExtensionTest",
                            "SystemLogExtensionTest",
                        )
                    }],
                        Tests with non-static `SystemLogExtension`: \[WithNonStaticAndMultipleLogExtensionTest],
                        Tests with non-final `SystemLogExtension`: \[WithNonFinalLogExtensionTest],
                        Tests with incorrectly named `SystemLogExtension` variables: \[${
                        inAnyOrder(
                            "WithIncorrectlyNamedErrLogExtensionTest.systemErrRule",
                            "WithIncorrectlyNamedOutLogExtensionTest.systemOutRule",
                        )
                    }],

                        Should all be using the same `SystemLogExtension` type: \{${
                        inAnyOrder(
                            "SystemLogExtension.SYSTEM_ERR=${expectedSystemErrInData + 1}",
                            "SystemLogExtension.SYSTEM_OUT=${expectedSystemOutInData + 2}",
                        )
                    }}
                    """.trimIndent(),
                ),
            )
    }

    private fun inAnyOrder(vararg values: String): String =
        values.toList()
            .permutations()
            .joinToString(separator = "|", prefix = "(", postfix = ")") {
                // Join the inner list with ", "
                it.joinToString()
            }

    fun <T> List<T>.permutations(): List<List<T>> =
        when {
            isEmpty() -> listOf(emptyList())
            else -> flatMap { item -> (this - item).permutations().map { listOf(item) + it } }
        }
}
