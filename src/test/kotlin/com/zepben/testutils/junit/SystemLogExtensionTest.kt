/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.junit

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class SystemLogExtensionTest {

    companion object {
        @JvmField
        @RegisterExtension
        var systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

        @JvmField
        @RegisterExtension
        var systemErr = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()
    }

    // This is simulating a pattern where initialising a class property inside a test class would itself create log lines
    private val classLevelLogMessage = "Example of a string produced while constructing the test class".also { println(it) }

    @Test
    fun capturesLogsAndMutes() {
        assertThat(systemOut, not(equalTo(systemErr)))

        println("out line 1")
        println("out line 2")
        println("out line 3")

        systemOut.stopCapturingLog().unmute()

        println("out line 4")
        println("out line 5")
        println("out line 6")

        System.err.println("err line 1")
        System.err.println("err line 2")

        assertThat(systemOut.logLines.size, equalTo(4)) // Three out lines, plus the example constructor string
        assertThat(systemErr.logLines.size, equalTo(2))

        assertThat(systemOut.logLines.toList(), contains(classLevelLogMessage, "out line 1", "out line 2", "out line 3"))

        assertThat(systemOut.clearCapturedLog().logLines.size, equalTo(0))
        assertThat(systemErr.clearCapturedLog().logLines.size, equalTo(0))

        System.err.println("err line 1")
        System.err.println("err line 2")
        System.err.println("err line 3")

        assertThat(systemErr.logLines.size, equalTo(3))
    }

    @Test
    fun clearsBetweenTests() {
        assertThat(systemErr.logLines.size, equalTo(0))

        System.err.println("err line 1")
        System.err.println("err line 2")
        System.err.println("err line 3")

        assertThat(systemErr.logLines.size, equalTo(3))
    }

}
