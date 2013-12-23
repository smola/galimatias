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

    @Test
    public void parseURL() throws MalformedURLException {
        URLParser p = new URLParser();

        // Authority (host)
        assertThat(p.parse("http://example.com/").toString()).isEqualTo("http://example.com/");
        assertThat(p.parse("http://example.com").toString()).isEqualTo("http://example.com/");

        // Authority (IPv4)
        assertThat(p.parse("http://1.1.1.1/").toString()).isEqualTo("http://1.1.1.1/");

        // Authority (IPv6)
        assertThat(p.parse("http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]/").toString())
                .isEqualTo("http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]/");

        // Port
        assertThat(p.parse("https://example.com:443/").toString()).isEqualTo("https://example.com/");
        assertThat(p.parse("ftp://example.com:80/").toString()).isEqualTo("ftp://example.com:80/");

        // Auth
        assertThat(p.parse("http://user:pass@example.com/").toString()).isEqualTo("http://user:pass@example.com/");

        // Path
        assertThat(p.parse("http://example.com/foo").toString()).isEqualTo("http://example.com/foo");
        assertThat(p.parse("http://example.com/foo/").toString()).isEqualTo("http://example.com/foo/");
        //TODO: Should path be treated differently if there is traling slash in the original path?
        assertThat(p.parse("http://example.com/foo/../bar").toString()).isEqualTo("http://example.com/bar");
        assertThat(p.parse("http://example.com/foo/%2E%2e/bar").toString()).isEqualTo("http://example.com/bar");
        assertThat(p.parse("http://example.com/foo/%2E./bar").toString()).isEqualTo("http://example.com/bar");
        assertThat(p.parse("http://example.com/foo/./bar").toString()).isEqualTo("http://example.com/foo/bar");
        assertThat(p.parse("http://example.com/./bar").toString()).isEqualTo("http://example.com/bar");

        // Query string
        assertThat(p.parse("http://example.com/?foo=1").toString()).isEqualTo("http://example.com/?foo=1");
        assertThat(p.parse("http://example.com/?foo%c3%9f").toString()).isEqualTo("http://example.com/?foo%c3%9f");
        assertThat(p.parse("http://example.com/?fooß").toString()).isEqualTo("http://example.com/?foo%C3%9F");

        // Fragment
        assertThat(p.parse("http://example.com/#foo").toString()).isEqualTo("http://example.com/#foo");
        assertThat(p.parse("http://example.com/?foo=1#foo").toString()).isEqualTo("http://example.com/?foo=1#foo");
        assertThat(p.parse("http://example.com/?foo=1#").toString()).isEqualTo("http://example.com/?foo=1#");
        assertThat(p.parse("http://example.com/#foo%c3%9f").toString()).isEqualTo("http://example.com/#foo%c3%9f");
        assertThat(p.parse("http://example.com/#fooß").toString()).isEqualTo("http://example.com/#foo%DF");
    }

    @Test
    public void parseSchemeData() throws MalformedURLException {
        URLParser p = new URLParser();
        assertThat(p.parse("data:foo").toString()).isEqualTo("data:foo");
        assertThat(p.parse("tel:+34600800900").toString()).isEqualTo("tel:+34600800900");
        assertThat(p.parse("magnet:?xt=urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C").toString())
                .isEqualTo("magnet:?xt=urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C");
    }

    @Test
    public void parseURLWithErrors() throws MalformedURLException {
        URLParser p = new URLParser();
        //TODO: Check errors
        assertThat(p.parse("http://example.com\\foo\\bar").toString()).isEqualTo("http://example.com/foo/bar");
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
