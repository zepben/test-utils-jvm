/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.junit;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.*;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import static java.lang.System.lineSeparator;

/**
 * JUnit5 Extension implementation of https://github.com/stefanbirkner/system-rules log handlers
 * <p>
 * See tests for example usage.
 */
@EverythingIsNonnullByDefault
@SuppressWarnings("UnusedReturnValue")
public class SystemLogExtension implements BeforeEachCallback, AfterEachCallback {

    public static SystemLogExtension SYSTEM_OUT = new SystemLogExtension(System.out, System::setOut);
    public static SystemLogExtension SYSTEM_ERR = new SystemLogExtension(System.err, System::setErr);

    private final PrintStream originalStream;
    private final Consumer<PrintStream> streamReplacer;
    private final LogWrapper logWrapper;

    public SystemLogExtension clearCapturedLog() {
        logWrapper.captureLog.reset();
        return this;
    }

    public SystemLogExtension captureLog() {
        logWrapper.captureLogEnabled = true;
        return this;
    }

    public SystemLogExtension stopCapturingLog() {
        logWrapper.captureLogEnabled = false;
        return this;
    }

    public SystemLogExtension mute() {
        logWrapper.originalStreamEnabled = false;
        logWrapper.failureLogEnabled = false;
        return this;
    }

    public SystemLogExtension unmute() {
        logWrapper.originalStreamEnabled = true;
        logWrapper.failureLogEnabled = false;
        return this;
    }

    public SystemLogExtension muteOnSuccess() {
        mute();
        logWrapper.failureLogEnabled = true;
        return this;
    }

    public String getLog() {
        try {
            return logWrapper.captureLog.toString(Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getLogLines() {
        String log = getLog();
        if (log.equals(""))
            return new String[0];
        else
            return log.split(lineSeparator(), 0);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        streamReplacer.accept(new PrintStream(logWrapper, true, Charset.defaultCharset().name()));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        streamReplacer.accept(originalStream);

        if (extensionContext.getExecutionException().isPresent())
            logWrapper.failureLog.writeTo(originalStream);

        logWrapper.failureLog.reset();
        logWrapper.captureLog.reset();
    }

    private SystemLogExtension(PrintStream originalStream, Consumer<PrintStream> streamReplacer) {
        this.originalStream = originalStream;
        this.streamReplacer = streamReplacer;
        this.logWrapper = new LogWrapper(originalStream);
    }

    private static class LogWrapper extends OutputStream {

        final OutputStream originalStream;
        final ByteArrayOutputStream failureLog = new ByteArrayOutputStream();
        final ByteArrayOutputStream captureLog = new ByteArrayOutputStream();
        boolean originalStreamEnabled = true;
        boolean failureLogEnabled = false;
        boolean captureLogEnabled = false;

        LogWrapper(OutputStream originalStream) {
            this.originalStream = originalStream;
        }

        @Override
        public void write(int b) throws IOException {
            if (originalStreamEnabled)
                originalStream.write(b);
            if (failureLogEnabled)
                failureLog.write(b);
            if (captureLogEnabled)
                captureLog.write(b);
        }

        @Override
        public void flush() throws IOException {
            originalStream.flush();
            //ByteArrayOutputStreams don't have to be closed
        }

        @Override
        public void close() throws IOException {
            originalStream.close();
            //ByteArrayOutputStreams don't have to be closed
        }

    }

}
