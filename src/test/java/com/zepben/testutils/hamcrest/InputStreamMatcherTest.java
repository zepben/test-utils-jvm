/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.hamcrest;

import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InputStreamMatcherTest {

    private final InputStreamMatcher matcher1 = InputStreamMatcher.matchesContent("matches one");
    private final InputStreamMatcher matcher2 = InputStreamMatcher.matchesContent("matches two");

    @Test
    public void matchesSafely() {
        assertThat(matcher1.matchesSafely(inputStreamOf("matches one")), equalTo(true));
        assertThat(matcher2.matchesSafely(inputStreamOf("matches two")), equalTo(true));

        assertThat(matcher1.matchesSafely(inputStreamOf("one")), equalTo(false));
        assertThat(matcher2.matchesSafely(inputStreamOf("two")), equalTo(false));

        assertThat(matcher1.matchesSafely(inputStreamOf("mismatches one")), equalTo(false));
        assertThat(matcher2.matchesSafely(inputStreamOf("mismatches two")), equalTo(false));
    }

    @Test
    public void describeTo() {
        Description description = mock(Description.class, Mockito.RETURNS_SELF);
        matcher1.describeTo(description);

        verify(description, times(1)).appendText(any());
        verify(description, times(1)).appendText("InputStream containing ");

        verify(description, times(1)).appendValue(any());
        verify(description, times(1)).appendValue("matches one");
    }

    @Test
    public void describeMismatchSafely() {
        Description description = mock(Description.class, Mockito.RETURNS_SELF);
        matcher1.describeMismatchSafely(inputStreamOf("other stream"), description);

        verify(description, times(1)).appendText(any());
        verify(description, times(1)).appendText(" was ");

        verify(description, times(1)).appendValue(any());
        verify(description, times(1)).appendValue("other stream");
    }

    private InputStream inputStreamOf(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

}
