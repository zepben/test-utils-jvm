/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics.extensions

import io.opentelemetry.sdk.metrics.data.Data
import io.opentelemetry.sdk.metrics.data.DoublePointData
import io.opentelemetry.sdk.metrics.data.LongPointData

// use JVM names to avoid platform declaration clashes

@get:JvmName("getDoubleAttributes")
val Data<DoublePointData>.attributes get() = points.map { it.attributes }

@get:JvmName("getLongAttributes")
val Data<LongPointData>.attributes get() = points.map { it.attributes }
