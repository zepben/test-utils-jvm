/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.junit

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
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
) : BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    val log: String
        get() = logWrapper.captureLog.toString(Charset.defaultCharset())

    val logLines: Array<String>
        get() =
            log.split(System.lineSeparator()).dropLastWhile { it.isBlank() }.toTypedArray()

    private val logWrapper: LogWrapper = LogWrapper(originalStream)
    private lateinit var initialSettings: LogWrapperSettings

    init {
        streamReplacer.accept(PrintStream(logWrapper, true, Charset.defaultCharset()))
    }

    fun clearCapturedLog(): SystemLogExtension {
        logWrapper.captureLog.reset()
        return this
    }

    fun captureLog(): SystemLogExtension {
        logWrapper.settings = logWrapper.settings.copy(captureLogEnabled = true)
        return this
    }

    fun stopCapturingLog(): SystemLogExtension {
        logWrapper.settings = logWrapper.settings.copy(captureLogEnabled = false)
        return this
    }

    fun mute(): SystemLogExtension {
        logWrapper.settings = logWrapper.settings.copy(originalStreamEnabled = false, failureLogEnabled = false)
        return this
    }

    fun unmute(): SystemLogExtension {
        logWrapper.settings = logWrapper.settings.copy(originalStreamEnabled = true, failureLogEnabled = false)
        return this
    }

    fun muteOnSuccess(): SystemLogExtension {
        mute()
        logWrapper.settings = logWrapper.settings.copy(failureLogEnabled = true)
        return this
    }

    override fun beforeAll(extensionContext: ExtensionContext) {
        streamReplacer.accept(PrintStream(logWrapper, true, Charset.defaultCharset()))
        initialSettings = logWrapper.settings
    }

    @Throws(Exception::class)
    override fun beforeEach(extensionContext: ExtensionContext) {
        logWrapper.settings = initialSettings
    }

    @Throws(Exception::class)
    override fun afterEach(extensionContext: ExtensionContext) {
        if (extensionContext.executionException.isPresent) logWrapper.failureLog.writeTo(originalStream)

        logWrapper.settings = initialSettings

        logWrapper.failureLog.reset()
        logWrapper.captureLog.reset()
    }

    override fun afterAll(extensionContext: ExtensionContext) {
        logWrapper.settings = initialSettings
        streamReplacer.accept(originalStream)
    }

    private class LogWrapper(
        val originalStream: OutputStream,
    ) : OutputStream() {

        val failureLog = ByteArrayOutputStream()
        val captureLog = ByteArrayOutputStream()

        var settings = LogWrapperSettings(
            originalStreamEnabled = true,
            captureLogEnabled = false,
            failureLogEnabled = false
        )

        @Throws(IOException::class)
        override fun write(b: Int) {
            if (settings.originalStreamEnabled) originalStream.write(b)
            if (settings.failureLogEnabled) failureLog.write(b)
            if (settings.captureLogEnabled) captureLog.write(b)
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

    private data class LogWrapperSettings(
        val originalStreamEnabled: Boolean,
        val captureLogEnabled: Boolean,
        val failureLogEnabled: Boolean
    )

    companion object {

        @JvmField
        var SYSTEM_OUT = SystemLogExtension(System.out) { out: PrintStream? -> System.setOut(out) }

        @JvmField
        var SYSTEM_ERR = SystemLogExtension(System.err) { err: PrintStream? -> System.setErr(err) }

    }

}
