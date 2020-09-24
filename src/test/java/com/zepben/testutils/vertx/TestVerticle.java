/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.vertx;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.function.Consumer;

;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class TestVerticle extends AbstractVerticle {

    public static void setOnStart(Runnable onStart) {
        setOnStart((future) -> {
            onStart.run();
            future.complete();
        });
    }

    public static void setOnStart(Consumer<Future<Void>> onStart) {
        TestVerticle.onStart = onStart;
    }

    public static void setOnStop(Runnable onStop) {
        setOnStop((future) -> {
            onStop.run();
            future.complete();
        });
    }

    public static void setOnStop(Consumer<Future<Void>> onStop) {
        TestVerticle.onStop = onStop;
    }

    private static Consumer<Future<Void>> onStart = Future::complete;
    private static Consumer<Future<Void>> onStop = Future::complete;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        onStart.accept(startFuture);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        onStop.accept(stopFuture);
    }

}
