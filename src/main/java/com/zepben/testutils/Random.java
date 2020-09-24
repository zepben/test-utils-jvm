/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils;

import com.zepben.annotations.EverythingIsNonnullByDefault;

import java.security.SecureRandom;

@EverythingIsNonnullByDefault
public class Random {

    private static final SecureRandom random = new SecureRandom();

    public static <T extends Enum<?>> T ofEnum(Class<T> clazz) {
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

}
