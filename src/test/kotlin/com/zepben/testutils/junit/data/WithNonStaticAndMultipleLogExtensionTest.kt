/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.junit.data

import com.zepben.testutils.junit.SystemLogExtension
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

// Used via the class loader in `TestClassValidator`.
@Suppress("unused")
internal class WithNonStaticAndMultipleLogExtensionTest {

    companion object {
        //
        // NOTE: We create a static copy of the log extension to avoid the risk of this being the first test run and
        //       failing the lateinit property check in the extension. This means it will also count as a duplicate
        //       log extension.
        //
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    /**
     * NOTE: We mark this as an inner/nested test to ensure we have at least one static copy of the log extension. If
     *       we didn't do this, we run the risk of this being the first test run and failing the lateinit property
     *       check in the extension.
     */
    @Nested
    inner class Inner {

        // NOTE: Deliberately non-static for testing purposes.
        @JvmField
        @RegisterExtension
        @Suppress("JUnitMalformedDeclaration")
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

        @Test
        internal fun `mark as test class`() {
        }
    }

}
