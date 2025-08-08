/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test

class RandomTest {

    @Test
    fun coverage() {
        assertThat(Random.ofEnum(TestEnum::class.java), any(TestEnum::class.java))
        assertThat(
            Random.ofEnum(TestEnum::class.java),
            anyOf(equalTo(TestEnum.A), equalTo(TestEnum.B), equalTo(TestEnum.C))
        )

        assertThat(Random.ofEnum<TestEnum>(), any(TestEnum::class.java))
    }

    private enum class TestEnum {
        A,
        B,
        C
    }

}
