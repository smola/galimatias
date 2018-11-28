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

import static io.mola.galimatias.PercentEncoding.*;

public class DecodeUnreservedCanonicalizer implements URLCanonicalizer {

    @Override
    public URL canonicalize(final URL input) throws GalimatiasParseException {
        if (input == null) {
            return input;
        }
        URL output = input;
        if (output.isHierarchical()) {
            output = output
                    .withUsername(reencode(output.username(), C0ControlPercentEncodeSet))
                    .withPassword(reencode(output.password(), C0ControlPercentEncodeSet))
                    .withPath(reencode(output.path(), C0ControlPercentEncodeSet));
        }

        final String query = output.query();
        if (query != null && !query.isEmpty()) {
            output = output.withQuery(reencode(query.substring(1), C0ControlPercentEncodeSet));
        }

        final String fragment = output.fragment();
        if (fragment != null && !fragment.isEmpty()) {
            output = output.withFragment(reencode(fragment.substring(1), fragmentPercentEncodeSet));
        }

        return output;
    }

    private static String reencode(final String input, final PercentEncodeSet encodeSet) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        final String decoded = percentDecode(input);
        return utf8PercentEncode(decoded, encodeSet);
    }

}
