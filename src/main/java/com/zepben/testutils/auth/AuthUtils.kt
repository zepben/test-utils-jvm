/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.auth

import com.auth0.jwk.Jwk
import com.auth0.jwk.UrlJwkProvider
import io.grpc.*
import java.io.InputStream

class MockMarshaller<T> : MethodDescriptor.Marshaller<T> {
    override fun stream(value: T): InputStream? = null
    override fun parse(stream: InputStream?): T? = null
}

class MockServerCallHandler<T, Y>(val startCallCallback: (ServerCall<T, Y>, Metadata) -> Unit) :
    ServerCallHandler<T, Y> {
    override fun startCall(call: ServerCall<T, Y>?, headers: Metadata?): ServerCall.Listener<T>? {
        startCallCallback(call!!, headers!!)
        return null
    }
}

class MockServerCall<ReqT, RespT>(
    private val closeCallback: (Status?, Metadata?) -> Unit,
    private val fullMethodName: String = "zepben.protobuf.np.NetworkProducer/testMethod"
) : ServerCall<ReqT, RespT>() {
    override fun sendMessage(message: RespT) {}
    override fun sendHeaders(headers: Metadata?) {}
    override fun getMethodDescriptor(): MethodDescriptor<ReqT, RespT> =
        MethodDescriptor.newBuilder<ReqT, RespT>()
            .setType(MethodDescriptor.MethodType.UNKNOWN)
            .setRequestMarshaller(MockMarshaller<ReqT>())
            .setResponseMarshaller(MockMarshaller<RespT>())
            .setFullMethodName(fullMethodName).build()

    override fun close(status: Status?, trailers: Metadata?) {
        closeCallback(status, trailers)
    }

    override fun request(numMessages: Int) {}
    override fun isCancelled(): Boolean = false
}

/**
 * n = modulus of public key.
 * e = exponent of public key. This is used to verify the [TOKEN]s below, derived from the corresponding PUBLIC KEY as follows:
 * 1. remove the BEGIN and END lines and join the rest to a single line/string
 * 2. delete the first 44 and last 6 characters (encoding the first 33 and last 4.5 bytes)
 * 3. change any + and / characters to - and _ respectively
 */
val attribs = mapOf(
    "n" to "nzyis1ZjfNB0bBgKFMSvvkTtwlvBsaJq7S5wA-kzeVOVpVWwkWdVha4s38XM_pa_yr47av7-z3VTmvDRyAHcaT92whREFpLv9cj5lTeJSibyr_Mrm_YtjCZVWgaOYIhwrXwKLqPr_11inWsAkfIytvHWTxZYEcXLgAXFuUuaS3uF9gEiNQwzGTU1v0FqkqTBr4B8nW3HCN47XUu0t8Y0e-lf4s4OxQawWD79J9_5d3Ry0vbV3Am1FtGJiJvOwRsIfVChDpYStTcHTCMqtvWbV6L11BWkpzGXSW4Hv43qa-GSYOD2QU68Mb59oSk2OB-BtOLpJofmbGEGgvmwyCI9Mw",
    "e" to "AQAB"
)

class MockJwksUrlProvider : UrlJwkProvider("fake") {
    override fun getAll(): List<Jwk> {
        return listOf(Jwk("fakekid", "RSA", "RS256", "", emptyList(), "", emptyList(), "", attribs))
    }
}

const val TOKEN =
    "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImZha2VraWQifQ.eyJpc3MiOiJodHRwczovL2lzc3Vlci8iLCJzdWIiOiJmYWtlIiwiYXVkIjoiaHR0cHM6Ly9mYWtlLWF1ZC8iLCJpYXQiOjE1OTE4MzQxNzksImV4cCI6OTU5MTkyMDU3OSwiYXpwIjoid2U5ZDNSME5jTUNWckpDZ2ROSWVmWWx6aHo2VE9SaGciLCJzY29wZSI6IndyaXRlOm5ldHdvcmsgcmVhZDpuZXR3b3JrIHdyaXRlOm1ldHJpY3MgcmVhZDpld2IiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMiLCJwZXJtaXNzaW9ucyI6WyJ3cml0ZTpuZXR3b3JrIiwicmVhZDpuZXR3b3JrIiwid3JpdGU6bWV0cmljcyIsInJlYWQ6ZXdiIl19.ay_YTwRsfcNzVdmQ4EgmuNMMypfZIIc8K9dCCtLqUmUJDtE7NUuKaVAmGDdmW1J-ngm0UsH4k6B5QpPIJnLIROpdDf7aRzdE9hNFuSHR3arpyCzmO2-TiFDZLFXQjHf0Q-BaxGoXLQBupGYuQaG_3flaLPB3hPV0nqPoBTIoJgG8n2w0Uo2tePe_y2Blqco1sK2wElwyMlYc-UuTyFSvwKlpSXYmO4ppVmbAa9lS2ley6lcv2TwXLCk0KfIIH2E5OBvJHevZqYEzFBAeLCnahKoWxexsVvEfZr40Nhc6oPRT5yJfHRBnCrDnO1fE96rqguQpsDG-HWCtd2GkpnAXNg"
const val TOKEN_RS512 =
    "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCIsImtpZCI6ImZha2VraWQifQ.eyJpc3MiOiJodHRwczovL2lzc3Vlci8iLCJzdWIiOiJmYWtlIiwiYXVkIjoiaHR0cHM6Ly9mYWtlLWF1ZC8iLCJpYXQiOjE1OTE4MzQxNzksImV4cCI6OTU5MTkyMDU3OSwiYXpwIjoid2U5ZDNSME5jTUNWckpDZ2ROSWVmWWx6aHo2VE9SaGciLCJzY29wZSI6IndyaXRlOm5ldHdvcmsgcmVhZDpuZXR3b3JrIHdyaXRlOm1ldHJpY3MgcmVhZDpld2IiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMiLCJwZXJtaXNzaW9ucyI6WyJ3cml0ZTpuZXR3b3JrIiwicmVhZDpuZXR3b3JrIiwid3JpdGU6bWV0cmljcyIsInJlYWQ6ZXdiIl19.bDmxSoCGcFoRSIRTdck7q5ifUVor-FZN80RbL4zi9HqanaGKaCtuSkuaWXCYEidfJRIddtskpf7fXKIZkVcMc7ZDcMNMp27g4nyTZXiMrtuwBHHmkgJOauMYsbdpLiUQJL8JfnzhYYKURww-FvZem6YjvoCjh3AwCZJBxam9ruO9Po_suuoid2kHW_h8SBhzpKrcivYgFEe1sN_9-3u_XcMHYwv353-miOwn6E7KSCJDL7_QzalYoEIwJIUs3BDAz6gi6QYyCyLgKeps3bnAJthgWViKtNfjZxtGOqhZU4d-3VQ5tjXhmIf5eSZOWVHYf-Txdgi-js0OVQgXCPSm7A"
const val TOKEN_BAD_SIG =
    "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImZha2VraWQifQ.eyJpc3MiOiJodHRwczovL2lzc3Vlci8iLCJzdWIiOiJmYWtlIiwiYXVkIjoiaHR0cHM6Ly9mYWtlLWF1ZC8iLCJpYXQiOjE1ODkzNDE0ODEsImV4cCI6OTk5OTk5OTk5OSwiYXpwIjoid2U5ZDNSME5jTUNWckpDZ2ROSWVmWWx6aHo2VE9SaGciLCJzY29wZSI6IndyaXRlOm5ldHdvcmsgcmVhZDpuZXR3b3JrIHdyaXRlOm1ldHJpY3MiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMifQ.lrcIxBOCil4lysTVMCJozK9y8h5YroNQjzxHgyKkq7L67KZx5HImtnkj6fojGzANDoE87FyfZHbSg2Mj9iYzhdXe1WHmkVtJngtXXSwocJcqKg4uZLmNndwXgqaeiKUZOw9FYIZsmuu71aixQuPGO2cjfgbqwnSbgdO0B1a0pqdREkheJESRYfpDPWcFJ3Ez3bmd7sOQxAj0yvJVh67tikWS2tNB8QOEGFiPpDT0-9LJxtjXGon-v2Zyw0x5NCsAqUSMiu2Afx6tPXM23FRIM5yYG0NyWMzRePFGUAu5oD7vczdXiavMbr5fFJiPT6KhSm_LNy8cih5YB-EDApLQBw"
const val TOKEN_EXPIRED =
    "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImZha2VraWQifQ.eyJpc3MiOiJodHRwczovL2lzc3Vlci8iLCJzdWIiOiJmYWtlIiwiYXVkIjoiaHR0cHM6Ly9mYWtlLWF1ZC8iLCJpYXQiOjE1ODkzNDE0ODEsImV4cCI6MTU4OTM0MTQ4Mn0.MVBVct15YROdqYGkOWjRVIGL3ZIgmeD2VSI-CkF7Vo0cwOeEkOytbnYY9Q34PaPltuiSUgi6lEdzRVOhTMvIiiYcLBuKtD1hLx3cCbEe8LLUMkc9hA-wR4OqYyxyFwEBkA_PQnFRO9BGS79NzBaIHJJyvtwmfIqDtevnVBhHod0hHLwwgi1-uspL3EPy-VBkkwKrICdJB7p-orrTF608EHFAGZ99dL5yxsvGzIgQlY75BTU1bDJ0Hdt9-CNCBgYufDuMMuObSyh67WMuakLQuUZ6G6Jw-YqrHV5wv1j5-b9skSeRSeVCu_bnKd-KEqjHNm5LB2HaT0g7WDYU79d5eQ"
/**
 * Token decoded:
 * {
 * "alg": "RS256",
 * "typ": "JWT",
 * "kid": "fakekid"
 * }
 * {
 * "iss": "https://issuer/",
 * "sub": "fake",
 * "aud": "https://fake-aud/",
 * "iat": 1589341481,
 * "exp": 9999999999,
 * "azp": "we9d3R0NcMCVrJCgdNIefYlzhz6TORhg",
 * "scope": "write:network read:network write:metrics",
 * "gty": "client-credentials"
 * }
 * Public key used for verifying this JWT:
 * -----BEGIN PUBLIC KEY-----
 * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnzyis1ZjfNB0bBgKFMSv
 * vkTtwlvBsaJq7S5wA+kzeVOVpVWwkWdVha4s38XM/pa/yr47av7+z3VTmvDRyAHc
 * aT92whREFpLv9cj5lTeJSibyr/Mrm/YtjCZVWgaOYIhwrXwKLqPr/11inWsAkfIy
 * tvHWTxZYEcXLgAXFuUuaS3uF9gEiNQwzGTU1v0FqkqTBr4B8nW3HCN47XUu0t8Y0
 * e+lf4s4OxQawWD79J9/5d3Ry0vbV3Am1FtGJiJvOwRsIfVChDpYStTcHTCMqtvWb
 * V6L11BWkpzGXSW4Hv43qa+GSYOD2QU68Mb59oSk2OB+BtOLpJofmbGEGgvmwyCI9
 * MwIDAQAB
 * -----END PUBLIC KEY-----
 * Private key used for generating more tokens (for reference and to be used in tests only)
 * -----BEGIN RSA PRIVATE KEY-----
 * MIIEogIBAAKCAQEAnzyis1ZjfNB0bBgKFMSvvkTtwlvBsaJq7S5wA+kzeVOVpVWw
 * kWdVha4s38XM/pa/yr47av7+z3VTmvDRyAHcaT92whREFpLv9cj5lTeJSibyr/Mr
 * m/YtjCZVWgaOYIhwrXwKLqPr/11inWsAkfIytvHWTxZYEcXLgAXFuUuaS3uF9gEi
 * NQwzGTU1v0FqkqTBr4B8nW3HCN47XUu0t8Y0e+lf4s4OxQawWD79J9/5d3Ry0vbV
 * 3Am1FtGJiJvOwRsIfVChDpYStTcHTCMqtvWbV6L11BWkpzGXSW4Hv43qa+GSYOD2
 * QU68Mb59oSk2OB+BtOLpJofmbGEGgvmwyCI9MwIDAQABAoIBACiARq2wkltjtcjs
 * kFvZ7w1JAORHbEufEO1Eu27zOIlqbgyAcAl7q+/1bip4Z/x1IVES84/yTaM8p0go
 * amMhvgry/mS8vNi1BN2SAZEnb/7xSxbflb70bX9RHLJqKnp5GZe2jexw+wyXlwaM
 * +bclUCrh9e1ltH7IvUrRrQnFJfh+is1fRon9Co9Li0GwoN0x0byrrngU8Ak3Y6D9
 * D8GjQA4Elm94ST3izJv8iCOLSDBmzsPsXfcCUZfmTfZ5DbUDMbMxRnSo3nQeoKGC
 * 0Lj9FkWcfmLcpGlSXTO+Ww1L7EGq+PT3NtRae1FZPwjddQ1/4V905kyQFLamAA5Y
 * lSpE2wkCgYEAy1OPLQcZt4NQnQzPz2SBJqQN2P5u3vXl+zNVKP8w4eBv0vWuJJF+
 * hkGNnSxXQrTkvDOIUddSKOzHHgSg4nY6K02ecyT0PPm/UZvtRpWrnBjcEVtHEJNp
 * bU9pLD5iZ0J9sbzPU/LxPmuAP2Bs8JmTn6aFRspFrP7W0s1Nmk2jsm0CgYEAyH0X
 * +jpoqxj4efZfkUrg5GbSEhf+dZglf0tTOA5bVg8IYwtmNk/pniLG/zI7c+GlTc9B
 * BwfMr59EzBq/eFMI7+LgXaVUsM/sS4Ry+yeK6SJx/otIMWtDfqxsLD8CPMCRvecC
 * 2Pip4uSgrl0MOebl9XKp57GoaUWRWRHqwV4Y6h8CgYAZhI4mh4qZtnhKjY4TKDjx
 * QYufXSdLAi9v3FxmvchDwOgn4L+PRVdMwDNms2bsL0m5uPn104EzM6w1vzz1zwKz
 * 5pTpPI0OjgWN13Tq8+PKvm/4Ga2MjgOgPWQkslulO/oMcXbPwWC3hcRdr9tcQtn9
 * Imf9n2spL/6EDFId+Hp/7QKBgAqlWdiXsWckdE1Fn91/NGHsc8syKvjjk1onDcw0
 * NvVi5vcba9oGdElJX3e9mxqUKMrw7msJJv1MX8LWyMQC5L6YNYHDfbPF1q5L4i8j
 * 8mRex97UVokJQRRA452V2vCO6S5ETgpnad36de3MUxHgCOX3qL382Qx9/THVmbma
 * 3YfRAoGAUxL/Eu5yvMK8SAt/dJK6FedngcM3JEFNplmtLYVLWhkIlNRGDwkg3I5K
 * y18Ae9n7dHVueyslrb6weq7dTkYDi3iOYRW8HRkIQh06wEdbxt0shTzAJvvCQfrB
 * jg/3747WSsf/zBTcHihTRBdAv6OmdhV4/dD5YBfLAkLrd+mX7iE=
 * -----END RSA PRIVATE KEY-----
 */
