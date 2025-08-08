/*
 * Copyright 2023 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.mockito;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

//
// NOTE: This class has been left as Java to allow the mix of primitive `int` and object `Integer` to ensure we can
//       differentiate them in the default answers.
//

interface TestObject {

    int intFunc1();

    int intFunc2();

    @Nonnull
    Integer integerFunction1();

    @Nonnull
    Integer integerFunction2();

    @Nonnull
    List<Integer> integerListFunc1();

    @Nonnull
    List<Integer> integerListFunc2();

    @Nullable
    TestObject nullableFunc();

}
