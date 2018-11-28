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

import java.util.Objects;

public class OpaqueHost extends Host {

    private final String host;

    private OpaqueHost(final String host) {
        this.host = host;
    }

    public static OpaqueHost parseOpaqueHost(final String input) throws GalimatiasParseException {
        final CodePointIterator it = new CodePointIterator(input);
        final StringBuilder output = new StringBuilder();
        while (it.hasNext()) {
            final int c = it.next();
            if (c != 0x25 && CodePoints.isForbiddenHost(c)) {
                throw new GalimatiasParseException("opaque host contains forbidden character");
            }
            PercentEncoding.utf8PercentEncode(c, output, PercentEncoding.C0ControlPercentEncodeSet);
        }
        return new OpaqueHost(output.toString());
    }

    @Override
    public int hashCode() {
        return host.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof OpaqueHost)) {
            return false;
        }

        return Objects.equals(host, ((OpaqueHost) obj).host);
    }

    @Override
    public String toString() {
        return host;
    }

    @Override
    public String toHumanString() {
        return host;
    }
}
