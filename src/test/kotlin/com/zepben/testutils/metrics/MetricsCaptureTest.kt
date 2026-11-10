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
import com.zepben.testutils.metrics.extensions.values
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
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
            assertThat(it.longSumData.values, containsInAnyOrder(3))
        }
        withMetric("requests", "second.scope") {
            assertThat(it.longSumData.values, containsInAnyOrder(5))
        }
    }

    @Test
    fun `withMetric on missing metric is error`() = withCapturedMetrics {
        expect {
            withMetric("my_metric") { }
        }.toThrow<NoSuchElementException>()
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

    @Test
    fun `supports dependency injection`() = withCapturedMetrics {

        class IngestorMetrics(openTelemetry: OpenTelemetry) {
            val metric = openTelemetry
                .getMeter("com.zepben.ingestor")
                .counterBuilder("failures")
                .build()
        }

        // don't need the explicit this@withCapturedMetrics, but just to clarify
        val ingestorMetrics = IngestorMetrics(this@withCapturedMetrics.openTelemetry)

        // oh, no! a failure
        ingestorMetrics.metric.add(1)

        withMetric("failures") {
            assertThat(it.longSumData.values, containsInAnyOrder(1))
        }

    }
}
