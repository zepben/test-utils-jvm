/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.junit

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class SystemLogExtensionTest {

    companion object {
        @JvmField
        @RegisterExtension
        val systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

        @JvmField
        @RegisterExtension
        val systemErr = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

        //
        // NOTE: You can also log a static message here, but it won't work how you expect when running all the tests.
        //       It will work correctly if you run `capturesLogsAndMutes` as a single test, but it can be swallowed
        //       elsewhere when running all tests, resulting in a test failure. You'll just have to trust it is working,
        //       which can be verified by adding the following block, adding it as the first expected message in
        //       `capturesLogsAndMutes` (commented out below), then running that test individually:
        //
        //       // This is simulating a pattern where initialising a static property inside a test class would itself create log lines
        //       private val staticLevelLogMessage =
        //          "Example of a string produced statically before constructing the test class".also { println(it) }
        //
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

        // Swap if using staticLevelLogMessage above:
        // assertThat(systemOut.logLines.size, equalTo(5)) // Three out lines, plus the example static and constructor strings
        assertThat(systemOut.logLines.size, equalTo(4)) // Three out lines, plus the example constructor string
        assertThat(systemErr.logLines.size, equalTo(2))

        assertThat(
            systemOut.logLines.toList(),
            // Swap if using staticLevelLogMessage above:
            // contains(staticLevelLogMessage, classLevelLogMessage, "out line 1", "out line 2", "out line 3"),
            contains(classLevelLogMessage, "out line 1", "out line 2", "out line 3"),
        )

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
