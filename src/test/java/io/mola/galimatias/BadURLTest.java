/**
 * Copyright (c) 2013-2014 Santiago M. Mola <santi@mola.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.mola.galimatias;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BadURLTest {

    @Test
    void parseNullURL() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> URL.parse(null));
        assertEquals("null input", exception.getMessage());

    }

    @Test
    void parseEmptyURL() {
        GalimatiasParseException exception = assertThrows(GalimatiasParseException.class, () -> URL.parse(""));
        assertEquals("Cannot parse relative URL without a base URL", exception.getMessage());
    }

    @Test
    void parseURLwithoutScheme() {
        assertThrows(GalimatiasParseException.class, () -> URL.parse("//scheme-relative-stuff"));
    }

    @Test
    void parseOneToken() {
        assertThrows(GalimatiasParseException.class, () -> URL.parse("http"));
    }

    @Test
    void parseURLWithBadBase() {
        assertThrows(GalimatiasParseException.class, () -> URL.parse(URL.parse("mailto:user@example.com"), "/relative"));
    }

    @Test
    void parseURLWithMalformedScheme() {
        assertThrows(GalimatiasParseException.class, () -> URL.parse("+http://example.com"));
    }

    @Test
    void parseURLWithErrorsStrict() {
        final URLParsingSettings settings = URLParsingSettings.create()
                .withErrorHandler(StrictErrorHandler.getInstance());
        assertThrows(GalimatiasParseException.class, () -> URL.parse(settings, "http://example.com\\foo\\bar"));
    }

    @Test
    void strictTabsInUser(){
        assertThrows(GalimatiasParseException.class, () -> URL.parse(
                URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance()),
                "http://a\tb@example.com"
        ));
    }

    @Test
    void strictCarriageInUser() {
        assertThrows(GalimatiasParseException.class, () -> URL.parse(
                URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance()),
                "http://a\rb@example.com"
        ));
    }

    @Test
    void strictNewlineInUser() {
        assertThrows(GalimatiasParseException.class, () -> URL.parse(
                URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance()),
                "http://a\nb@example.com"
        ));
    }

    @Test
    void strictTabsNewlines() {
        URLParsingSettings strictSettings = URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance());
        for (String replacement : new String[] { "\t", "\n", "\r" }) {
            for (String url : new String[] {
                    "http://a%sb@example.com", "http://a%sb:foo@example.com", "http://foo:a%sb@example.com",
                    "http://a%sb.com", "http://ab.com:2%s2", "http://example.com/a%sb",
                    "http://example.com/?a%sb", "http://exaple.com/#a%sb",
                    "file://host%sname/path"
            }) {
                assertThrows(GalimatiasParseException.class, () -> URL.parse(strictSettings, url));
            }
        }
    }

    @Test
    void strictUnencodedPercentage() {
        URLParsingSettings settings = URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance());
        for (String replacement : new String[] { "%%", "%1Z", "%Z1", "%ZZ" }) {
            for (String url : new String[] {
                    "data:%s", "http://%s@example.com", "http://%s:foo@example.com", "http://foo:%s@example.com",
                    "http://example.com/%s", "http://example.com/?%s", "http://exaple.com/#%s"
            }) {
                final String urlString = String.format(url, replacement);
                assertThrows(GalimatiasParseException.class, () -> URL.parse(settings, urlString));
            }
        }
    }

}
