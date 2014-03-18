/*
 * Copyright (c) 2014 Santiago M. Mola <santi@mola.io>
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

package io.mola.galimatias.canonicalize;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

import static io.mola.galimatias.URLUtils.*;

public class RFC2396Canonicalizer implements URLCanonicalizer {

    public URL canonicalize(final URL input) throws GalimatiasParseException {
        URL result = input;

        // User
        String user = input.username();
        if (user != null && !user.isEmpty()) {
            StringBuilder newUser = new StringBuilder();
            final int length = user.length();
            for (int offset = 0; offset < length; ) {
                final int c = user.codePointAt(offset);

                if (c == '%' && user.length() > offset + 2 &&
                        isASCIIHexDigit(user.charAt(offset + 1)) && isASCIIHexDigit(user.charAt(offset + 2))) {
                    newUser.append((char) c);
                } else if (isUserInfo(c)) {
                    newUser.append((char)c);
                } else {
                    final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
                    for (final byte b : bytes) {
                        percentEncode(b, newUser);
                    }
                }

                offset += Character.charCount(c);
            }
            result = input.withUsername(newUser.toString());
        }

        // Pass
        String pass = input.password();
        if (pass != null && !pass.isEmpty()) {
            StringBuilder newPass = new StringBuilder();
            final int length = pass.length();
            for (int offset = 0; offset < length; ) {
                final int c = pass.codePointAt(offset);

                if (c == '%' && pass.length() > offset + 2 &&
                        isASCIIHexDigit(pass.charAt(offset + 1)) && isASCIIHexDigit(pass.charAt(offset + 2))) {
                    newPass.append((char) c);
                } else if (isUserInfo(c)) {
                    newPass.append((char)c);
                } else {
                    final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
                    for (final byte b : bytes) {
                        percentEncode(b, newPass);
                    }
                }

                offset += Character.charCount(c);
            }
            result = input.withPassword(newPass.toString());
        }

        // Path
        String path = input.path();
        if (path != null) {
            StringBuilder newPath = new StringBuilder();
            final int length = path.length();
            for (int offset = 0; offset < length; ) {
                final int c = path.codePointAt(offset);

                if (c == '%' && path.length() > offset + 2 &&
                        isASCIIHexDigit(path.charAt(offset + 1)) && isASCIIHexDigit(path.charAt(offset + 2))) {
                    newPath.append((char) c);
                } else if (isPChar(c) || c == '/') {
                    newPath.append((char)c);
                } else {
                    final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
                    for (final byte b : bytes) {
                        percentEncode(b, newPath);
                    }
                }

                offset += Character.charCount(c);
            }
            result = input.withPath(newPath.toString());
        }

        // Query
        String query = input.query();
        if (query != null) {
            StringBuilder newQuery = new StringBuilder();
            final int length = query.length();
            for (int offset = 0; offset < length; ) {
                final int c = query.codePointAt(offset);

                if (c == '%' && query.length() > offset + 2 &&
                        isASCIIHexDigit(query.charAt(offset + 1)) && isASCIIHexDigit(query.charAt(offset + 2))) {
                    newQuery.append((char)c);
                } else if (isUric(c)) {
                    newQuery.append((char)c);
                } else {
                    final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
                    for (final byte b : bytes) {
                        percentEncode(b, newQuery);
                    }
                }

                offset += Character.charCount(c);
            }
            result = input.withQuery(newQuery.toString());
        }

        // Fragment
        String fragment = input.fragment();
        if (fragment != null) {
            StringBuilder newFragment = new StringBuilder();
            final int length = fragment.length();
            for (int offset = 0; offset < length; ) {
                final int c = fragment.codePointAt(offset);

                if (c == '%' && fragment.length() > offset + 2 &&
                        isASCIIHexDigit(fragment.charAt(offset + 1)) && isASCIIHexDigit(fragment.charAt(offset + 2))) {
                    newFragment.append((char) c);
                } else if (isUric(c)) {
                    newFragment.append((char)c);
                } else {
                    final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
                    for (final byte b : bytes) {
                        percentEncode(b, newFragment);
                    }
                }

                offset += Character.charCount(c);
            }
            result = input.withFragment(newFragment.toString());
        }

        return result;
    }

    private boolean isMark(final int c) {
        return c == '-' || c  == '_' || c == '.' || c == '!' || c == '*' || c == '\'' || c ==  '(' || c == ')';
    }

    private boolean isUnreserved(final int c) {
        return isASCIIAlphanumeric(c) || isMark(c);
    }

    private boolean isReserved(final int c) {
        return c == ';' || c == '/' || c == '?' || c == ':' || c == '@' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    private boolean isPChar(final int c) {
        //XXX: "pct-encoded" is pchar, but we check for it before calling this.
        return isUnreserved(c) || c == ':' || c == '@' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    private boolean isUric(final int c) {
        return isReserved(c) || isUnreserved(c);
    }

    private boolean isUserInfo(final int c) {
        //XXX: ':' is excluded here, since we work with user/pass, not userInfo
        return isUnreserved(c) || c == ';' || c == ':' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

}
