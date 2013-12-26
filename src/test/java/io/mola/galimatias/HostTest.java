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

import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class HostTest {

    @Test
    public void parseTest() throws MalformedURLException {
        assertThat(Host.parse("example.com")).isInstanceOf(Domain.class);
        assertThat(Host.parse("[2001:0db8:85a3:08d3:1319:8a2e:0370:7334]")).isInstanceOf(IPv6Address.class);
    }

    @Test(expected = MalformedURLException.class)
    public void parseHostWithUnmatchedBracket() throws MalformedURLException {
        Host.parse("[2001:0db8:85a3:08d3:1319:8a2e:0370:7334");
    }

    @Test(expected = NullPointerException.class)
    public void parseNullHost() throws MalformedURLException {
        Host.parse(null);
    }

    @Test(expected = MalformedURLException.class)
    public void parseEmptyHost() throws MalformedURLException {
        Host.parse("");
    }

}
