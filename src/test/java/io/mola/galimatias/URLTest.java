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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assume.*;

@RunWith(Theories.class)
public class URLTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Theory
    public void parse_url_whatwg(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                     TestURL testURL) throws GalimatiasParseException {
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
    public void parse_url_bad_whatwg(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                         TestURL testURL) throws GalimatiasParseException {
        assumeTrue(testURL.parsedURL == null);
        thrown.expect(GalimatiasParseException.class);
        URL.parse(testURL.parsedBaseURL, testURL.rawURL);
    }

    @Theory
    public void parse_url_host_whatwg(final @TestURL.TestURLs(dataset = TestURL.DATASETS.HOST_WHATWG)
                                          TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        final URL parsedURL = URL.parse(testURL.parsedBaseURL, testURL.rawURL);
        assertThat(parsedURL.host()).isEqualTo(testURL.parsedURL.host());
    }

    @Theory
    public void parse_url_bad_host_whatwg(final @TestURL.TestURLs(dataset = TestURL.DATASETS.HOST_WHATWG)
                                              TestURL testURL) throws GalimatiasParseException {
        assumeTrue(testURL.parsedURL == null);
        thrown.expect(GalimatiasParseException.class);
        URL.parse(testURL.parsedBaseURL, testURL.rawURL);
    }

    @Theory
    public void userInfoWithUsernameAndPassword(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                                    TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeNotNull(testURL.parsedURL.username(), testURL.parsedURL.password());
        assertThat(testURL.parsedURL.userInfo())
                .isEqualTo(String.format("%s:%s", testURL.parsedURL.username(), testURL.parsedURL.password()));
    }

    @Theory
    public void userInfoWithUsernameOnly(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                             TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeNotNull(testURL.parsedURL.username());
        assumeTrue(testURL.parsedURL.password() == null);
        assertThat(testURL.parsedURL.userInfo()).isEqualTo(testURL.parsedURL.username());
    }

    @Theory
    public void userInfoWithPasswordOnly(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                             TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeNotNull(testURL.parsedURL.password());
        assumeTrue("".equals(testURL.parsedURL.username()));
        assertThat(testURL.parsedURL.userInfo()).isEqualTo(":" + testURL.parsedURL.password());
    }

    @Theory
    public void withSchemeFromHierarchical(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                               TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        for (final String scheme : new String[] { "http", "https", "ws", "wss", "ftp", "file" }) {
            assertThat(testURL.parsedURL.withScheme(scheme).scheme()).isEqualTo(scheme);
        }
    }

    @Theory
    public void withNullScheme(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                   TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        thrown.expect(NullPointerException.class);
        testURL.parsedURL.withScheme(null);
    }

    @Theory
    public void withEmptyScheme(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                    TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withScheme("");
    }

    @Theory
    public void withSchemeFromOpaque(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                         TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isOpaque());
        for (final String scheme : new String[] { "data", "foobar" }) {
            assertThat(testURL.parsedURL.withScheme(scheme).scheme()).isEqualTo(scheme);
        }
    }

    @Theory
    public void withUsername(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                 TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        assertThat(testURL.parsedURL.withUsername("user").username()).isEqualTo("user");
        assertThat(testURL.parsedURL.withUsername(null).username()).isEqualTo("");
    }

    @Theory
    public void withUsernameOpaque(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                       TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withUsername("user");
    }

    @Theory
    public void withPassword(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                 TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        assertThat(testURL.parsedURL.withPassword("password").password()).isEqualTo("password");
        assertThat(testURL.parsedURL.withPassword(null).password()).isNull();
    }

    @Theory
    public void withPasswordOpaque(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                       TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withPassword("password");
    }

    @Theory
    public void withHost(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                             TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        assertThat(testURL.parsedURL.withHost("example.com").host()).isEqualTo(Host.parseHost("example.com"));
    }

    @Theory
    public void withHostNoHierarchical(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                           TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(!testURL.parsedURL.isHierarchical());
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withHost("example.com");
    }

    @Theory
    public void withHostNoHierarchical2(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                       TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(!testURL.parsedURL.isHierarchical());
        final Host host = Host.parseHost("example.com");
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withHost(host);
    }

    @Theory
    public void withNullHost(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                 TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        thrown.expect(NullPointerException.class);
        testURL.parsedURL.withHost((String)null);
    }

    @Theory
    public void withNullHost2(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                             TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        thrown.expect(NullPointerException.class);
        testURL.parsedURL.withHost((Host)null);
    }

    @Theory
    public void withHostOpaque(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                   TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withHost("example.com");
    }

    @Theory
    public void withPort(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                             TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        assertThat(testURL.parsedURL.withPort(80).port()).isEqualTo(80);
    }

    @Theory
    public void withPortOpaque(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                   TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withPort(80);
    }

    @Theory
    public void withPath(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                             TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        assertThat(testURL.parsedURL.withPath("/foo/bar").path()).isEqualTo("/foo/bar");
    }

    @Theory
    public void withPathOpaque(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                   TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isOpaque());
        thrown.expect(GalimatiasParseException.class);
        testURL.parsedURL.withPath("/path");
    }

    @Theory
    public void withQuery(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                              TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assertThat(testURL.parsedURL.withQuery("query").query()).isEqualTo("query");
        assertThat(testURL.parsedURL.withQuery("?query").query()).isEqualTo("query");
        assertThat(testURL.parsedURL.withQuery(null).query()).isEqualTo(null);
    }

    @Theory
    public void withFragment(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                 TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assertThat(testURL.parsedURL.withFragment("fragment").fragment()).isEqualTo("fragment");
        assertThat(testURL.parsedURL.withFragment("#fragment").fragment()).isEqualTo("fragment");
        assertThat(testURL.parsedURL.withFragment(null).fragment()).isEqualTo(null);
    }

    @Theory
    public void getAuthorityNullOnOpaque(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                             TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isOpaque());
        assertThat(testURL.parsedURL.authority()).isNull();
    }

    @Theory
    public void getAuthorityNonNullOnHierarchical(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                                      TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.isHierarchical());
        assumeFalse(testURL.parsedURL.scheme().equals("file"));
        assertThat(testURL.parsedURL.authority()).isNotNull();
    }

    @Theory
    public void toFromJavaURI(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                  TestURL testURL) throws GalimatiasParseException, URISyntaxException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(isConvertibleToJavaURI(testURL.parsedURL));
        final URL oneRound = URL.fromJavaURI(testURL.parsedURL.toJavaURI());
        final URL twoRound = URL.fromJavaURI(oneRound.toJavaURI());
        assertThat(oneRound).isEqualTo(twoRound);
    }

    @Theory
    public void toFromJavaURIException(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                              TestURL testURL) throws GalimatiasParseException, URISyntaxException {
        assumeNotNull(testURL.parsedURL);
        assumeFalse(isConvertibleToJavaURI(testURL.parsedURL));
        thrown.expect(URISyntaxException.class);
        testURL.parsedURL.toJavaURI();
    }

    private static boolean isConvertibleToJavaURI(final URL url) {
        if (url.isOpaque() && "//".equals(url.schemeData())) {
            return false;
        }
        if (url.isHierarchical() && url.host() != null && (url.host() instanceof Domain)) {
            final Domain domain = (Domain) url.host();
            return URLUtils.isASCIIAlpha(domain.labels().get(domain.labels().size() - 1).charAt(0));
        }
        return true;
    }

    private static final List<String> JAVA_URL_PROTOCOLS = Arrays.asList(
            "http", "https", "ftp", "file", "jar"
    );

    @Theory
    public void toFromJavaURL(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
                                  TestURL testURL) throws GalimatiasParseException, MalformedURLException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(JAVA_URL_PROTOCOLS.contains(testURL.parsedURL.scheme()));
        final java.net.URL toURL = testURL.parsedURL.toJavaURL();
        assertThat(testURL.parsedURL).isEqualTo(URL.fromJavaURL(toURL));
    }

    @Test
    public void equivalences() throws GalimatiasParseException {
        assertThat(URL.parse("http://example.com/")).isNotEqualTo("http://example.com/");

        assertThat(URL.parse("http://a.com")).isEqualTo(URL.parse("http://a.com/"));
        assertThat(URL.parse("http://a.com").hashCode()).isEqualTo(URL.parse("http://a.com/").hashCode());

        assertThat(URL.parse("http://a.com/")).isNotEqualTo(URL.parse("http://a.com/?"));
        assertThat(URL.parse("http://a.com/").hashCode()).isNotEqualTo(URL.parse("http://a.com/?").hashCode());
        assertThat(URL.parse("http://a.com/?").withQuery(null)).isEqualTo(URL.parse("http://a.com/"));
        assertThat(URL.parse("http://a.com/").withQuery("")).isEqualTo(URL.parse("http://a.com/?"));

        assertThat(URL.parse("http://a.com/")).isNotEqualTo(URL.parse("http://a.com/#"));
        assertThat(URL.parse("http://a.com/").hashCode()).isNotEqualTo(URL.parse("http://a.com/#").hashCode());
        assertThat(URL.parse("http://a.com/#").withFragment(null)).isEqualTo(URL.parse("http://a.com/"));
        assertThat(URL.parse("http://a.com/").withFragment("")).isEqualTo(URL.parse("http://a.com/#"));

    }

    @Test
    public void reduceObjectCreation() throws GalimatiasParseException {
        URL original = URL.parse("http://a.com/?foo");
        assertThat(original == original.withQuery("foo"));
        original = URL.parse("http://a.com/#foo");
        assertThat(original == original.withFragment("foo"));
    }

    @Test(expected = NullPointerException.class)
    public void schemeCannotBeNull() throws GalimatiasParseException {
        new URL(null, null, null, null, Host.parseHost("example.com"), -1, "/", null, null, true);
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

    @Test
    public void relativizeChecks() throws GalimatiasParseException {
        URL base = URL.parse("about:blank");
        assertThat(base.relativize(URL.parse("about:blank/foo"))).isEqualTo("about:blank/foo");
        assertThat(base.relativize(URL.parse("http://example.com/"))).isEqualTo("http://example.com/");

        base = URL.parse("http://example.com/");
        assertThat(base.relativize(URL.parse("about:blank"))).isEqualTo("about:blank");
        assertThat(base.relativize(URL.parse("https://example.com/"))).isEqualTo("https://example.com/");
        assertThat(base.relativize(URL.parse("http://other.com/"))).isEqualTo("http://other.com/");
        assertThat(base.relativize(URL.parse("http://user@example.com/"))).isEqualTo("http://user@example.com/");
        assertThat(base.relativize(URL.parse("http://user:pass@example.com/"))).isEqualTo("http://user:pass@example.com/");
        assertThat(base.relativize(URL.parse("http://:pass@example.com/"))).isEqualTo("http://:pass@example.com/");
        assertThat(base.relativize(URL.parse("http://example.com/foo"))).isEqualTo("foo");
        assertThat(base.relativize(URL.parse("http://example.com/foo?bar"))).isEqualTo("foo?bar");
        assertThat(base.relativize(URL.parse("http://example.com/foo#bar"))).isEqualTo("foo#bar");
        assertThat(base.relativize(URL.parse("http://example.com/foo?bar#baz"))).isEqualTo("foo?bar#baz");

        base = URL.parse("http://example.com/foo?bar#baz");
        assertThat(base.relativize(URL.parse("http://example.com/bar"))).isEqualTo("http://example.com/bar");
        assertThat(base.relativize(URL.parse("http://example.com/foo/bar"))).isEqualTo("bar");
        assertThat(base.relativize(URL.parse("http://example.com/foo/bar"))).isEqualTo("bar");
        assertThat(base.relativize(URL.parse("http://example.com/foo"))).isEqualTo("");
        assertThat(base.relativize(URL.parse("http://example.com/foo/"))).isEqualTo("");
        assertThat(base.relativize(URL.parse("http://example.com/foo?bar#baz"))).isEqualTo("?bar#baz");

        base = URL.parse("http://example.com/foo/?bar#baz");
        assertThat(base.relativize(URL.parse("http://example.com/foo"))).isEqualTo("http://example.com/foo");
        assertThat(base.relativize(URL.parse("http://example.com/foo/"))).isEqualTo("");
        assertThat(base.relativize(URL.parse("http://example.com/foo/?bar#baz"))).isEqualTo("?bar#baz");

        base = URL.parse("file:///etc/fstab");
        assertThat(base.relativize(URL.parse("file://localhost/etc/fstab"))).isEqualTo("file://localhost/etc/fstab");
        assertThat(base.relativize(URL.parse("file:///etc/fstab"))).isEqualTo("");
        assertThat(base.relativize(URL.parse("file:///etc/fstab/bar"))).isEqualTo("bar");
        assertThat(base.relativize(URL.parse("file:///etc/fstab?bar#baz"))).isEqualTo("?bar#baz");

        base = URL.parse("file://localhost/etc/fstab");
        assertThat(base.relativize(URL.parse("file:///etc/fstab"))).isEqualTo("file:///etc/fstab");
    }

    @Test
    public void buildHierarchical() throws GalimatiasParseException {
        assertThat(URL.buildHierarchical("http", "example.com")).isEqualTo(URL.parse("http://example.com"));
        assertThat(URL.buildHierarchical("https", "example.com")).isEqualTo(URL.parse("https://example.com"));
        assertThat(URL.buildHierarchical("ftp", "example.com")).isEqualTo(URL.parse("ftp://example.com"));
    }

    @Test
    public void buildOpaque() throws GalimatiasParseException {
        assertThat(URL.buildOpaque("git")).isEqualTo(URL.parse("git:"));
        assertThat(URL.buildOpaque("ed2k")).isEqualTo(URL.parse("ed2k:"));
    }

    @Test
    public void buildFile() throws GalimatiasParseException {
        assertThat(URL.buildFile()).isEqualTo(URL.parse("file://"));
    }

    @Test(expected = GalimatiasParseException.class)
    public void buildHierarchicalWithOpaqueScheme() throws GalimatiasParseException {
        URL.buildHierarchical("git", "example.com");
    }

    @Test(expected = GalimatiasParseException.class)
    public void buildOpaqueWithHierarchical() throws GalimatiasParseException {
        URL.buildOpaque("http");
    }

    @Theory
    public void toHumanStringIdempotence(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG) TestURL testURL)
        throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        assumeTrue(testURL.parsedURL.host() instanceof Domain);
        Domain domain = (Domain) testURL.parsedURL.host();
        assertThat(domain.toHumanString()).isEqualTo(Domain.parseDomain(domain.toString()).toHumanString());
        assertThat(Domain.parseDomain(domain.toString(), true).toString()).isEqualTo(Domain.parseDomain(domain.toString()).toHumanString());
        assertThat(Domain.parseDomain(domain.toString(), true).toHumanString()).isEqualTo(Domain.parseDomain(domain.toString()).toHumanString());
    }

    @Test
    public void toHumanStringChecks() throws GalimatiasParseException {
        assertThat(URL.parse("http://á.com/").toHumanString()).isEqualTo("http://á.com/");
        assertThat(URL.parse("http://á.com/%2F").toHumanString()).isEqualTo("http://á.com//");
        assertThat(URL.parse("http://á.com/%7E?%2F#%2F").toHumanString()).isEqualTo("http://á.com/~?/#/");
        assertThat(URL.parse("http://á/").toHumanString()).isEqualTo("http://á/");
    }

    @Theory
    public void toHumanStringNoExceptions(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG)
        TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        testURL.parsedURL.toHumanString();
    }

}
