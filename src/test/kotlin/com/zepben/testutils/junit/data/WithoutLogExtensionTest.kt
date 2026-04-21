/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.junit.data

import org.junit.jupiter.api.Test

// Used via the class loader in `TestClassValidator`.
@Suppress("unused")
internal class WithoutLogExtensionTest {

    // NOTE: Deliberately missing extensions for testing purposes.

    @Test
    internal fun `mark as test class`() {
    }

}
