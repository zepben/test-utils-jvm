/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.hamcrest.containsDatapoints
import com.zepben.testutils.junit.SystemLogExtension
import io.opentelemetry.api.GlobalOpenTelemetry
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

        withMetric("requests", "first.scope") { assertThat(it, containsDatapoints(3)) }
        withMetric("requests", "second.scope") { assertThat(it, containsDatapoints(5)) }
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
    fun `withMetric on missing metric is error`() = withCapturedMetrics {
        expect {
            withMetric("my_metric") { }
        }.toThrow<NoSuchElementException>()

        Unit
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

        Unit
    }
}
