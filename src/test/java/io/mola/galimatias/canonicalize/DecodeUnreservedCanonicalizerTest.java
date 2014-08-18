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
import io.mola.galimatias.TestURL;
import io.mola.galimatias.URL;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assume.assumeNotNull;

@RunWith(Theories.class)
public class DecodeUnreservedCanonicalizerTest {

    @Test
    public void test() throws GalimatiasParseException {
        final URLCanonicalizer canon = new DecodeUnreservedCanonicalizer();
        for (final String[] pair : new String[][] {
                new String[]{ "http://%41%5A%61%7A%30%39%2D%2E%5F%7E@example.com/", "http://AZaz09-._~@example.com/"},
                new String[]{ "http://:%41%5A%61%7A%30%39%2D%2E%5F%7E@example.com/", "http://:AZaz09-._~@example.com/"},
                new String[]{ "http://example.com/%41%5A%61%7A%30%39%2D%2E%5F%7E", "http://example.com/AZaz09-._~" },
                new String[]{ "http://example.com/?%41%5A%61%7A%30%39%2D%2E%5F%7E", "http://example.com/?AZaz09-._~" },
                new String[]{ "http://example.com/#%41%5A%61%7A%30%39%2D%2E%5F%7E", "http://example.com/#AZaz09-._~" }
        }) {
            assertThat(canon.canonicalize(URL.parse(pair[0])).toString())
                .isEqualTo(URL.parse(pair[1]).toString());
        }
    }

    @Theory
    public void idempotence(final @TestURL.TestURLs(dataset = TestURL.DATASETS.WHATWG) TestURL testURL) throws GalimatiasParseException {
        assumeNotNull(testURL.parsedURL);
        final URLCanonicalizer canon = new DecodeUnreservedCanonicalizer();
        final URL roundOne = canon.canonicalize(testURL.parsedURL);
        final URL roundTwo = canon.canonicalize(roundOne);
        assertThat(roundOne).isEqualTo(roundTwo);
        final URL reparse = URL.parse(roundTwo.toString());
        assertThat(reparse).isEqualTo(roundTwo);
    }

}
