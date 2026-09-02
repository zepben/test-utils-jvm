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
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData
import io.opentelemetry.sdk.resources.Resource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SumsToMatcherTest {

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    @Test
    fun `long gauge sum`() = withCapturedMetrics {
        // gauge metrics emit the last value they were set to when sampled
        val gauge = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .gaugeBuilder("long_gauge").ofLongs()
            .build()

        gauge.set(5)
        withMetric("long_gauge") { assertThat(it, sumsTo(5)) }
        gauge.set(7)
        withMetric("long_gauge") { assertThat(it, sumsTo(7)) }
        gauge.set(12)
        withMetric("long_gauge") { assertThat(it, sumsTo(12)) }
    }

    @Test
    fun `double gauge sum`() = withCapturedMetrics {
        // gauge metrics emit the last value they were set to when sampled
        val gauge = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .gaugeBuilder("double_gauge")
            .build()

        gauge.set(1.1)
        withMetric("double_gauge") { assertThat(it, sumsTo(1.1)) }
        gauge.set(2.2)
        withMetric("double_gauge") { assertThat(it, sumsTo(2.2)) }
        gauge.set(3.3)
        withMetric("double_gauge") { assertThat(it, sumsTo(3.3)) }
    }

    @Test
    fun `long counter sum`() = withCapturedMetrics {
        val counter = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .counterBuilder("long_counter")
            .build()

        counter.add(1)
        counter.add(35)
        counter.add(0)

        withMetric("long_counter") { assertThat(it, sumsTo(36)) }
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

        withMetric("double_counter") { assertThat(it, sumsTo(5.1)) }
    }

    @Test
    fun `long histogram sum`() = withCapturedMetrics {
        val histogram = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .histogramBuilder("long_histogram").ofLongs()
            .build()

        histogram.record(5)
        histogram.record(2)
        histogram.record(9)

        withMetric("long_histogram") { assertThat(it, sumsTo(16.0)) }
    }

    @Test
    fun `double histogram sum`() = withCapturedMetrics {
        val histogram = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .histogramBuilder("double_histogram")
            .build()

        histogram.record(8.0)
        histogram.record(1.0)
        histogram.record(3.0)

        withMetric("double_histogram") { assertThat(it, sumsTo(12.0)) }
    }

    @Test
    fun `double histogram multi attribute sum`() = withCapturedMetrics {
        val histogram = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .histogramBuilder("histogram")
            .build()

        histogram.record(0.1, Attributes.of(AttributeKey.stringKey("task"), "generator"))
        histogram.record(0.2, Attributes.of(AttributeKey.stringKey("task"), "executor"))

        withMetric("histogram") {
            assertThat(it, sumsTo(0.3))
        }
    }

    @Test
    fun `exponential histogram sum`() {
        val buckets = ExponentialHistogramBuckets.create(0, 0, emptyList())
        val point = ExponentialHistogramPointData.create(
            0,
            7.0,
            0,
            false,
            0.0,
            false,
            0.0,
            buckets,
            buckets,
            0,
            0,
            Attributes.empty(),
            emptyList(),
        )
        val metric = ImmutableMetricData.createExponentialHistogram(
            Resource.empty(),
            InstrumentationScopeInfo.create(javaClass.name),
            "exponential_histogram",
            "",
            "",
            ExponentialHistogramData.create(AggregationTemporality.CUMULATIVE, listOf(point)),
        )

        assertThat(metric, sumsTo(7.0))
    }

    @Test
    fun `long sums preserve precision`() = withCapturedMetrics {
        val value = 9_007_199_254_740_993L

        val gauge = GlobalOpenTelemetry
            .getMeter(javaClass.`package`.name)
            .gaugeBuilder("large_long_gauge").ofLongs()
            .build()

        gauge.set(value)

        withMetric("large_long_gauge") {
            assertThat(it, not(sumsTo(value - 1)))
            assertThat(it, sumsTo(value))
        }
    }
}
