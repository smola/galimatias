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

import java.util.Arrays;
import java.util.List;

public class Domain extends Host {

    private String[] labels;

    private Domain(final String[] labels) {
        this.labels = labels;
    }

    public static Domain parseDomain(final String input) throws GalimatiasParseException {
        return parseDomain(input, false);
    }

    public List<String> labels() {
        return Arrays.asList(labels);
    }

    public static Domain parseDomain(final String input, final boolean unicode) throws GalimatiasParseException {
        if (input == null) {
            throw new NullPointerException();
        }
        if (input.isEmpty()) {
            throw new GalimatiasParseException("input is empty");
        }

        // WHATWG says: Let host be the result of running utf-8's decoder on the percent decoding of running utf-8 encode on input.
        final String host = URLUtils.percentDecode(input);

        // WHATWG says: Let domain be the result of splitting host on any domain label separators.
        final String[] domain = host.split("[\u002E\u3002\uFF0E\uFF61]");
        if (domain.length == 0) {
            throw new GalimatiasParseException("Zero domain labels found");
        }

        final String[] asciiDomain = URLUtils.domainToASCII(domain);

        for (int i = 0; i < asciiDomain.length; i++) {
            final char[] labelChars = asciiDomain[i].toCharArray();
            for (int j = 0; j < labelChars.length; j++) {
                final char c = labelChars[j];
                if (URLUtils.isASCIIAlpha(c)) {
                    labelChars[j] = Character.toLowerCase(c);
                } else if (c == 0x000 || c == 0x0009 ||c == 0x000A || c == 0x000D || c == 0x0020 || c == '#' || c == '%' || c == '/' || c == ':' || c == '?' || c == '@' || c == '\\') {
                    throw new GalimatiasParseException("Illegal character in host", i);
                }
            }
            asciiDomain[i] = new String(labelChars);
        }

        if (!unicode) {
            return new Domain(asciiDomain);
        }

        return new Domain(URLUtils.domainToUnicode(asciiDomain));
    }

    @Override
    public String toString() {
        if (labels.length == 1) {
            return labels[0];
        }
        final StringBuilder output = new StringBuilder(labels.length * 10);
        output.append(labels[0]);
        for (int i = 1; i < labels.length; i++) {
            output.append('.').append(labels[i]);
        }

        return output.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Domain domain = (Domain) o;

        if (labels.length != domain.labels.length) {
            return false;
        }

        for (int i = 0; i < labels.length; i++) {
            if (!labels[i].equals(domain.labels[i])) {
                return false;
            }

        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(labels);
    }
}
