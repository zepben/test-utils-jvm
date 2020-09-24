/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.mockito;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class DefaultAnswer implements Answer<Object> {

    private final Answer<Object> defaultAnswer;
    private final Map<Class<?>, Object> answers = new HashMap<>();

    public static DefaultAnswer of(Class<?> clazz, Object answer) {
        return of(clazz, answer, Mockito.RETURNS_DEFAULTS);
    }

    public static DefaultAnswer of(Class<?> clazz, Object answer, Answer<Object> defaultAnswer) {
        DefaultAnswer mockitoDefaultAnswer = new DefaultAnswer(defaultAnswer);
        mockitoDefaultAnswer.answers.put(clazz, answer);
        return mockitoDefaultAnswer;
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        if (answers.containsKey(invocation.getMethod().getReturnType()))
            return answers.get(invocation.getMethod().getReturnType());
        else
            return defaultAnswer.answer(invocation);
    }

    public DefaultAnswer and(Class<?> clazz, Object answer) {
        answers.put(clazz, answer);
        return this;
    }

    private DefaultAnswer(Answer<Object> defaultAnswer) {
        this.defaultAnswer = defaultAnswer;
    }

}
