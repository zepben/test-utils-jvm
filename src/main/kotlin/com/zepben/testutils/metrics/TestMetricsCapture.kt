/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import io.opentelemetry.sdk.testing.time.TestClock
import java.lang.AutoCloseable

class TestMetricsCapture: MetricsCapture, AutoCloseable {
    override val clock: TestClock = TestClock.create()
    override val metricsReader: InMemoryMetricReader = MetricsCapture.createReader(clock)

    override fun close() {
        metricsReader.close()
    }
}

/**
 * Run the [block] while capturing metrics.
 *
 * Captured metrics are accessible via [com.zepben.testutils.metrics.withMetric].
 *
 * > **NOTE:** Only metrics created inside the [block] will have their metrics captured.
 */
inline fun withCapturedMetrics(block: MetricsCapture.() -> Unit) {
    try {
        TestMetricsCapture().use { it.block() }
    } finally {
        GlobalOpenTelemetry.resetForTest()
    }
}
