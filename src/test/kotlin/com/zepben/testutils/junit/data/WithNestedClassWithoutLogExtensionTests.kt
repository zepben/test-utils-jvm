/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.junit.data

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// Used via the class loader in `TestClassValidator`.
@Suppress("unused")
internal class WithNestedClassWithoutLogExtensionTests {

    //
    // NOTE: This is an invalid test class with only tests in the inner class. This is used to ensure
    //       invalid configurations of inner classes are logged.
    //

    @Nested
    inner class Inner {

        @Test
        internal fun `mark as test class`() {
        }

    }

}
