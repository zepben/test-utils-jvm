/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.junit

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.util.function.Consumer

/**
 * JUnit5 Extension implementation of [https://github.com/stefanbirkner/system-rules](https://github.com/stefanbirkner/system-rules) log handlers
 *
 *
 * See tests for example usage.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SystemLogExtension private constructor(
    private val originalStream: PrintStream,
    private val streamReplacer: Consumer<PrintStream>
) : BeforeEachCallback, AfterEachCallback {

    private val logWrapper: LogWrapper = LogWrapper(originalStream)

    fun clearCapturedLog(): SystemLogExtension {
        logWrapper.captureLog.reset()
        return this
    }

    fun captureLog(): SystemLogExtension {
        logWrapper.captureLogEnabled = true
        return this
    }

    fun stopCapturingLog(): SystemLogExtension {
        logWrapper.captureLogEnabled = false
        return this
    }

    fun mute(): SystemLogExtension {
        logWrapper.originalStreamEnabled = false
        logWrapper.failureLogEnabled = false
        return this
    }

    fun unmute(): SystemLogExtension {
        logWrapper.originalStreamEnabled = true
        logWrapper.failureLogEnabled = false
        return this
    }

    fun muteOnSuccess(): SystemLogExtension {
        mute()
        logWrapper.failureLogEnabled = true
        return this
    }

    val log: String
        get() = logWrapper.captureLog.toString(Charset.defaultCharset())

    val logLines: Array<String>
        get() =
            log.split(System.lineSeparator()).dropLastWhile { it.isBlank() }.toTypedArray()

    @Throws(Exception::class)
    override fun beforeEach(extensionContext: ExtensionContext) {
        streamReplacer.accept(PrintStream(logWrapper, true, Charset.defaultCharset()))
    }

    @Throws(Exception::class)
    override fun afterEach(extensionContext: ExtensionContext) {
        streamReplacer.accept(originalStream)
        if (extensionContext.executionException.isPresent) logWrapper.failureLog.writeTo(originalStream)
        logWrapper.failureLog.reset()
        logWrapper.captureLog.reset()
    }

    private class LogWrapper(
        val originalStream: OutputStream
    ) : OutputStream() {

        val failureLog = ByteArrayOutputStream()
        val captureLog = ByteArrayOutputStream()

        var originalStreamEnabled = true
        var failureLogEnabled = false
        var captureLogEnabled = false

        @Throws(IOException::class)
        override fun write(b: Int) {
            if (originalStreamEnabled) originalStream.write(b)
            if (failureLogEnabled) failureLog.write(b)
            if (captureLogEnabled) captureLog.write(b)
        }

        @Throws(IOException::class)
        override fun flush() {
            originalStream.flush()
            //ByteArrayOutputStreams don't have to be closed
        }

        @Throws(IOException::class)
        override fun close() {
            originalStream.close()
            //ByteArrayOutputStreams don't have to be closed
        }

    }

    companion object {

        @JvmField
        var SYSTEM_OUT = SystemLogExtension(System.out) { out: PrintStream? -> System.setOut(out) }

        @JvmField
        var SYSTEM_ERR = SystemLogExtension(System.err) { err: PrintStream? -> System.setErr(err) }

    }

}
