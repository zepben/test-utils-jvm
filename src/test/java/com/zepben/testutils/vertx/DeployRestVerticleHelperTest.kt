/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.vertx

import com.zepben.testutils.exception.ExpectException.Companion.expect
import org.junit.jupiter.api.extension.RegisterExtension
import com.zepben.testutils.junit.SystemLogExtension
import org.junit.jupiter.api.Test
import kotlin.Throws
import java.lang.Void
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import java.lang.Exception

class DeployRestVerticleHelperTest {
    @JvmField
    @RegisterExtension
    val systemErr = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    @Test
    @Throws(Exception::class)
    fun coverageOnlyTest() {
        // TODO: Make this an actual test.
        val helper = buildHelper()
        helper.requestSpec()
        helper.randomPortNumber
        helper.close()
        TestVerticle.setOnStart { future: Future<Void?> -> future.fail("test start fail") }
        expect { buildHelper() }.toThrow<AssertionError>()
    }

    private fun buildHelper(): DeployRestVerticleHelper {
        return DeployRestVerticleHelper(TestVerticle::class.java, JsonObject())
    }
}