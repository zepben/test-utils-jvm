/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.sdk.metrics.data.Data
import io.opentelemetry.sdk.metrics.data.DoublePointData
import io.opentelemetry.sdk.metrics.data.GaugeData
import io.opentelemetry.sdk.metrics.data.HistogramData
import io.opentelemetry.sdk.metrics.data.LongPointData
import io.opentelemetry.sdk.metrics.data.SumData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MetricsCaptureTest {

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    @Test
    fun `withMetric filters same-name metrics by instrumentation scope`() = withCapturedMetrics {
        GlobalOpenTelemetry.getMeter("first.scope").counterBuilder("requests").build().add(3)
        GlobalOpenTelemetry.getMeter("second.scope").counterBuilder("requests").build().add(5)

        withMetric("requests", "first.scope") {
            assertThat(it.longSumData.points.map { point -> point.value }, containsInAnyOrder(3))
        }
        withMetric("requests", "second.scope") {
            assertThat(it.longSumData.points.map { point -> point.value }, containsInAnyOrder(5))
        }
    }

    @Test
    fun `withMetricData extracts data`() = withCapturedMetrics {
        val requests = GlobalOpenTelemetry
            .getMeter("scope")
            .counterBuilder("requests")
            .build()

        requests.add(3)

        withMetricData<SumData<LongPointData>>("requests", "scope") {
            assertThat(it.points.map { point -> point.value }, containsInAnyOrder(3))
        }
    }

    @Test
    fun `withMetricData extracts double gauge data`() = withCapturedMetrics {
        GlobalOpenTelemetry
            .getMeter("scope")
            .gaugeBuilder("temperature")
            .build()
            .set(21.5)

        withMetricData<GaugeData<DoublePointData>>("temperature", "scope") {
            assertThat(it.points.map { point -> point.value }, containsInAnyOrder(21.5))
        }
    }

    @Test
    fun `withMetric on missing metric is error`() = withCapturedMetrics {
        expect {
            withMetric("my_metric") { }
        }.toThrow<NoSuchElementException>()
    }

    @Test
    fun `withMetricData on wrong data type`() = withCapturedMetrics {
        val requests = GlobalOpenTelemetry
            .getMeter("scope")
            .counterBuilder("requests")
            .build()

        requests.add(3)

        expect {
            withMetricData<HistogramData>("requests") { }
        }.toThrow<IllegalArgumentException>()
            .withMessage("The metric `requests` has data type LONG_SUM. Expected `HISTOGRAM`.")
    }

    @Test
    fun `withMetricData on wrong point type`() = withCapturedMetrics {
        val requests = GlobalOpenTelemetry
            .getMeter("scope")
            .counterBuilder("requests")
            .build()

        requests.add(3)

        expect {
            withMetricData<SumData<DoublePointData>>("requests") { }
        }.toThrow<IllegalArgumentException>()
            .withMessage("The metric `requests` has data type LONG_SUM. Expected `DOUBLE_SUM`.")
    }

    @Test
    fun `withMetricData on star projected data type is error`() = withCapturedMetrics {
        val requests = GlobalOpenTelemetry
            .getMeter("scope")
            .counterBuilder("requests")
            .build()

        requests.add(3)

        expect {
            withMetricData<SumData<*>>("requests") { }
        }.toThrow<IllegalArgumentException>()
            .withMessage("Unsupported metric data type `SumData<*>`.")
    }

    @Test
    fun `withMetricData on unknown data type is error`() = withCapturedMetrics {
        val requests = GlobalOpenTelemetry
            .getMeter("scope")
            .counterBuilder("requests")
            .build()

        requests.add(3)

        expect {
            withMetricData<Data<*>>("requests") { }
        }.toThrow<IllegalArgumentException>()
            .withMessage("Unsupported metric data type `Data<*>`.")
    }

    @Test
    fun `withMetric throws ambiguousMetricException when the exist multiple matching metrics`() = withCapturedMetrics {
        val metricA = GlobalOpenTelemetry.getMeter("scope.one").counterBuilder("metric").build()
        val metricB = GlobalOpenTelemetry.getMeter("scope.two").counterBuilder("metric").build()

        metricA.add(12)
        metricB.add(45)

        expect {
            // not clarifying the scope here, so just 'metric' is ambiguous between the two metrics
            withMetric("metric") { }
        }.toThrow<AmbiguousMetricException>()
    }
}
