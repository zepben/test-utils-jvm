/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.testutils.mockito

import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

class DefaultAnswer private constructor(
    private val defaultAnswer: Answer<Any>
) : Answer<Any> {

    private val answers: MutableMap<Class<*>, Any> = HashMap()

    @Throws(Throwable::class)
    override fun answer(invocation: InvocationOnMock): Any? =
        answers[invocation.method.returnType] ?: defaultAnswer.answer(invocation)

    inline fun <reified T> and(answer: Any): DefaultAnswer =
        and(T::class.java, answer)

    fun and(clazz: Class<*>, answer: Any): DefaultAnswer {
        answers[clazz] = answer
        return this
    }

    companion object {

        inline fun <reified T> of(
            answer: Any,
            defaultAnswer: Answer<Any> = Mockito.RETURNS_DEFAULTS
        ): DefaultAnswer =
            of(T::class.java, answer, defaultAnswer)

        @JvmStatic
        @JvmOverloads
        fun of(clazz: Class<*>, answer: Any, defaultAnswer: Answer<Any> = Mockito.RETURNS_DEFAULTS): DefaultAnswer =
            DefaultAnswer(defaultAnswer).apply {
                answers[clazz] = answer
            }

    }

}
