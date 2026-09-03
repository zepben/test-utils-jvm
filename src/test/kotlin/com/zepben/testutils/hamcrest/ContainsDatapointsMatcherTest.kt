/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.hamcrest

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.testutils.metrics.withCapturedMetrics
import com.zepben.testutils.metrics.withMetric
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.GlobalOpenTelemetry
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ContainsDatapointsMatcherTest {

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    @Test
    fun `long gauge contains`() = withCapturedMetrics {
        // gauge metrics emit the last value they were set to when sampled
        val gauge = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .gaugeBuilder("long_gauge").ofLongs()
            .build()

        gauge.set(5)
        gauge.set(7)
        gauge.set(12)

        withMetric("long_gauge") {
            assertThat(it, not(containsDatapoints(5)))
            assertThat(it, not(containsDatapoints(7)))
            assertThat(it, containsDatapoints(12))
        }
    }

    @Test
    fun `double gauge contains`() = withCapturedMetrics {
        // gauge metrics emit the last value they were set to when sampled
        val gauge = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .gaugeBuilder("double_gauge")
            .build()

        gauge.set(1.1)
        gauge.set(2.2)
        gauge.set(3.3)

        withMetric("double_gauge") {
            assertThat(it, not(containsDatapoints(1.1)))
            assertThat(it, not(containsDatapoints(2.2)))
            assertThat(it, containsDatapoints(3.3))
        }
    }

    @Test
    fun `long counter contains`() = withCapturedMetrics {
        val counter = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .counterBuilder("long_counter")
            .build()

        counter.add(1)
        counter.add(35)
        counter.add(0)

        withMetric("long_counter") { assertThat(it, containsDatapoints(36)) }
    }

    @Test
    fun `double counter sum`() = withCapturedMetrics {
        val counter = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .counterBuilder("double_counter").ofDoubles()
            .build()

        counter.add(1.5)
        counter.add(3.5)
        counter.add(0.1)

        withMetric("double_counter") { assertThat(it, containsDatapoints(5.1)) }
    }

    @Test
    fun `counter contains datapoints for each attribute set`() = withCapturedMetrics {
        val counter = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .counterBuilder("attributed_counter")
            .build()

        counter.add(3, Attributes.of(AttributeKey.stringKey("colour"), "red"))
        counter.add(5, Attributes.of(AttributeKey.stringKey("colour"), "blue"))

        withMetric("attributed_counter") { assertThat(it, containsDatapoints(3, 5)) }
    }

}
