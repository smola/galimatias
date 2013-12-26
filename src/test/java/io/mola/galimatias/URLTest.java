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

import java.net.MalformedURLException;
import java.net.URI;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class URLTest {

    @Test
    public void testEquals() throws MalformedURLException {
        for (final URLParserTest.TestURL testURL : URLParserTest.TEST_URLS) {
            final URL originalURL = URL.parse(testURL.original);
            final URL resultURL = URL.parse(testURL.result);
            assertThat(originalURL).isEqualTo(resultURL);
            assertThat(originalURL).isEqualTo(originalURL);
            assertThat(originalURL.hashCode()).isEqualTo(resultURL.hashCode());
        }
    }

    @Test
    public void toFromJavaURI() throws MalformedURLException {
        for (final URLParserTest.TestURL testURL : URLParserTest.TEST_URLS) {
            final URL originalURL = URL.parse(testURL.result);
            final URI toURI = originalURL.toJavaURI();
            assertThat(originalURL).isEqualTo(URL.fromJavaURI(toURI));
        }
    }


    @Test
    public void toFromJavaURL() throws MalformedURLException {
        for (final URLParserTest.TestURL testURL : URLParserTest.TEST_URLS) {
            final URL originalURL = URL.parse(testURL.result);
            final java.net.URL toURL = originalURL.toJavaURL();
            assertThat(originalURL).isEqualTo(URL.fromJavaURL(toURL));
        }
    }

}
