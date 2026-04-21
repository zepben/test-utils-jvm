/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils

import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RandomTest {

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    @Test
    fun coverage() {
        assertThat(Random.ofEnum(TestEnum::class.java), any(TestEnum::class.java))
        assertThat(
            Random.ofEnum(TestEnum::class.java),
            anyOf(equalTo(TestEnum.A), equalTo(TestEnum.B), equalTo(TestEnum.C)),
        )

        assertThat(Random.ofEnum<TestEnum>(), any(TestEnum::class.java))
    }

    private enum class TestEnum {
        A,
        B,
        C
    }

}
