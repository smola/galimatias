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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HostTest {

    @Test
    void parseTest() throws GalimatiasParseException {
        assertTrue(Host.parseHost("example.com") instanceof Domain);
        assertTrue(Host.parseHost("[2001:0db8:85a3:08d3:1319:8a2e:0370:7334]") instanceof IPv6Address);
    }

    @Test
    void parseHostWithUnmatchedBracket() {
        assertThrows(GalimatiasParseException.class, () -> Host.parseHost("[2001:0db8:85a3:08d3:1319:8a2e:0370:7334"));
    }

    @Test
    void parseNullHost() {
        assertThrows(NullPointerException.class, () -> Host.parseHost(null));
    }

    @Test
    void parseEmptyHost() {
        assertThrows(GalimatiasParseException.class, () -> Host.parseHost(""));
    }

}
