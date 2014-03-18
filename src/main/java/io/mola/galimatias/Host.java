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

import java.io.Serializable;

public abstract class Host implements Serializable {

    @Override
    public abstract String toString();

    public abstract String toHumanString();

    /**
     * Parses a host as found in URLs. IPv6 literals are expected
     * enclosed in square brackets (i.e. [ipv6-literal]).
     *
     * @param input
     * @return
     * @throws GalimatiasParseException
     */
    public static Host parseHost(final String input) throws GalimatiasParseException {
        if (input == null) {
            throw new NullPointerException("null host");
        }
        if (input.isEmpty()) {
            throw new GalimatiasParseException("empty host", -1);
        }
        if (input.charAt(0) == '[') {
            if (input.charAt(input.length() - 1) != ']') {
                throw new GalimatiasParseException("Unmatched '['", -1);
            }
            return IPv6Address.parseIPv6Address(input.substring(1, input.length() - 1));
        }
        final Domain domain = Domain.parseDomain(input);
        try {
            return IPv4Address.parseIPv4Address(domain.toString());
        } catch (GalimatiasParseException e) {
            return domain;
        }
    }

}
