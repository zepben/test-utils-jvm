/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.junit;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemLogExtensionTest {

    @RegisterExtension
    SystemLogExtension systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess();

    @RegisterExtension
    SystemLogExtension systemErr = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess();

    @Test
    @Order(1)
    void capturesLogsAndMutes() {
        assertThat(systemOut, not(equalTo(systemErr)));

        System.out.println("out line 1");
        System.out.println("out line 2");
        System.out.println("out line 3");

        systemOut.stopCapturingLog().unmute();

        System.out.println("out line 4");
        System.out.println("out line 5");
        System.out.println("out line 6");

        System.err.println("err line 1");
        System.err.println("err line 2");

        assertThat(systemOut.getLogLines().length, equalTo(3));
        assertThat(systemErr.getLogLines().length, equalTo(2));

        assertThat(systemOut.clearCapturedLog().getLogLines().length, equalTo(0));
        assertThat(systemErr.clearCapturedLog().getLogLines().length, equalTo(0));

        System.err.println("err line 1");
        System.err.println("err line 2");
        System.err.println("err line 3");

        assertThat(systemErr.getLogLines().length, equalTo(3));
    }

    @Test
    @Order(2)
    void clearsBetweenTests() {
        assertThat(systemErr.getLogLines().length, equalTo(0));

        System.err.println("err line 1");
        System.err.println("err line 2");
        System.err.println("err line 3");

        assertThat(systemErr.getLogLines().length, equalTo(3));
    }

}
