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
internal class WithNestedClassWithLogExtensionTests {

    //
    // NOTE: This is a valid test class with only tests in the inner class. This is used to ensure
    //       valid configurations of inner classes aren't logged.
    //

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    @Nested
    inner class Inner {

        @Test
        internal fun `mark as test class`() {
        }

    }

}
