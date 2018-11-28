/**
 * Copyright (c) 2018 Santiago M. Mola <santi@mola.io>
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class URLParserTest {
    @ParameterizedTest
    @MethodSource("io.mola.galimatias.URLTestData#cases")
    void testParseValid(final URLTestData data) throws GalimatiasParseException {
        assumeFalse(data.failure);
        final URL base = URL.parse(data.base);
        final URL url = URL.parse(base, data.input);
        assertURL(data, url);
    }

    /** https://github.com/smola/galimatias/issues/62 */
    @Test
    void testTripleDash() throws GalimatiasParseException {
        final URL url = URL.parse("https://r7---sn-h557snes.googlevideo.com/videoplayback");
        assertEquals(Optional.of("r7---sn-h557snes.googlevideo.com"), url.host().map(Object::toString));
    }

    void assertURL(final URLTestData data, final URL url) {
        assertEquals(data.scheme(), url.scheme());
        assertEquals(data.username, url.username());
        assertEquals(data.password, url.password());
        assertEquals(data.hostname, (url.host().map(Host::toHostString).orElse("")));
        Optional<Integer> port = url.port();
        if (port.equals(url.defaultPort())) {
            port = Optional.empty();
        }
        assertEquals(data.port, port.map(Object::toString).orElse(""));
        assertEquals(data.pathname, url.path());
        assertEquals(data.search, url.query());
        assertEquals(data.hash, url.fragment());
    }
}
