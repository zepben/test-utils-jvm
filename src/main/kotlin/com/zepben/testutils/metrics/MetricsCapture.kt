/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import io.opentelemetry.sdk.testing.time.TestClock

interface MetricsCapture: AutoCloseable {

    val clock: TestClock
    val metricsReader: InMemoryMetricReader

    /**
     * The instance of [OpenTelemetry] that is being captured.
     */
    val openTelemetry: OpenTelemetrySdk

    companion object {
        /**
         * Create a [MetricsCapture]. This clears any existing metrics reader, creates a new reader, and sets it as the
         * global metric provider.
         */
        fun create(clock: TestClock = TestClock.create()): MetricsCapture {
            GlobalOpenTelemetry.resetForTest()

            val metricReader = InMemoryMetricReader.create()
            val meterProvider = SdkMeterProvider.builder().setClock(clock).registerMetricReader(metricReader).build()
            val openTelemetry = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build()

            GlobalOpenTelemetry.set(openTelemetry)

            return object : MetricsCapture {
                override val clock: TestClock = clock
                override val metricsReader: InMemoryMetricReader = metricReader
                override val openTelemetry: OpenTelemetrySdk = openTelemetry
            }
        }
    }

    override fun close() {
        metricsReader.close()
        openTelemetry.close()
    }
}

/**
 * Run the [block] with [MetricData] from the metric with name [name]. If multiple such metrics (in different
 * instrumentation scopes), the first option will be returned.
 *
 * @param instrumentationScope an optional instrumentation scope to filter metrics by.
 *
 * @throws NoSuchElementException if no matching metric is found.
 * @throws AmbiguousMetricException if multiple matching metrics are found.
 */
inline fun MetricsCapture.withMetric(
    name: String,
    instrumentationScope: String? = null,
    block: (MetricData) -> Unit,
) {
    val allMetrics = metricsReader.collectAllMetrics()
    val metrics = allMetrics
        .filter { metric -> instrumentationScope?.let { metric.instrumentationScopeInfo.name == it } ?: true }
        .filter { metric -> metric.name == name }

    if (metrics.isEmpty()) throw NoSuchElementException("No metric `$name` recorded. Recorded metrics: ${allMetrics.joinToString { it.name }}")
    if (metrics.size > 1) throw AmbiguousMetricException(name, metrics.map { it.instrumentationScopeInfo.name })

    block(metrics.single())
}
