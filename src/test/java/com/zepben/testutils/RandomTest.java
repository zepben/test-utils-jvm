/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RandomTest {

    @Test
    public void coverage() {
        assertThat(Random.ofEnum(TestEnum.class), any(TestEnum.class));
        assertThat(Random.ofEnum(TestEnum.class), anyOf(equalTo(TestEnum.A), equalTo(TestEnum.B), equalTo(TestEnum.C)));
    }

    private enum TestEnum {
        A,
        B,
        C
    }

}
