/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics

import io.opentelemetry.sdk.metrics.data.DoublePointData
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData
import io.opentelemetry.sdk.metrics.data.HistogramPointData
import io.opentelemetry.sdk.metrics.data.LongPointData
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.data.MetricDataType

internal inline fun <reified T: Any> MetricData.datapoints(): List<T> {
    return when (this.type) {
        MetricDataType.LONG_GAUGE if T::class == Long::class -> this.data.points.map { (it as LongPointData).value as T }
        MetricDataType.DOUBLE_GAUGE if T::class == Double::class -> this.data.points.map { (it as DoublePointData).value as T }
        MetricDataType.LONG_SUM if T::class == Long::class -> this.data.points.map { (it as LongPointData).value as T }
        MetricDataType.DOUBLE_SUM if T::class == Double::class -> this.data.points.map { (it as DoublePointData).value as T }
        MetricDataType.SUMMARY -> throw IllegalArgumentException("Collecting datapoints of `Summary` metric data is not supported")
        MetricDataType.HISTOGRAM if T::class == HistogramPointData::class -> this.data.points.map { (it as HistogramPointData) as T }
        MetricDataType.EXPONENTIAL_HISTOGRAM if T::class == ExponentialHistogramPointData::class -> this.data.points.map { (it as ExponentialHistogramPointData) as T }
        else -> throw IllegalArgumentException("Metric data has type `$this.type`, but T is `${T::class.simpleName}`")
    }
}
