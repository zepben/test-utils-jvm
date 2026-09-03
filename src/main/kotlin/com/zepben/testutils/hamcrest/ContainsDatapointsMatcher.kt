/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.hamcrest

import com.zepben.testutils.metrics.datapoints
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.data.MetricDataType
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import kotlin.reflect.KClass

class ContainsDatapointsMatcher<T>(val datapoints: MutableList<T>) : TypeSafeMatcher<MetricData>() {

    override fun matchesSafely(data: MetricData?): Boolean {
        if (data == null) return false
        if (datapoints.isEmpty()) return true

        return when (datapoints.first()) {
            is Long if data.type == MetricDataType.LONG_GAUGE -> data.datapoints<Long>().containsAll(datapoints as MutableList<Long>)
            is Long if data.type == MetricDataType.LONG_SUM -> data.datapoints<Long>().containsAll(datapoints as MutableList<Long>)
            is Double if data.type == MetricDataType.DOUBLE_GAUGE -> data.datapoints<Double>().containsAll(datapoints as MutableList<Double>)
            is Double if data.type == MetricDataType.DOUBLE_SUM -> data.datapoints<Double>().containsAll(datapoints as MutableList<Double>)
            else -> throw IllegalArgumentException("Matching on datapoints of `$` on metric with datatype ${data.type} is not supported")
        }
    }

    override fun describeTo(description: Description?) {
        description?.appendText("data points `${datapoints}`")
    }
}

/**
 * Match if the [MetricData] contains all the [datapoints].
 *
 * > **Note:** There is always a single data point per attribute permutation.
 * > - For Gauges, this is the last set datapoint
 * > - For Counters, this is the sum of all previous datapoints
 */
fun containsDatapoints(vararg datapoints: Long) = ContainsDatapointsMatcher(datapoints.toMutableList())

/**
 * Match if the [MetricData] contains all the [datapoints].
 *
 * > **Note:** There is always a single data point per attribute permutation.
 * > - For Gauges, this is the last set datapoint
 * > - For Counters, this is the sum of all previous datapoints
 */
fun containsDatapoints(vararg datapoints: Double) = ContainsDatapointsMatcher(datapoints.toMutableList())
