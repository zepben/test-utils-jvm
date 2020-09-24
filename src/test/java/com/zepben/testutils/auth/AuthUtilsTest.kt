/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.auth

import io.grpc.Metadata
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test

class AuthUtilsTest {

    @Test
    fun testMockServerCall() {
        var called = false
        val msc = MockServerCall<Int, Int>({ _, _ -> called = true }, "zepben.protobuf.np.NetworkProducer/testMethod")
        assertThat(msc.methodDescriptor.serviceName, equalTo("zepben.protobuf.np.NetworkProducer"))
        msc.sendHeaders(Metadata())
        msc.sendMessage(3)
        msc.request(1)
        assertThat("coverage", !msc.isCancelled)
        msc.close(null, null)
        assertThat("Method was not called", called)
    }

    @Test
    fun testMockJwksUrlProvider() {
        val mjup = MockJwksUrlProvider()
        assertThat(mjup.all.size, equalTo(1))
        assertThat(mjup.all[0].additionalAttributes["n"], equalTo(attribs["n"]))
        assertThat(mjup.all[0].additionalAttributes["e"], equalTo(attribs["e"]))
    }

    @Test
    fun testMockMarshaller(){
        val mm = MockMarshaller<Int>()
        // coverage
        assertThat(mm.stream(0), nullValue())
        assertThat(mm.parse(null), nullValue())
    }
}
