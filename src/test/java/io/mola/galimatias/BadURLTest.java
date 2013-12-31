/*
 * Copyright (c) 2013 Santiago M. Mola <santi@mola.io>
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a
 *   copy of this software and associated documentation files (the "Software"),
 *   to deal in the Software without restriction, including without limitation
 *   the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *   and/or sell copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *   OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *   DEALINGS IN THE SOFTWARE.
 */

package io.mola.galimatias;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;

import static io.mola.galimatias.TestURL.TestURLs;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(Theories.class)
public class BadURLTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parseNullURL() throws GalimatiasParseException {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("null input");
        URL.parse(null);
    }

    @Test
    public void parseEmptyURL() throws GalimatiasParseException {
        thrown.expect(GalimatiasParseException.class);
        thrown.expectMessage("empty input");
        URL.parse("");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseURLwithoutScheme() throws GalimatiasParseException {
        URL.parse("//scheme-relative-stuff");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseOneToken() throws GalimatiasParseException {
        URL.parse("http");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseURLWithBadBase() throws GalimatiasParseException {
        URL.parse(URL.parse("mailto:user@example.com"), "/relative");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseURLWithMalformedScheme() throws GalimatiasParseException {
        URL.parse("+http://example.com");
    }

    @Test
    public void parseURLWithErrors() throws GalimatiasParseException {
        assertThat(URL.parse("http://example.com\\foo\\bar").toString()).isEqualTo("http://example.com/foo/bar");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseURLWithErrorsStrict() throws GalimatiasParseException {
        final URLParsingSettings settings = URLParsingSettings.create()
                .withErrorHandler(StrictErrorHandler.getInstance());
        assertThat(URL.parse(settings, "http://example.com\\foo\\bar").toString()).isEqualTo("http://example.com/foo/bar");
    }

    @Theory
    public void withNullScheme(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL url = URL.parse(testURL.base(), testURL.original());
        thrown.expect(NullPointerException.class);
        url.withScheme(null);
    }

    @Theory
    public void withEmptyScheme(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL url = URL.parse(testURL.base(), testURL.original());
        thrown.expect(GalimatiasParseException.class);
        url.withScheme("");
    }

}
