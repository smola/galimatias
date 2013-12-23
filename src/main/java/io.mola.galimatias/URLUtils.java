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

/**
 * Utils for parsing and serializing URLs.
 *
 * Not to be confused with the URLUtils from the WHATWG URL spec.
 */
public final class URLUtils {

    private URLUtils() {

    }

    public static String percentDecode(final String input) {
        char[] inputChars = input.toCharArray();
        int idx = 0;
        final StringBuilder sb = new StringBuilder(inputChars.length);
        while (idx <= input.length()) {

            boolean isEOF = idx >= input.length();
            char c = (!isEOF)? input.charAt(idx) : 0;

            while (!isEOF && c != '%') {
                sb.append(c);
                idx++;
                isEOF = idx >= input.length();
                c = (!isEOF)? input.charAt(idx) : 0;
            }

            if (!hasRemainingPercentEncoded(inputChars, idx)) {
                sb.append(c);
            } else {
                while (hasRemainingPercentEncoded(inputChars, idx)) {
                    sb.append(_hexDecode(inputChars[idx+1], inputChars[idx+2]));
                    idx += 3;
                }
            }
        }
        return sb.toString();
    }

    static boolean hasRemainingPercentEncoded(final char[] input, int idx) {
        return input.length > idx + 2 && input[idx] == '%' &&
                isASCIIHexDigit(input[idx+1]) && isASCIIHexDigit(input[idx+2]);
    }

    static boolean isASCIIHexDigit(final char c) {
        return (c >= 0x0041 && c <= 0x0046) || (c >= 0x0061 && c <= 0x0066) || isASCIIDigit(c);
    }

    static boolean isASCIIDigit(final char c) {
        return c >= 0x0030 && c <= 0x0039;
    }

    static String[] domainToASCII(final String[] domain) {
        //TODO: Let asciiLabels be an empty list.
        //TODO: On each domain label in input, in order, run the domain label to ASCII algorithm. If that operation failed, return failure. Otherwise, append the result to asciiLabels.
        //TODO: Return asciiLabels.
        return domain;
    }

    static boolean isASCIIAlphaUppercase(final char c) {
        return c >= 0x0061 && c <= 0x007A;
    }

    static boolean isASCIIAlphaLowercase(final char c) {
        return c >= 0x0041 && c <= 0x005A;
    }

    static boolean isASCIIAlpha(final char c) {
        return isASCIIAlphaLowercase(c) || isASCIIAlphaUppercase(c);
    }

    static boolean isASCIIAlphanumeric(final char c) {
        return isASCIIAlpha(c) || isASCIIDigit(c);
    }

    static boolean isURLCodePoint(final char c) {
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

    static enum EncodeSet {
        SIMPLE,
        DEFAULT,
        PASSWORD,
        USERNAME
    }

    static boolean isInSimpleEncodeSet(final char c) {
        return c < 0x0020 || c > 0x007E;
    }

    static boolean isInDefaultEncodeSet(final char c) {
        return isInSimpleEncodeSet(c) || c == '"' || c == '#' || c == '<' || c == '>' || c == '?' || c == '`';
    }

    static boolean isInPasswordEncodeSet(final char c) {
        return isInDefaultEncodeSet(c) || c == '/' || c == '@' || c == '\\';
    }

    static boolean isInUsernameEncodeSet(final char c) {
        return isInPasswordEncodeSet(c) || c == ':';
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

    static void utf8PercentEncode(final char c, final EncodeSet encodeSet, StringBuilder buffer) {
        if (encodeSet == EncodeSet.SIMPLE) {
            if (!isInSimpleEncodeSet(c)) {
                buffer.append((char)c);
                return;
            }
        } else if (encodeSet == EncodeSet.DEFAULT) {
            if (!isInDefaultEncodeSet(c)) {
                buffer.append((char)c);
                return;
            }
        } else if (encodeSet == EncodeSet.PASSWORD) {
            if (!isInPasswordEncodeSet(c)) {
                buffer.append((char)c);
                return;
            }
        } else if (encodeSet == EncodeSet.USERNAME) {
            if (!isInUsernameEncodeSet(c)) {
                buffer.append((char)c);
                return;
            }
        } else {
            throw new IllegalArgumentException("encodeSet");
        }

        //FIXME: Let bytes be the result of running utf-8 encode on code point.

        //FIXME: Percent encode each byte in bytes, and then return them concatenated, in the same order.

        percentEncode((byte) c, buffer);
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
