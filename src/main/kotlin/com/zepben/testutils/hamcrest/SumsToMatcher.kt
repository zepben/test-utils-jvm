/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.hamcrest

import com.zepben.testutils.metrics.datapoints
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData
import io.opentelemetry.sdk.metrics.data.HistogramPointData
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.data.MetricDataType
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import kotlin.math.abs

class SumsToMatcher<T: Any> internal constructor (val total: T): TypeSafeMatcher<MetricData>() {

    override fun matchesSafely(data: MetricData?): Boolean {
        if (data == null) return false

        return when (total) {
            is Long if data.type == MetricDataType.LONG_GAUGE -> data.datapoints<Long>().sum() == total
            is Long if data.type == MetricDataType.LONG_SUM -> data.datapoints<Long>().sum() == total
            is Double if data.type == MetricDataType.DOUBLE_GAUGE -> data.datapoints<Double>().sum() closeTo total
            is Double if data.type == MetricDataType.DOUBLE_SUM -> data.datapoints<Double>().sum() closeTo total
            is Double if data.type == MetricDataType.HISTOGRAM -> data.datapoints<HistogramPointData>().sumOf { it.sum } closeTo total
            is Double if data.type == MetricDataType.EXPONENTIAL_HISTOGRAM -> data.datapoints<ExponentialHistogramPointData>().sumOf { it.sum } closeTo total
            else -> throw IllegalArgumentException("Matching on sum to `${total::class.simpleName}` of metric with datatype ${data.type} is not supported")
        }
    }

    override fun describeTo(description: Description?) {
        description?.appendText("data points summing to $total")
    }

}

private infix fun Double.closeTo(other: Any): Boolean = abs(this - (other as Double)) < 0.00001

/**
 * Match if the [MetricData] datapoints sums to [total].
 *
 * > **Note:** There is always a single data point per attribute permutation.
 * > - For Gauges, this is the last set datapoint
 * > - For Counters, this is the sum of all previous datapoints
 */
fun sumsTo(total: Long) = SumsToMatcher(total)

/**
 * Match if the [MetricData] datapoints sums to [total].
 *
 * > **Note:** There is always a single data point per attribute permutation.
 * > - For Gauges, this is the last set datapoint
 * > - For Counters, this is the sum of all previous datapoints
 */
fun sumsTo(total: Double) = SumsToMatcher(total)
