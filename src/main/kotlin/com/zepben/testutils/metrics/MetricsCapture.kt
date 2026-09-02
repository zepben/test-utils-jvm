/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.metrics.data.Data
import io.opentelemetry.sdk.testing.time.TestClock

interface MetricsCapture {

    val clock: TestClock
    val metricsReader: InMemoryMetricReader

    companion object {
        /**
         * Create an [InMemoryMetricReader]. This clears any existing metrics reader, creates a new reader, and sets it
         * as the global metric provider.
         */
        fun createReader(clock: Clock): InMemoryMetricReader {
            GlobalOpenTelemetry.resetForTest()

            val metricReader = InMemoryMetricReader.create()
            val meterProvider = SdkMeterProvider.builder().setClock(clock).registerMetricReader(metricReader).build()
            val openTelemetry = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build()

            GlobalOpenTelemetry.set(openTelemetry)

            return metricReader
        }
    }
}

/**
 * Run the [block] with [MetricData] from the metric with name [name]. If multiple such metrics (in different
 * instrumentation scopes), the first option will be returned.
 *
 * @param instrumentationScope an optional instrumentation scope to filter metrics by.
 */
inline fun <T> MetricsCapture.withMetric(
    name: String,
    instrumentationScope: String? = null,
    block: (MetricData) -> T,
): T {
    val allMetrics = metricsReader.collectAllMetrics()
    val metric = allMetrics
        .filter { metric -> instrumentationScope?.let { metric.instrumentationScopeInfo.name == it } ?: true }
        .find { metric -> metric.name == name }
        ?: throw NoSuchElementException("No metric such metric `$name` recorded. Recorded metrics: ${allMetrics.joinToString { it.name }}")

    return block(metric)
}

inline fun <reified D: Data<*>> MetricsCapture.withMetricData(
    name: String,
    instrumentationScope: String? = null,
    block: (D) -> Unit
) {
    withMetric(name, instrumentationScope) { metric ->
        val data = when (metric.data) {
            is D -> metric.data as D
            else -> throw IllegalArgumentException("The metric `$name` has data type ${metric.data.javaClass.simpleName}. Expected `${D::class.simpleName}`.")
        }

        return block(data)
    }
}
