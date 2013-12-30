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

import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static io.mola.galimatias.TestURL.TestURLs;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class URLTest {

    @Theory
    public void parseURL(final @TestURLs TestURL testURL) throws MalformedURLException {
        final URL url = URL.parse(testURL.base(), testURL.original());
        assertThat(url.toString()).isEqualTo(testURL.result());
    }


    @Theory
    public void parseURLAsRFC2396(final @TestURLs TestURL testURL) throws MalformedURLException {
        final URLParsingSettings settings = URLParsingSettings.create()
                .withStandard(URLParsingSettings.Standard.RFC_2396);

        final URL url = URL.parse(settings, testURL.base(), testURL.original());
        assertThat(url.toString()).isEqualTo(testURL.resultForRFC2396());
    }

    @Theory
    public void userInfoWithUsernameAndPassword(final @TestURLs TestURL testURL) throws MalformedURLException {
        URL url = URL.parse(testURL.base(), testURL.original());
        assumeNotNull(url.username(), url.password());
        assertThat(url.userInfo()).isEqualTo(String.format("%s:%s", url.username(), url.password()));
    }

    @Theory
    public void userInfoWithUsernameOnly(final @TestURLs TestURL testURL) throws MalformedURLException {
        URL url = URL.parse(testURL.base(), testURL.original());
        assumeNotNull(url.username());
        assumeTrue(url.password() == null);
        assertThat(url.userInfo()).isEqualTo(url.username());
    }

    @Theory
    public void userInfoWithPasswordOnly(final @TestURLs TestURL testURL) throws MalformedURLException {
        URL url = URL.parse(testURL.base(), testURL.original());
        assumeNotNull(url.password());
        assumeTrue(url.username() == null || url.username().isEmpty());
        assertThat(url.userInfo()).isEqualTo(String.format(":%s", url.password()));
    }

    @Theory
    public void withScheme(final @TestURLs TestURL testURL) throws MalformedURLException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
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

    @Theory
    public void equality(final @TestURLs TestURL testURL) throws MalformedURLException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        final URL resultURL = URL.parse(testURL.result());
        assertThat(originalURL).isEqualTo(resultURL);
        assertThat(originalURL).isEqualTo(originalURL);
        assertThat(originalURL).isEqualTo(originalURL);
        assertThat(originalURL.hashCode()).isEqualTo(resultURL.hashCode());
        assertThat(originalURL).isNotEqualTo("other");
    }

    @Theory
    public void toFromJavaURI(final @TestURLs TestURL testURL) throws MalformedURLException, URISyntaxException {
        assumeTrue(testURL.isValidURI());

        final URLParsingSettings settings = URLParsingSettings.create()
                .withStandard(URLParsingSettings.Standard.RFC_2396);

        final URL originalURL = URL.parse(settings, testURL.base(), testURL.original());
        final URI toURI = originalURL.toJavaURI();
        assertThat(originalURL).isEqualTo(URL.fromJavaURI(toURI));
    }


    private static final List<String> JAVA_URL_PROTOCOLS = Arrays.asList(
            "http", "https", "ftp", "file", "jar"
    );

    @Theory
    public void toFromJavaURL(final @TestURLs TestURL testURL) throws MalformedURLException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());

        assumeTrue(JAVA_URL_PROTOCOLS.contains(originalURL.scheme()));

        final java.net.URL toURL = originalURL.toJavaURL();
        assertThat(originalURL).isEqualTo(URL.fromJavaURL(toURL));
    }

}
