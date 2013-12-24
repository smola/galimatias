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

import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class URLParserTest {

    private static final Logger log = LoggerFactory.getLogger(URLParserTest.class);

    @Test(expected = NullPointerException.class)
    public void parseNullURL() throws MalformedURLException {
        new URLParser().parse(null);
    }

    @Test(expected = MalformedURLException.class)
    public void parseEmptyURL() throws MalformedURLException {
        new URLParser().parse("");
    }

    private static class TestURL {
        String original;
        String result;
        TestURL(String original, String result) {
            this.original = original;
            this.result = result;
        }
    }

    private static final TestURL[] TEST_URLS = new TestURL[] {
            new TestURL("http://example.com/", "http://example.com/"),
            new TestURL("http://example.com", "http://example.com/"),

            new TestURL("http://1.1.1.1/", "http://1.1.1.1/"),
            new TestURL("http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]/", "http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]/"),

            new TestURL("https://example.com:443/", "https://example.com/"),
            new TestURL("ftp://example.com:80/", "ftp://example.com:80/"),

            new TestURL("http://user:pass@example.com/", "http://user:pass@example.com/"),
            new TestURL("http://user@example.com/", "http://user@example.com/"),
            new TestURL("http://user:@example.com/", "http://user:@example.com/"),
            new TestURL("http://:@example.com/", "http://:@example.com/"),


            new TestURL("http://example.com/foo", "http://example.com/foo"),
            new TestURL("http://example.com/foo/", "http://example.com/foo/"),
            new TestURL("http://example.com/foo/../bar", "http://example.com/bar"),
            new TestURL("http://example.com/foo/%2E%2e/bar", "http://example.com/bar"),
            new TestURL("http://example.com/foo/%2E./bar", "http://example.com/bar"),
            new TestURL("http://example.com/foo/./bar", "http://example.com/foo/bar"),
            new TestURL("http://example.com/./bar", "http://example.com/bar"),

            new TestURL("http://example.com/?foo=1", "http://example.com/?foo=1"),
            new TestURL("http://example.com/?foo%c3%9f", "http://example.com/?foo%c3%9f"),
            new TestURL("http://example.com/?fooß", "http://example.com/?foo%C3%9F"),

            new TestURL("http://example.com/#foo", "http://example.com/#foo"),
            new TestURL("http://example.com/?foo=1#foo", "http://example.com/?foo=1#foo"),
            new TestURL("http://example.com/?foo=1#", "http://example.com/?foo=1#"),
            new TestURL("http://example.com/#foo%c3%9f", "http://example.com/#foo%c3%9f"),
            new TestURL("http://example.com/#fooß", "http://example.com/#foo%DF")
    };

    @Test
    public void parseURL() throws MalformedURLException {
        URLParser p = new URLParser();

        for (final TestURL testURL : TEST_URLS) {
            System.out.println("TESTING: " + testURL.original);
            final URL url = p.parse(testURL.original);
            assertThat(url.toString()).isEqualTo(testURL.result);
            url.toJavaURI();
            url.toJavaURL();
        }
    }

    @Test(expected = MalformedURLException.class)
    public void parseOneToken() throws MalformedURLException {
        new URLParser().parse("http").toString();
    }

    @Test
    public void parseURLLeadingWhitespace() throws MalformedURLException {
        URLParser p = new URLParser();
        assertThat(p.parse("   http://google.com/").toString()).isEqualTo("http://google.com/");
    }

    @Test
    public void parseURLUncommonSchemes() throws MalformedURLException {
        URLParser p = new URLParser();

        //TODO: Is this really parsed correctly?
        assertThat(p.parse("aaa://host.example.com:1813;transport=udp;protocol=rad").toString()).isEqualTo("aaa://host.example.com:1813;transport=udp;protocol=rad");
        assertThat(p.parse("about:blank").toString()).isEqualTo("about:blank");
        assertThat(p.parse("adiumxtra://www.adiumxtras.com/download/0000").toString()).isEqualTo("adiumxtra://www.adiumxtras.com/download/0000");
        assertThat(p.parse("aim:goim?screenname=notarealuser&message=This+is+my+message").toString()).isEqualTo("aim:goim?screenname=notarealuser&message=This+is+my+message");
        assertThat(p.parse("apt:gcc").toString()).isEqualTo("apt:gcc");
        assertThat(p.parse("callto:+34600800900").toString()).isEqualTo("callto:+34600800900");
        assertThat(p.parse("ed2k://|file|The_Two_Towers-The_Purist_Edit-Trailer.avi|14997504|965c013e991ee246d63d45ea71954c4d|/|sources,202.89.123.6:4662|/").toString())
                .isEqualTo("ed2k://|file|The_Two_Towers-The_Purist_Edit-Trailer.avi|14997504|965c013e991ee246d63d45ea71954c4d|/|sources,202.89.123.6:4662|/");
        assertThat(p.parse("feed:https://example.com/rss.xml").toString()).isEqualTo("feed:https://example.com/rss.xml");
        assertThat(p.parse("magnet:?xt=urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C").toString())
                .isEqualTo("magnet:?xt=urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C");
        assertThat(p.parse("mailto:user@example.com").toString())
                .isEqualTo("mailto:user@example.com");

    }

    @Test
    public void parseURLWithBase() throws MalformedURLException {
        URLParser p = new URLParser();
        assertThat(p.parse(p.parse("http://example.com"), "/foo").toString())
                .isEqualTo("http://example.com/foo");
        assertThat(p.parse(p.parse("https://example.com"), "//mycdn.com/foo").toString())
                .isEqualTo("https://mycdn.com/foo");
        assertThat(p.parse(p.parse("http://example.com"), "http://mycdn.com/foo").toString())
                .isEqualTo("http://mycdn.com/foo");
        assertThat(p.parse(p.parse("http://example.com"), "http://example.com/foo").toString())
                .isEqualTo("http://example.com/foo");
    }

    @Test
    public void parseURLStateOverride() throws MalformedURLException {
        URLParser p = new URLParser();
        assertThat(p.parse("ftp:", p.parse("http://google.com/"), URLParser.ParseURLState.SCHEME_START).toString())
                .isEqualTo("ftp://google.com/");
        assertThat(p.parse("ftp:", p.parse("http://google.com/"), URLParser.ParseURLState.SCHEME_START).toString())
                .isEqualTo("ftp://google.com/");
    }

    @Test
    public void parseSchemeData() throws MalformedURLException {
        URLParser p = new URLParser();
        assertThat(p.parse("data:foo").toString()).isEqualTo("data:foo");
    }

    @Test
    public void parseURLWithIDNA() throws MalformedURLException {
        URLParser p = new URLParser();
        assertThat(p.parse("http://ジェーピーニック.jp").toString()).isEqualTo("http://xn--hckqz9bzb1cyrb.jp/");
    }

    @Test
    public void parseURLWithErrors() throws MalformedURLException {
        URLParser p = new URLParser();
        //TODO: Check errors
        assertThat(p.parse("http://example.com\\foo\\bar").toString()).isEqualTo("http://example.com/foo/bar");
        //TODO: assertThat(p.parse("http://example.com/^").toString()).isEqualTo("http://example.com/%5E");
    }

    @Test
    public void parseURLFile() throws MalformedURLException {
        URLParser p = new URLParser();
        assertThat(p.parse("file://localhost/etc/fstab").toString()).isEqualTo("file://localhost/etc/fstab");
        assertThat(p.parse("file:////etc/fstab").toString()).isEqualTo("file:////etc/fstab");
        assertThat(p.parse("file:////etc/fstab").toString()).isEqualTo("file:////etc/fstab");
        assertThat(p.parse("file:///c:/WINDOWS/clock.avi").toString()).isEqualTo("file:///c:/WINDOWS/clock.avi");
        assertThat(p.parse("file://localhost/c|/WINDOWS/clock.avi").toString()).isEqualTo("file://localhost/c:/WINDOWS/clock.avi");
        assertThat(p.parse("file:///c|/WINDOWS/clock.avi").toString()).isEqualTo("file:///c:/WINDOWS/clock.avi");
        assertThat(p.parse("file://localhost/c:/WINDOWS/clock.avi").toString()).isEqualTo("file://localhost/c:/WINDOWS/clock.avi");
    }

    @Test
    public void parseIPv6Address() {
        URLParser p = new URLParser();
        assertThat(p.parseIPv6Address("0:0:0:0:0:0:0:1").toString()).isEqualTo("::1");
        assertThat(p.parseIPv6Address("0:0:0:0:0:0:0:0").toString()).isEqualTo("::");
        assertThat(p.parseIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334").toString())
                .isEqualTo("2001:db8:85a3::8a2e:370:7334");
    }

}
