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

/**
 * Operations on Unicode code points.
 */
final public class CodePoints {

    private CodePoints() { }

    public static String toString(final int c) {
        return new String(Character.toChars(c));
    }

    public static boolean isC0Control(final int c) {
        // https://infra.spec.whatwg.org/#c0-control
        return c >= 0x00 && c <= 0x1F;
    }

    public static boolean isC0ControlOrSpace(final int c) {
        // https://infra.spec.whatwg.org/#c0-control-or-space
        return isC0Control(c) || c == 0x20;
    }

    public static boolean isASCIITabOrNewline(final int c) {
        // https://infra.spec.whatwg.org/#ascii-tab-or-newline
        return c == 0x09 || c == 0x0A || c == 0x0D;
    }

    public static boolean isASCIIAlpha(final int c) {
        // https://infra.spec.whatwg.org/#ascii-alpha
        return isASCIILowerAlpha(c) || isASCIIUpperAlpha(c);
    }

    public static boolean isASCIILowerAlpha(final int c) {
        // https://infra.spec.whatwg.org/#ascii-lower-alpha
        return c >= 0x61 && c <= 0x7A;
    }

    public static boolean isASCIIUpperAlpha(final int c) {
        // https://infra.spec.whatwg.org/#ascii-upper-alpha
        return c >= 0x41 && c <= 0x5A;
    }

    public static boolean isASCIIDigit(final int c) {
        // https://infra.spec.whatwg.org/#ascii-digit
        return c >= 0x30 && c <= 0x39;
    }

    public static boolean isASCIIHexDigit(final int c) {
        // https://infra.spec.whatwg.org/#ascii-hex-digit
        return isASCIIDigit(c)
                || (c >= 0x41 && c <= 0x46)
                || (c >= 0x61 && c <= 0x66);
    }

    public static boolean isASCIIAlphanumeric(final int c) {
        // https://infra.spec.whatwg.org/#ascii-alphanumeric
        return isASCIIAlpha(c) || isASCIIDigit(c);
    }

    public static boolean isURLCodePoint(final int c) {
        // https://url.spec.whatwg.org/#url-code-points
        return isASCIIAlphanumeric(c)
                || c == 0x21 /* ! */
                || c == 0x24 /* $ */
        || c == 0x26 /* & */
        || c == 0x27 /* ' */
        || c == 0x28 /* ( */
        || c == 0x29 /* ) */
        || c == 0x2A /* * */
        || c == 0x2B /* + */
        || c == 0x2C /* , */
        || c == 0x2D /* - */
        || c == 0x2E /* . */
        || c == 0x2F /* / */
        || c == 0x3A /* : */
        || c == 0x3B /* ; */
        || c == 0x3D /* = */
        || c == 0x3F /* ? */
        || c == 0x40 /* @ */
        || c == 0x5F /* _ */
        || c == 0x7E /* ~ */
        || (c >= 0xA0 && c <= 0x10FFFD && !isSurrogate(c) && !isNonCharacter(c));
    }

    public static boolean isSurrogate(final int c) {
        // https://infra.spec.whatwg.org/#surrogate
        return c >= 0xD800 && c <= 0xDFFF;
    }

    public static boolean isNonCharacter(final int c) {
        // https://infra.spec.whatwg.org/#noncharacter
        return (c >= 0xFDD0 && c <= 0xFDEF)
                || c == 0xFFFE || c == 0xFFFF
                || c == 0x1FFFE || c == 0x1FFFF
                || c == 0x2FFFE || c == 0x2FFFF
                || c == 0x3FFFE || c == 0x3FFFF
                || c == 0x4FFFE || c == 0x4FFFF
                || c == 0x5FFFE || c == 0x5FFFF
                || c == 0x6FFFE || c == 0x6FFFF
                || c == 0x7FFFE || c == 0x7FFFF
                || c == 0x8FFFE || c == 0x8FFFF
                || c == 0x9FFFE || c == 0x9FFFF
                || c == 0xAFFFE || c == 0xAFFFF
                || c == 0xBFFFE || c == 0xBFFFF
                || c == 0xCFFFE || c == 0xCFFFF
                || c == 0xDFFFE || c == 0xDFFFF
                || c == 0xEFFFE || c == 0xEFFFF
                || c == 0xFFFFE || c == 0xFFFFF
                || c == 0x10FFFE || c == 0x10FFFF;
    }

    static boolean isForbiddenHost(final int c) {
        // https://url.spec.whatwg.org/#forbidden-host-code-point
        return c == 0x00
                || c == 0x09
                || c == 0x0A
                || c == 0x0D
                || c == 0x20
                || c == 0x23
                || c == 0x25
                || c == 0x2F
                || c == 0x3A
                || c == 0x3F
                || c == 0x40
                || c == 0x5B
                || c == 0x5C
                || c == 0x5D;
    }
}
