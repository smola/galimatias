/**
 * Copyright (c) 2013-2014, 2018 Santiago M. Mola <santi@mola.io>
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;

import static io.mola.galimatias.CodePoints.*;

/**
 * Percent-encoding operations.
 */
final public class PercentEncoding {

    private PercentEncoding() { }

    public interface PercentEncodeSet {
        boolean match(int c);
    }

    public static void percentEncode(final byte b, StringBuilder buffer) {
        buffer.append('%');
        byteToHex(b, buffer);
    }

    /**
     * Percent-decodes a string.
     *
     * Percent-encoded bytes are assumed to represent UTF-8 characters.
     *
     * @see <a href="http://url.spec.whatwg.org/#percent-encoded-bytes">WHATWG URL Standard: Percent-encoded bytes</a>
     *
     * @param input
     * @return
     */
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
                    if (c <= 0x7F) { // String.getBytes is slow, so do not perform encoding
                        // if not needed
                        bytes.write((byte) c);
                        idx++;
                    } else {
                        bytes.write(new String(Character.toChars(c)).getBytes(UTF_8));
                        idx += Character.charCount(c);
                    }
                    isEOF = idx >= input.length();
                    c = (isEOF)? 0x00 : input.codePointAt(idx);
                }

                if (c == '%' && (input.length() <= idx + 2 ||
                        !isASCIIHexDigit(input.charAt(idx + 1)) ||
                        !isASCIIHexDigit(input.charAt(idx + 2)))) {
                    if (c <= 0x7F) { // String.getBytes is slow, so do not perform encoding
                        // if not needed
                        bytes.write((byte) c);
                        idx++;
                    } else {
                        bytes.write(new String(Character.toChars(c)).getBytes(UTF_8));
                        idx += Character.charCount(c);
                    }
                } else {
                    while (c == '%' && input.length() > idx + 2 &&
                            isASCIIHexDigit(input.charAt(idx + 1)) &&
                            isASCIIHexDigit(input.charAt(idx + 2))) {
                        bytes.write(hexToInt(input.charAt(idx + 1), input.charAt(idx + 2)));
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

    public static String utf8PercentEncode(final String input, final PercentEncodeSet encodeSet) {
        final CodePointIterator it = new CodePointIterator(input);
        final StringBuilder output = new StringBuilder();
        while (it.hasNext()) {
            utf8PercentEncode(it.next(), output, encodeSet);
        }
        return output.toString();
    }

    public static void utf8PercentEncode(final int c, final StringBuilder buffer, final PercentEncodeSet encodeSet) {
        if (encodeSet != null && !encodeSet.match(c)) {
                buffer.appendCodePoint(c);
                return;
        }

        final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
        for (final byte b : bytes) {
            percentEncode(b, buffer);
        }
    }

    public static final PercentEncodeSet C0ControlPercentEncodeSet = (int c) ->
                    isC0Control(c)
                    || c > 0x7E /* ~ */;

    public static final PercentEncodeSet fragmentPercentEncodeSet = (int c) ->
            C0ControlPercentEncodeSet.match(c)
            || c == 0x20 /*   */
            || c == 0x22 /* " */
            || c == 0x3C /* < */
            || c == 0x3E /* > */
            || c == 0x60 /* ` */;

    public static final PercentEncodeSet pathPercentEncodeSet = (int c) ->
            fragmentPercentEncodeSet.match(c)
            || c == 0x23 /* # */
            || c == 0x3F /* ? */
            || c == 0x7B /* { */
            || c == 0x7D /* } */;

    public static final PercentEncodeSet userinfoPercentEncodeSet = (int c) ->
            pathPercentEncodeSet.match(c)
            || c == 0x2F /* / */
            || c == 0x3A /* : */
            || c == 0x3B /* ; */
            || c == 0x3D /* = */
            || c == 0x40 /* @ */
            || c == 0x5B /* [ */
            || c == 0x5C /* \ */
            || c == 0x5D /* ] */
            || c == 0x5E /* ^ */
            || c == 0x7C /* | */;

    private static final char[] _hex = "0123456789ABCDEF".toCharArray();

    private static void byteToHex(final byte b, StringBuilder buffer) {
        final int i = b & 0xFF;
        buffer.append(_hex[i >>> 4]);
        buffer.append(_hex[i & 0x0F]);
    }

    private static int hexToInt(final char c1, final char c2) {
        return Integer.parseInt(new String(new char[]{c1, c2}), 16);
    }

}
