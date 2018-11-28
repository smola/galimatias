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
package io.mola.galimatias.canonicalize;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import io.mola.galimatias.canonicalize.RFC2396Canonicalizer;
import io.mola.galimatias.canonicalize.URLCanonicalizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RFC2396CanonicalizerTest {

    static Stream<Arguments> data() {
        return Arrays.stream(new String[][] {
                new String[]{ "http://example.com/^{}|[]`~", "http://example.com/%5E%7B%7D%7C%5B%5D%60%7E" },
                new String[]{ "http://example.com/?^{}|[]`~", "http://example.com/?%5E%7B%7D%7C%5B%5D%60%7E" },
                new String[]{ "http://example.com/#^{}|[]`~", "http://example.com/#%5E%7B%7D%7C%5B%5D%60%7E" }
        }).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("data")
    void test(final String origin, final String target) throws GalimatiasParseException {
        final URLCanonicalizer canon = new RFC2396Canonicalizer();
        Assertions.assertEquals(URL.parse(target).toString(), canon.canonicalize(URL.parse(origin)).toString());
    }

}
