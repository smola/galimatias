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

import java.net.MalformedURLException;
import java.util.StringTokenizer;

public class Domain extends Host {

    private String[] labels;

    private Domain(final String[] labels) {
        this.labels = labels;
    }

    public static Domain parseDomain(final String input) throws MalformedURLException {
        if (input == null) {
            throw new NullPointerException();
        }
        if (input.isEmpty()) {
            throw new MalformedURLException("input is empty");
        }

        //TODO: Let host be the result of running utf-8's decoder on the percent decoding of input.
        final StringTokenizer st = new StringTokenizer(input, "\u002E\u3002\uFF0E\uFF61");
        final String[] domain = new String[st.countTokens()];
        for (int i = 0; i < domain.length; i++) {
            domain[i] = st.nextToken();
        }
        if (domain.length == 0) {
            throw new MalformedURLException("Zero domain labels found");
        }
        return new Domain(domain);
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

}
