/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils

import java.security.SecureRandom

object Random {

    private val random = SecureRandom()

    @JvmStatic
    fun <T : Enum<*>> ofEnum(clazz: Class<T>): T {
        val x = random.nextInt(clazz.enumConstants.size)
        return clazz.enumConstants[x]
    }

    inline fun <reified T : Enum<*>> ofEnum(): T = ofEnum(T::class.java)

}
