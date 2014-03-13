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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static io.mola.galimatias.TestURL.TestURLs;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assume.*;

@RunWith(Theories.class)
public class URLTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Theory
    public void parseURL(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL url = URL.parse(testURL.base(), testURL.original());
        assertThat(url.toString()).isEqualTo(testURL.result());
    }

    @Theory
    public void parse_url_whatwg(final @TestURL3.TestURLs(dataset = TestURL3.DATASETS.WHATWG)
                                     TestURL3 testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        final URL parsedURL = URL.parse(testURL.parsedBaseURL, testURL.rawURL);
        assertThat(parsedURL).isEqualTo(testURL.parsedURL);
        assertThat(parsedURL.scheme()).isEqualTo(testURL.parsedURL.scheme());
        assertThat(parsedURL.schemeData()).isEqualTo(testURL.parsedURL.schemeData());
        assertThat(parsedURL.username()).isEqualTo(testURL.parsedURL.username());
        assertThat(parsedURL.password()).isEqualTo(testURL.parsedURL.password());
        assertThat(parsedURL.host()).isEqualTo(testURL.parsedURL.host());
        assertThat(parsedURL.port()).isEqualTo(testURL.parsedURL.port());
        assertThat(parsedURL.path()).isEqualTo(testURL.parsedURL.path());
        assertThat(parsedURL.query()).isEqualTo(testURL.parsedURL.query());
        assertThat(parsedURL.fragment()).isEqualTo(testURL.parsedURL.fragment());
        assertThat(parsedURL.isHierarchical()).isEqualTo(testURL.parsedURL.isHierarchical());
        assertThat(parsedURL.isOpaque()).isEqualTo(testURL.parsedURL.isOpaque());
    }

    @Theory
    public void parse_url_bad_whatwg(final @TestURL3.TestURLs(dataset = TestURL3.DATASETS.WHATWG)
                                 TestURL3 testURL) throws GalimatiasParseException {
        assumeTrue(testURL.parsedURL == null);
        thrown.expect(GalimatiasParseException.class);
        URL.parse(testURL.parsedBaseURL, testURL.rawURL);
    }

    @Theory
    public void parse_url_host_whatwg(final @TestURL3.TestURLs(dataset = TestURL3.DATASETS.HOST_WHATWG)
                                 TestURL3 testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        final URL parsedURL = URL.parse(testURL.parsedBaseURL, testURL.rawURL);
        assertThat(parsedURL.host()).isEqualTo(testURL.parsedURL.host());
    }

    @Theory
    public void parse_url_bad_host_whatwg(final @TestURL3.TestURLs(dataset = TestURL3.DATASETS.HOST_WHATWG)
                                      TestURL3 testURL) throws GalimatiasParseException {
        assumeTrue(testURL.parsedURL == null);
        thrown.expect(GalimatiasParseException.class);
        URL.parse(testURL.parsedBaseURL, testURL.rawURL);
    }

    @Theory
    public void parseURLAsRFC2396(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URLParsingSettings settings = URLParsingSettings.create()
                .withStandard(URLParsingSettings.Standard.RFC_2396);

        final URL url = URL.parse(settings, testURL.base(), testURL.original());
        assertThat(url.toString()).isEqualTo(testURL.resultForRFC2396());
    }

    @Theory
    public void userInfoWithUsernameAndPassword(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        URL url = URL.parse(testURL.base(), testURL.original());
        assumeNotNull(url.username(), url.password());
        assertThat(url.userInfo()).isEqualTo(String.format("%s:%s", url.username(), url.password()));
    }

    @Theory
    public void userInfoWithUsernameOnly(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        URL url = URL.parse(testURL.base(), testURL.original());
        assumeNotNull(url.username());
        assumeTrue(url.password() == null);
        assertThat(url.userInfo()).isEqualTo(url.username());
    }

    @Theory
    public void userInfoWithPasswordOnly(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        URL url = URL.parse(testURL.base(), testURL.original());
        assumeNotNull(url.password());
        assumeTrue(url.username() == null || url.username().isEmpty());
        assertThat(url.userInfo()).isEqualTo(String.format(":%s", url.password()));
    }

    @Theory
    public void withSchemeFromHierarchical(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isHierarchical());
        for (final String scheme : new String[] { "http", "https", "ws", "wss", "ftp", "file" }) {
            assertThat(originalURL.withScheme(scheme).scheme()).isEqualTo(scheme);
        }
    }

    @Theory
    public void withNullScheme(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        thrown.expect(NullPointerException.class);
        originalURL.withScheme(null);
    }

    @Theory
    public void withEmptyScheme(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        thrown.expect(GalimatiasParseException.class);
        originalURL.withScheme("");
    }

    @Theory
    public void withSchemeFromOpaque(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isOpaque());
        for (final String scheme : new String[] { "data", "foobar" }) {
            assertThat(originalURL.withScheme(scheme).scheme()).isEqualTo(scheme);
        }
    }

    @Theory
    public void withUsername(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isHierarchical());
        assertThat(originalURL.withUsername("user").username()).isEqualTo("user");
        assertThat(originalURL.withUsername(null).username()).isEqualTo("");
    }

    @Theory
    public void withUsernameOpaque(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        originalURL.withUsername("user");
    }

    @Theory
    public void withPassword(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isHierarchical());
        assertThat(originalURL.withPassword("password").password()).isEqualTo("password");
        assertThat(originalURL.withPassword(null).password()).isNull();
    }

    @Theory
    public void withPasswordOpaque(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        originalURL.withPassword("password");
    }

    @Theory
    public void withHost(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isHierarchical());
        assertThat(originalURL.withHost("example.com").host()).isEqualTo(Host.parseHost("example.com"));
    }

    @Theory
    public void withHostNoHierarchical(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(!originalURL.isHierarchical());
        thrown.expect(GalimatiasParseException.class);
        originalURL.withHost("example.com");
    }

    @Theory
    public void withNullHost(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isHierarchical());
        thrown.expect(NullPointerException.class);
        originalURL.withHost((Host) null);
        originalURL.withHost((String)null);
    }

    @Theory
    public void withHostOpaque(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        originalURL.withHost("example.com");
    }

    @Theory
    public void withPort(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isHierarchical());
        assertThat(originalURL.withPort(80).port()).isEqualTo(80);
    }

    @Theory
    public void withPortOpaque(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        originalURL.withPort(80);
    }

    @Theory
    public void withPath(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isHierarchical());
        assertThat(originalURL.withPath("/foo/bar").path()).isEqualTo("/foo/bar");
    }

    @Theory
    public void withPathOpaque(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        originalURL.withPath("/path");
    }

    @Theory
    public void withQuery(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isHierarchical());
        assertThat(originalURL.withQuery("query").query()).isEqualTo("query");
        assertThat(originalURL.withQuery("?query").query()).isEqualTo("query");
        assertThat(originalURL.withQuery(null).query()).isEqualTo(null);
    }

    @Theory
    public void withQueryOpaque(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeTrue(originalURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        originalURL.withQuery("query");
    }

    @Theory
    public void withFragment(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        assumeFalse("javascript".equals(originalURL.scheme()));
        assertThat(originalURL.withFragment("fragment").fragment()).isEqualTo("fragment");
        assertThat(originalURL.withFragment("#fragment").fragment()).isEqualTo("fragment");
        assertThat(originalURL.withFragment(null).fragment()).isEqualTo(null);
    }


    @Theory
    public void equality(final @TestURLs TestURL testURL) throws GalimatiasParseException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());
        final URL resultURL = URL.parse(testURL.result());
        assertThat(originalURL).isEqualTo(resultURL);
        assertThat(originalURL).isEqualTo(originalURL);
        assertThat(originalURL).isEqualTo(originalURL);
        assertThat(originalURL.hashCode()).isEqualTo(resultURL.hashCode());
        assertThat(originalURL).isNotEqualTo("other");
    }

    @Theory
    public void toFromJavaURI(final @TestURLs TestURL testURL) throws GalimatiasParseException, URISyntaxException {
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
    public void toFromJavaURL(final @TestURLs TestURL testURL) throws GalimatiasParseException, MalformedURLException {
        final URL originalURL = URL.parse(testURL.base(), testURL.original());

        assumeTrue(JAVA_URL_PROTOCOLS.contains(originalURL.scheme()));

        final java.net.URL toURL = originalURL.toJavaURL();
        assertThat(originalURL).isEqualTo(URL.fromJavaURL(toURL));
    }

    @Test
    public void internalURLParserChecks() throws GalimatiasParseException {
        // Trying to override scheme without ending with colon should have no effect
        // as per WHATWG spec.
        assertThat(new URLParser("ws", URL.parse("http://example.com"), URLParser.ParseURLState.SCHEME).parse())
                .isEqualTo(URL.parse("http://example.com"));

        assertThat(new URLParser("other.com:222", URL.parse("http://example.com"), URLParser.ParseURLState.HOST).parse())
                .isEqualTo(URL.parse("http://other.com"));
        assertThat(new URLParser("other.com/foo", URL.parse("http://example.com"), URLParser.ParseURLState.HOST).parse())
                .isEqualTo(URL.parse("http://other.com"));
        assertThat(new URLParser("other.com?foo", URL.parse("http://example.com"), URLParser.ParseURLState.HOST).parse())
                .isEqualTo(URL.parse("http://other.com"));
        assertThat(new URLParser("other.com#foo", URL.parse("http://example.com"), URLParser.ParseURLState.HOST).parse())
                .isEqualTo(URL.parse("http://other.com"));

        assertThat(new URLParser("22", URL.parse("http://example.com"), URLParser.ParseURLState.PORT).parse())
                .isEqualTo(URL.parse("http://example.com:22"));
    }

}
