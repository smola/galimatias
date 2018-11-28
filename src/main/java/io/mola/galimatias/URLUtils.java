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

import com.ibm.icu.text.IDNA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Utils for parsing and serializing URLs.
 *
 * Not to be confused with the URLUtils from the WHATWG URL spec.
 *
 */
public final class URLUtils {

    private URLUtils() {

    }

    private static final List<String> RELATIVE_SCHEMES = Arrays.asList(
            "ftp", "file", "gopher", "http", "https", "ws", "wss"
    );

    /**
     * Returns true if the schema is a known relative schema
     * (ftp, file, gopher, http, https, ws, wss).
     *
     * @param scheme
     * @return
     */
    public static boolean isRelativeScheme(final String scheme) {
        return RELATIVE_SCHEMES.contains(scheme);
    }

    /**
     * Gets the default port for a given schema. That is:
     *
     * <ol>
     *     <li>ftp - 21</li>
     *     <li>file - null</li>
     *     <li>gopher - 70</li>
     *     <li>http - 80</li>
     *     <li>https - 443</li>
     *     <li>ws - 80</li>
     *     <li>wss - 433</li>
     * </ol>
     *
     * @param scheme
     * @return
     */
    public static Optional<Integer> getDefaultPortForScheme(final String scheme) {
        if ("ftp".equals(scheme)) {
            return Optional.of(21);
        }
        if ("file".equals(scheme)) {
            return Optional.empty();
        }
        if ("gopher".equals(scheme)) {
            return Optional.of(70);
        }
        if ("http".equals(scheme)) {
            return Optional.of(80);
        }
        if ("https".equals(scheme)) {
            return Optional.of(443);
        }
        if ("ws".equals(scheme)) {
            return Optional.of(80);
        }
        if ("wss".equals(scheme)) {
            return Optional.of(443);
        }
        return Optional.empty();
    }

    static String defaultEmpty(final String s) {
        return (s == null)? "" : s;
    }
}
