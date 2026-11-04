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
import io.opentelemetry.sdk.metrics.data.DoublePointData
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData
import io.opentelemetry.sdk.metrics.data.GaugeData
import io.opentelemetry.sdk.metrics.data.HistogramData
import io.opentelemetry.sdk.metrics.data.LongPointData
import io.opentelemetry.sdk.metrics.data.MetricDataType
import io.opentelemetry.sdk.metrics.data.SumData
import io.opentelemetry.sdk.metrics.data.SummaryData
import io.opentelemetry.sdk.testing.time.TestClock
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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

inline fun <reified D: Data<*>> MetricsCapture.withMetricData(
    name: String,
    instrumentationScope: String? = null,
    block: (D) -> Unit
) {
    withMetric(name, instrumentationScope) { metric ->
        val expectedType = metricDataTypeOf(typeOf<D>())
        val data = metric.data
        if (metric.type != expectedType || data !is D)
            throw IllegalArgumentException("The metric `$name` has data type ${metric.type}. Expected `$expectedType`.")

        block(data)
    }
}

/**
 * Get the [MetricDataType] represented by [type].
 *
 * @throws IllegalArgumentException if [type] is not one of the known [Data] types with a concrete point type.
 */
@PublishedApi
internal fun metricDataTypeOf(type: KType): MetricDataType {
    val pointType = type.arguments.firstOrNull()?.type?.classifier

    return when (type.classifier) {
        SumData::class if pointType == LongPointData::class -> MetricDataType.LONG_SUM
        SumData::class if pointType == DoublePointData::class -> MetricDataType.DOUBLE_SUM
        GaugeData::class if pointType == LongPointData::class -> MetricDataType.LONG_GAUGE
        GaugeData::class if pointType == DoublePointData::class -> MetricDataType.DOUBLE_GAUGE
        HistogramData::class -> MetricDataType.HISTOGRAM
        ExponentialHistogramData::class -> MetricDataType.EXPONENTIAL_HISTOGRAM
        SummaryData::class -> MetricDataType.SUMMARY
        else -> throw IllegalArgumentException("Unsupported metric data type `${type.simpleName()}`.")
    }
}

/**
 * Format [this] type using simple class names, e.g. `SumData<LongPointData>` or `SumData<*>`.
 *
 * We build the name ourselves rather than using [KType.toString], as without kotlin-reflect on the classpath that
 * appends " (Kotlin reflection is not available)" to the name, and it uses fully qualified names.
 */
private fun KType.simpleName(): String {
    val name = (classifier as? KClass<*>)?.simpleName ?: classifier.toString()
    return if (arguments.isEmpty())
        name
    else
        arguments.joinToString(prefix = "$name<", postfix = ">") { (it.type?.classifier as? KClass<*>)?.simpleName ?: "*" }
}
