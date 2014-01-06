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

import java.awt.image.ImagingOpException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.IDN;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Utils for parsing and serializing URLs.
 *
 * Not to be confused with the URLUtils from the WHATWG URL spec.
 *
 */
class URLUtils {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private URLUtils() {

    }

    public static String percentDecode(final String input) {
        if (input.isEmpty()) {
            return input;
        }
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            int idx = 0;
            while (idx < input.length()) {

                boolean isEOF = idx >= input.length();
                int c = (isEOF)? 0x00 : input.codePointAt(idx);

                while (!isEOF && c != '%') {
                    bytes.write(new String(Character.toChars(c)).getBytes(UTF_8));
                    idx += Character.charCount(c);
                    isEOF = idx >= input.length();
                    c = (isEOF)? 0x00 : input.codePointAt(idx);
                }

                if (c == '%' && (input.length() <= idx + 2 ||
                        !isASCIIHexDigit(input.charAt(idx + 1)) ||
                        !isASCIIHexDigit(input.charAt(idx + 2)))) {
                    bytes.write(new String(Character.toChars(c)).getBytes(UTF_8));
                    idx += Character.charCount(c);
                } else {
                    while (c == '%' && input.length() > idx + 2 &&
                            isASCIIHexDigit(input.charAt(idx + 1)) &&
                            isASCIIHexDigit(input.charAt(idx + 2))) {
                        bytes.write(_hexDecode(input.charAt(idx + 1), input.charAt(idx + 2)));
                        idx += 3;
                        c = (input.length() <= idx)? 0x00 : input.codePointAt(idx);
                    }
                }
            }
            return new String(bytes.toByteArray(), UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * <strong>domain to ASCII</strong> algorithm.
     *
     * @todo Handle failures.
     *
     * @see <a href="http://url.spec.whatwg.org/#idna">WHATWG URL Standard - IDNA Section</a>
     *
     * @param domainLabels
     * @return
     */
    static String[] domainToASCII(final String[] domainLabels) {
        final List<String> asciiLabels = new ArrayList<String>();
        for (final String domainLabel : domainLabels) {
            //XXX: The lowercasing is added here as it's the most sane thing to do
            //     and browsers do it. However, WHATWG URL does not specify this.
            //     See https://www.w3.org/Bugs/Public/show_bug.cgi?id=24187
            asciiLabels.add(domainLabelToASCII(domainLabel).toLowerCase(Locale.ENGLISH));
        }
        final String[] result = new String[asciiLabels.size()];
        return asciiLabels.toArray(result);
    }

    /**
     * <strong>domain to Unicode</strong> algorithm.
     *
     * @see <a href="http://url.spec.whatwg.org/#idna">WHATWG URL Standard - IDNA Section</a>
     *
     * @param domainLabels
     * @return
     */
    static String[] domainToUnicode(final String[] domainLabels) {
        final List<String> unicodeLabels = new ArrayList<String>();
        for (final String domainLabel : domainLabels) {
            unicodeLabels.add(domainLabelToUnicode(domainLabel));
        }
        return (String[]) unicodeLabels.toArray();
    }

    /**
     * <strong>domain label to ASCII</strong> algorithm.
     *
     * This happens to be {@link java.net.IDN#toASCII(String,int)} with the
     * {@link java.net.IDN#ALLOW_UNASSIGNED} flag set.
     *
     * @see <a href="http://url.spec.whatwg.org/#idna">WHATWG URL Standard - IDNA Section</a>
     *
     * @param input
     * @return
     */
    static String domainLabelToASCII(final String input) {
        return IDN.toASCII(input, IDN.ALLOW_UNASSIGNED);
    }

    /**
     * <strong>domain label to Unicode</strong> algorithm.
     *
     * This happens to be {@link java.net.IDN#toUnicode(String,int)} with the
     * {@link java.net.IDN#ALLOW_UNASSIGNED} flag set.
     *
     * @see <a href="http://url.spec.whatwg.org/#idna">WHATWG URL Standard - IDNA Section</a>
     *
     * @param input
     * @return
     */
    static String domainLabelToUnicode(final String input) {
        return IDN.toUnicode(input, IDN.ALLOW_UNASSIGNED);
    }

    static boolean isASCIIHexDigit(final int c) {
        return (c >= 0x0041 && c <= 0x0046) || (c >= 0x0061 && c <= 0x0066) || isASCIIDigit(c);
    }

    static boolean isASCIIDigit(final int c) {
        return c >= 0x0030 && c <= 0x0039;
    }

    static boolean isASCIIAlphaUppercase(final int c) {
        return c >= 0x0061 && c <= 0x007A;
    }

    static boolean isASCIIAlphaLowercase(final int c) {
        return c >= 0x0041 && c <= 0x005A;
    }

    static boolean isASCIIAlpha(final int c) {
        return isASCIIAlphaLowercase(c) || isASCIIAlphaUppercase(c);
    }

    static boolean isASCIIAlphanumeric(final int c) {
        return isASCIIAlpha(c) || isASCIIDigit(c);
    }

    static boolean isURLCodePoint(final int c) {
        return
                isASCIIAlphanumeric(c) ||
                        c == '!' ||
                        c == '$' ||
                        c == '&' ||
                        c == '\'' ||
                        c == '(' ||
                        c == ')' ||
                        c == '*' ||
                        c == '+' ||
                        c == ',' ||
                        c == '-' ||
                        c == '.' ||
                        c == '/' ||
                        c == ':' ||
                        c == ';' ||
                        c == '=' ||
                        c == '?' ||
                        c == '@' ||
                        c == '_' ||
                        c == '~' ||
                        (c >= 0x00A0 && c <= 0xD7FF) ||
                        (c >= 0xE000 && c <= 0xFDCF) ||
                        (c >= 0xFDF0 && c <= 0xFFEF) ||
                        (c >= 0x10000 && c <= 0x1FFFD) ||
                        (c >= 0x20000 && c <= 0x2FFFD) ||
                        (c >= 0x30000 && c <= 0x3FFFD) ||
                        (c >= 0x40000 && c <= 0x4FFFD) ||
                        (c >= 0x50000 && c <= 0x5FFFD) ||
                        (c >= 0x60000 && c <= 0x6FFFD) ||
                        (c >= 0x70000 && c <= 0x7FFFD) ||
                        (c >= 0x80000 && c <= 0x8FFFD) ||
                        (c >= 0x90000 && c <= 0x9FFFD) ||
                        (c >= 0xA0000 && c <= 0xAFFFD) ||
                        (c >= 0xB0000 && c <= 0xBFFFD) ||
                        (c >= 0xC0000 && c <= 0xCFFFD) ||
                        (c >= 0xD0000 && c <= 0xDFFFD) ||
                        (c >= 0xE0000 && c <= 0xEFFFD) ||
                        (c >= 0xF0000 && c <= 0xFFFFD) ||
                        (c >= 0x100000 && c <= 0x10FFFD);
    }

    private static final char[] _hex = "0123456789ABCDEF".toCharArray();
    static void byteToHex(final byte b, StringBuilder buffer) {
        int i = b & 0xFF;
        buffer.append(_hex[i >>> 4]);
        buffer.append(_hex[i & 0x0F]);
    }

    static int _hexDecode(final char c1, final char c2) {
        //TODO: Some micro-optimization here?
        return Integer.parseInt(new String(new char[]{c1, c2}), 16);
    }

    static void percentEncode(final byte b, StringBuilder buffer) {
        buffer.append('%');
        byteToHex(b, buffer);
    }

    private static final List<String> RELATIVE_SCHEMES = Arrays.asList(
            "ftp", "file", "gopher", "http", "https", "ws", "wss"
    );
    static boolean isRelativeScheme(final String scheme) {
        return RELATIVE_SCHEMES.contains(scheme);
    }

    static String getDefaultPortForScheme(final String scheme) {
        if ("ftp".equals(scheme)) {
            return "21";
        }
        if ("file".equals(scheme)) {
            return null;
        }
        if ("gopher".equals(scheme)) {
            return "70";
        }
        if ("http".equals(scheme)) {
            return "80";
        }
        if ("https".equals(scheme)) {
            return "443";
        }
        if ("ws".equals(scheme)) {
            return "80";
        }
        if ("wss".equals(scheme)) {
            return "443";
        }
        return null;
    }

}
