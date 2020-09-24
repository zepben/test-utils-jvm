/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.testutils.hamcrest;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class InputStreamMatcher extends TypeSafeMatcher<InputStream> {

    private final String expected;

    public static InputStreamMatcher matchesContent(String expected) {
        return new InputStreamMatcher(expected);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("InputStream containing ").appendValue(expected);
    }

    @Override
    protected void describeMismatchSafely(InputStream item, Description mismatchDescription) {
        mismatchDescription.appendText(" was ").appendValue(toString(item));
    }

    @Override
    protected boolean matchesSafely(InputStream item) {
        return expected.equals(toString(item));
    }

    private String toString(InputStream inputStream) {
        try {
            int size = inputStream.available();
            byte[] bytes = new byte[size];
            assertThat(inputStream.read(bytes, 0, size), equalTo(size));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStreamMatcher(String expected) {
        this.expected = expected;
    }

}
