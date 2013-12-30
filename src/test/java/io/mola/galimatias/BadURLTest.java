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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class BadURLTest {

    private static final Logger log = LoggerFactory.getLogger(BadURLTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = NullPointerException.class)
    public void parseNullURL() throws MalformedURLException {
        URL.parse(null);
    }

    @Test(expected = MalformedURLException.class)
    public void parseEmptyURL() throws MalformedURLException {
        URL.parse("");
    }

    @Test(expected = MalformedURLException.class)
    public void parseURLwithoutScheme() throws MalformedURLException {
        URL.parse("//scheme-relative-stuff");
    }

    @Test(expected = MalformedURLException.class)
    public void parseOneToken() throws MalformedURLException {
        URL.parse("http");
    }

    @Test(expected = MalformedURLException.class)
    public void parseURLWithBadBase() throws MalformedURLException {
        URL.parse(URL.parse("mailto:user@example.com"), "/relative");
    }

    @Test(expected = MalformedURLException.class)
    public void parseURLWithMalformedScheme() throws MalformedURLException {
        URL.parse("+http://example.com");
    }

    @Test
    public void parseURLWithErrors() throws MalformedURLException {
        //TODO: Check errors
        assertThat(URL.parse("http://example.com\\foo\\bar").toString()).isEqualTo("http://example.com/foo/bar");
    }

}
