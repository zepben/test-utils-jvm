/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics

import io.opentelemetry.api.GlobalOpenTelemetry

/**
 * Run the [block] while capturing metrics. An instance of `OpenTelemetry` is available by `GlobalOpenTelemetry.get()`,
 * or `openTelemetry` for dependency injection.
 *
 * Captured metrics are accessible via [com.zepben.testutils.metrics.withMetric].
 *
 * > **NOTE:** Only metrics created inside the [block] will have their metrics captured.
 */
inline fun withCapturedMetrics(block: MetricsCapture.() -> Unit) {
    try {
        MetricsCapture.create().use { it.block() }
    } finally {
        GlobalOpenTelemetry.resetForTest()
    }
}
