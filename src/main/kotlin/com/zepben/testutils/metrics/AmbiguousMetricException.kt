/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.metrics

class AmbiguousMetricException(metric: String, scopes: List<String>): IllegalStateException(
    "Ambiguous metric `$metric`. Consider filtering by metric scope. The metric was present in the following scopes: $scopes"
)
