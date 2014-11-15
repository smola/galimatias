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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assume.*;

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
        thrown.expectMessage("Missing scheme");
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
    public void withSchemeInvalidCharacter(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                               TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withScheme("http%%");
    }

    @Theory
    public void withSchemeStartingNotAlpha(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                           TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withScheme("1foo");
    }

    @Test
    public void strictTabsInUser() throws GalimatiasParseException {
        assertThat(URL.parse("http://a\tb@example.com")).isEqualTo(URL.parse("http://ab@example.com"));
        thrown.expect(GalimatiasParseException.class);
        URL.parse(
                URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance()),
                "http://a\tb@example.com"
        );
    }

    @Test
    public void strictCarriageInUser() throws GalimatiasParseException {
        assertThat(URL.parse("http://a\rb@example.com")).isEqualTo(URL.parse("http://ab@example.com"));
        thrown.expect(GalimatiasParseException.class);
        URL.parse(
                URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance()),
                "http://a\rb@example.com"
        );
    }

    @Test
    public void strictNewlineInUser() throws GalimatiasParseException {
        assertThat(URL.parse("http://a\nb@example.com")).isEqualTo(URL.parse("http://ab@example.com"));
        thrown.expect(GalimatiasParseException.class);
        URL.parse(
                URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance()),
                "http://a\nb@example.com"
        );
    }

    @Test
    public void strictTabsNewlines() throws GalimatiasParseException {
        URLParsingSettings strictSettings = URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance());
        for (String replacement : new String[] { "\t", "\n", "\r" }) {
            for (String url : new String[] {
                    "http://a%sb@example.com", "http://a%sb:foo@example.com", "http://foo:a%sb@example.com",
                    "http://a%sb.com", "http://ab.com:2%s2", "http://example.com/a%sb",
                    "http://example.com/?a%sb", "http://exaple.com/#a%sb",
                    "file://host%sname/path"
            }) {
                final String urlString = String.format(url, replacement);
                assertThat(URL.parse(urlString)).isEqualTo(URL.parse(String.format(url, "")));
                try {
                    URL.parse(strictSettings, urlString);
                    assertThat(false);
                } catch (GalimatiasParseException ex) {
                    assertThat(true);
                }
            }
        }
    }

    @Test
    public void strictUnencodedPercentage() throws GalimatiasParseException {
        URLParsingSettings strictSettings = URLParsingSettings.create().withErrorHandler(StrictErrorHandler.getInstance());
        for (String replacement : new String[] { "%%", "%1Z", "%Z1", "%ZZ" }) {
            for (String url : new String[] {
                    "data:%s", "http://%s@example.com", "http://%s:foo@example.com", "http://foo:%s@example.com",
                    "http://example.com/%s", "http://example.com/?%s", "http://exaple.com/#%s"
            }) {
                final String urlString = String.format(url, replacement);
                URL.parse(urlString);
                try {
                    URL.parse(strictSettings, urlString);
                    assertThat(false);
                } catch (GalimatiasParseException ex) {
                    assertThat(true);
                }
            }
        }
    }

}
