/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics.extensions

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.testutils.metrics.withCapturedMetrics
import com.zepben.testutils.metrics.withMetricData
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.metrics.data.LongPointData
import io.opentelemetry.sdk.metrics.data.SumData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AttributesTest {

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    @Test
    fun `exposes metric data attributes`() = withCapturedMetrics {
        val users = GlobalOpenTelemetry
            .getMeter("com.zepben.testutils.metrics.extensions")
            .counterBuilder("users")
            .build()

        users.add(1, Attributes.of(AttributeKey.stringKey("password"), "12345678"))

        withMetricData<SumData<LongPointData>>("users") {
            assertThat(it.attributes, equalTo(listOf(Attributes.of(AttributeKey.stringKey("password"), "12345678"))))
        }
    }

}
