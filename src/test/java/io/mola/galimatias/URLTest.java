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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class URLTest {

    private static final Logger log = LoggerFactory.getLogger(URLTest.class);

    @Test
    public void withScheme() throws MalformedURLException {
        for (final URLParserTest.TestURL testURL : URLParserTest.TEST_URLS) {
            log.debug("TESTING: {}, {}", testURL.original, testURL.base);
            final URL originalURL = URL.parse((testURL.base == null)? null : URL.parse(testURL.base), testURL.original);
            if (originalURL.relativeFlag()) {
                for (final String scheme : new String[] { "http", "https", "ws", "wss", "ftp", "file" }) {
                    assertThat(originalURL.withScheme(scheme).toString()).startsWith(scheme + ":");
                }

            } else {
                for (final String scheme : new String[] { "data", "foobar" }) {
                    assertThat(originalURL.withScheme(scheme).toString()).startsWith(scheme + ":");
                }
            }
        }
    }

    @Test
    public void testEquals() throws MalformedURLException {
        for (final URLParserTest.TestURL testURL : URLParserTest.TEST_URLS) {
            log.debug("TESTING: {}, {}", testURL.original, testURL.base);
            final URL originalURL = URL.parse((testURL.base == null)? null : URL.parse(testURL.base), testURL.original);
            final URL resultURL = URL.parse(testURL.result);
            assertThat(originalURL).isEqualTo(resultURL);
            assertThat(originalURL).isEqualTo(originalURL);
            assertThat(originalURL.hashCode()).isEqualTo(resultURL.hashCode());
        }
    }

    @Test
    public void toFromJavaURI() throws MalformedURLException, URISyntaxException {
        final URLParsingSettings settings = URLParsingSettings.create()
                .withStandard(URLParsingSettings.Standard.RFC_2396);
        for (final URLParserTest.TestURL testURL : URLParserTest.TEST_URLS) {
            log.debug("TESTING: {}, {}", testURL.original, testURL.base);
            if (!testURL.validURI) {
                log.debug("Skipping, invalid URI.");
                continue;
            }
            final URL originalURL = URL.parse(settings, testURL.resultForRFC2396);
            final URI toURI = originalURL.toJavaURI();
            assertThat(originalURL).isEqualTo(URL.fromJavaURI(toURI));
        }
    }


    private static final List<String> JAVA_URL_PROTOCOLS = Arrays.asList(
            "http", "https", "ftp", "file", "jar"
    );

    @Test
    public void toFromJavaURL() throws MalformedURLException {
        for (final URLParserTest.TestURL testURL : URLParserTest.TEST_URLS) {
            log.debug("TESTING: {}, {}", testURL.original, testURL.base);
            final URL originalURL = URL.parse(testURL.result);
            if (!JAVA_URL_PROTOCOLS.contains(originalURL.scheme())) {
                log.debug("Skipping unsupported java.net.URL scheme");
                continue;
            }
            final java.net.URL toURL = originalURL.toJavaURL();
            assertThat(originalURL).isEqualTo(URL.fromJavaURL(toURL));
        }
    }

}
