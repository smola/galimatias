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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Locale;
import static io.mola.galimatias.URLUtils.*;

/**
 *  http://url.spec.whatwg.org/
 *
 *  NOTES:
 *
 *  - Lowercase percent-encoded bytes are not uppercased, although
 *    it's good practice to uppercase then. Is this work for the canonicalizer
 *    or the parser itself? Google Chrome does not normalize here.
 *
 *  - Why utf-8 percent-encoding in fragment and only percent-encoding in query?
 *
 *  TODO: This still does not perform percent-decoding of unreserved characters
 *       on parsing. It seems WebKit does decode while Gecko does not (or does in
 *       a limited fashion).
 *       https://www.w3.org/Bugs/Public/show_bug.cgi?id=24164
 *
 *  TODO: Unsafe characters are not percent-encoded: ^, {, }, [, ], |... the URL
 *       Spec does not specify their encoding, but WebKit/Gecko encodes them,
 *       which is aligned with relevant RFCs and common practices.
 *       https://www.w3.org/Bugs/Public/show_bug.cgi?id=24163
 */
public class URLParser {

    private static final Logger log = LoggerFactory.getLogger(URLParser.class);

    public URLParser() {

    }

    /**
     * Parse URL states as defined by WHATWG URL spec.
     *
     * http://url.spec.whatwg.org/#scheme-start-state
     */
    public static enum ParseURLState {
        SCHEME_START,
        SCHEME,
        SCHEME_DATA,
        NO_SCHEME,
        RELATIVE_OR_AUTHORITY,
        RELATIVE,
        RELATIVE_SLASH,
        AUTHORITY_FIRST_SLASH,
        AUTHORITY_SECOND_SLASH,
        AUTHORITY_IGNORE_SLASHES,
        AUTHORITY,
        FILE_HOST,
        HOST,
        HOSTNAME,
        PORT,
        RELATIVE_PATH_START,
        RELATIVE_PATH,
        QUERY,
        FRAGMENT
    }

    public URL parse(final String urlString) throws MalformedURLException {
          return parse(null, urlString, null, null);
    }

    public URL parse(final URL base, final String urlString) throws MalformedURLException {
        return parse(base, urlString, null, null);
    }

    public URL parse(final String urlString, final URL url, final ParseURLState stateOverride) throws MalformedURLException {
        return parse(null, urlString, url, stateOverride);
    }

    // Based on http://src.chromium.org/viewvc/chrome/trunk/src/url/third_party/mozilla/url_parse.cc
    // http://url.spec.whatwg.org/#parsing
    //
    public URL parse(final URL base, final String urlString, final URL url, final ParseURLState stateOverride) throws MalformedURLException {

        if (urlString == null) {
            throw new NullPointerException("urlString");
        }

        if (urlString.isEmpty()) {
            throw new MalformedURLException("urlString is empty");
        }

        final char[] urlChars = urlString.toCharArray();
        final StringBuilder buffer = new StringBuilder(urlChars.length);

        //TODO: WHATWG URL: state override
        //TODO: WHATWG URL: encoding override
        //TODO: WHATWG URL: base
        //TODO: WHATWG URL: at_flag and brackets_flag

        String encodingOverride = "utf-8";
        String scheme = (url == null)? null : url.scheme();
        StringBuilder schemeData = (url == null)? new StringBuilder() : new StringBuilder(url.schemeData());
        String username = (url == null || url.username() == null)? null : url.username();
        String password = (url == null || url.password() == null)? null : url.password();
        Host host = (url == null)? null : url.host();
        Integer port = (url == null)? null : url.port();
        boolean relativeFlag = (url != null) && url.relativeFlag();
        boolean atFlag = false; // @-flag
        boolean bracketsFlag = false; // []-flag
        String[] path = (url == null)? new String[0] : url.path();
        StringBuilder query = (url == null || url.query() == null)? null : new StringBuilder(url.query());
        StringBuilder fragment = (url == null || url.fragment() == null)? null : new StringBuilder(url.fragment());

        int idx = 0;

        // Skip leading spaces
        // This is not defined in WHATWG URL spec, but it's sane to do it. Chromium does it too (on space, tabs and \n.
        // We do it on all Java whitespace.
        while (Character.isWhitespace(urlChars[idx])) {
            idx++;
        }

        // TODO: Skip traling spaces

        ParseURLState state = (stateOverride == null)? ParseURLState.SCHEME_START : stateOverride;

        // WHATWG URL 5.2.8: Keep running the following state machine by switching on state, increasing pointer by one
        //                   after each time it is run, as long as pointer does not point past the end of input.
        boolean terminate = false;
        while (!terminate) {

            if (idx > urlChars.length) {
                break;
            }

            final boolean isEOF = idx == urlChars.length;
            final char c = (isEOF)? 0x00 : urlChars[idx];

            //log.debug("STATE: {}", state.name());
            //log.debug("IDX: {} | C: {}", idx, c);

            switch (state) {

                case SCHEME_START: {

                    // WHATWG URL .8.1: If c is an ASCII alpha, append c, lowercased, to buffer, and set state to scheme state.
                    if (isASCIIAlpha(c)) {
                        buffer.append(Character.toLowerCase(c));
                        state = ParseURLState.SCHEME;
                    } else {
                        // WHATWG URL .8.2: Otherwise, if state override is not given, set state to no scheme state,
                        //                  and decrease pointer by one.
                        if (stateOverride == null) {
                            state = ParseURLState.NO_SCHEME;
                            idx--;
                        } else {
                            log.error("Parse error");
                            terminate = true;
                        }
                    }
                    break;
                }
                case SCHEME: {
                    // WHATWG URL .8.1: If c is an ASCII alphanumeric, "+", "-", or ".", append c, lowercased, to buffer.
                    if (isASCIIAlphanumeric(c) || c == '+' || c == '-' || c == '.') {
                        buffer.append(Character.toLowerCase(c));
                    }

                    // WHATWG URL .8.2: Otherwise, if c is ":", set url's scheme to buffer, buffer to the empty string,
                    //                  and then run these substeps:
                    else if (c == ':') {
                        scheme = buffer.toString();
                        buffer.setLength(0);

                        // WHATWG URL .1: If state override is given, terminate this algorithm.
                        if (stateOverride != null) {
                            terminate = true;
                            break;
                        }

                        // WHATWG URL .2: If url's scheme is a relative scheme, set url's relative flag.
                        relativeFlag = isRelativeScheme(scheme);

                        // WHATWG URL .3: If url's scheme is "file", set state to relative state.
                        if ("file".equals(scheme)) {
                            state = ParseURLState.RELATIVE;
                        }
                        // WHATWG URL .4: Otherwise, if url's relative flag is set, base is not null and base's
                        //                     scheme is equal to url's scheme, set state to relative or authority state.
                        else if (relativeFlag && base != null && base.scheme().equals(scheme)) {
                            state = ParseURLState.RELATIVE_OR_AUTHORITY;
                        }
                        // WHATWG URL .5: Otherwise, if url's relative flag is set, set state to authority first slash state.
                        else if (relativeFlag) {
                            state = ParseURLState.AUTHORITY_FIRST_SLASH;
                        }
                        // WHAT WG URL .6: Otherwise, set state to scheme data state.
                        else {
                            state = ParseURLState.SCHEME_DATA;
                        }

                    }

                    // WHATWG URL: Otherwise, if state override is not given, set buffer to the empty string,
                    //                  state to no scheme state, and start over (from the first code point in input).
                    else if (stateOverride == null) {
                        buffer.setLength(0);
                        state = ParseURLState.NO_SCHEME;
                        idx = 0;
                    }

                    // WHATWG URL: Otherwise, if c is the EOF code point, terminate this algorithm.
                    else if (isEOF) {
                        terminate = true;
                    }

                    // WHATWG URL: Otherwise, parse error, terminate this algorithm.
                    else {
                        throw new MalformedURLException("Schema could not be parsed");
                    }

                    break;
                }

                case SCHEME_DATA: {

                    // WHATWG URL: If c is "?", set url's query to the empty string and state to query state.
                    if (c == '?') {
                        query = new StringBuilder();
                        state = ParseURLState.QUERY;
                    }
                    // WHATWG URL: Otherwise, if c is "#", set url's fragment to the empty string and state to fragment state.
                    else if (c == '#') {
                        fragment = new StringBuilder();
                        state = ParseURLState.FRAGMENT;
                    }
                    // WHATWG URL: Otherwise, run these substeps:
                    else {

                        // WHATWG URL: If c is not the EOF code point, not a URL code point, and not "%", parse error.
                        if (!isEOF && c != '%' && !isURLCodePoint(c)) {
                            log.error("Parse error");
                        }

                        // WHATWG URL: If c is "%" and remaining does not start with two ASCII hex digits, parse error.
                        if (c == '%' && ( idx >= urlChars.length - 2 || !isASCIIHexDigit(urlChars[idx+1]) || !isASCIIHexDigit(urlChars[idx+2]))) {
                            log.error("Parse error");

                        }

                        // WHATWG URL: If c is none of EOF code point, U+0009, U+000A, and U+000D, utf-8 percent encode
                        //             c using the simple encode set, and append the result to url's scheme data.
                        if (!isEOF && c != 0x0009 && c != 0x000A && c != 0x000D) {
                            utf8PercentEncode(c, EncodeSet.SIMPLE, schemeData);
                        }

                    }

                    break;
                }

                case NO_SCHEME: {
                    if (base == null || !isRelativeScheme(base.scheme())) {
                        throw new MalformedURLException();
                    }
                    state = ParseURLState.RELATIVE;
                    idx--;
                    break;
                }

                case RELATIVE_OR_AUTHORITY: {
                    if (c == '/' && idx < urlChars.length - 1 && urlChars[idx+1] == '/') {
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        idx++;
                    } else {
                        throw new MalformedURLException();
                        //FIXME: state = RELATIVE; idx--;
                    }
                    break;
                }

                case RELATIVE: {
                    relativeFlag = true;

                    if (scheme == null || !"file".equals(scheme)) {
                        scheme = (base == null)? null : base.scheme();
                    }

                    if (isEOF) {
                        host = (base == null)? null : base.host();
                        port = (base == null)? null : base.port();
                        path = (base == null)? null : base.path();
                        query = (base == null)? null : new StringBuilder(base.query());
                    } else if (c == '/' || c == '\\') {
                        if (c == '\\') {
                            log.error("Parse error");
                        }
                        state = ParseURLState.RELATIVE_SLASH;
                    } else if (c == '?') {
                        host = (base == null)? null : base.host();
                        port = (base == null)? null : base.port();
                        path = (base == null)? null : base.path();
                        query = new StringBuilder();
                        state = ParseURLState.QUERY;
                    } else if (c == '#') {
                        host = (base == null)? null : base.host();
                        port = (base == null)? null : base.port();
                        path = (base == null)? null : base.path();
                        query = (base == null)? null : new StringBuilder(base.query());
                        fragment = new StringBuilder();
                        state = ParseURLState.FRAGMENT;
                    } else {
                        //TODO: file: and Windows drive quirk
                        state = ParseURLState.RELATIVE_PATH;
                        idx--;
                    }
                    break;
                }

                case RELATIVE_SLASH: {
                    if (c == '/' || c == '\\') {
                        if (c == '\\') {
                            //TODO: Log parse error
                        }
                        if ("file".equals(scheme)) {
                            state = ParseURLState.FILE_HOST;
                        } else {
                            state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        }
                    } else {
                        if (!"file".equals(scheme)) {
                            host = base.host();
                            port = base.port();
                        }
                        state = ParseURLState.RELATIVE_PATH;
                        idx--;
                    }
                    break;
                }

                case AUTHORITY_FIRST_SLASH: {
                    if (c == '/') {
                        state = ParseURLState.AUTHORITY_SECOND_SLASH;
                    } else {
                        log.error("Parse error");
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        idx--;
                    }
                    break;
                }

                case AUTHORITY_SECOND_SLASH: {
                    if (c == '/') {
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                    } else {
                        log.error("Parse error");
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        idx--;
                    }
                    break;
                }

                case AUTHORITY_IGNORE_SLASHES: {
                    if (c != '/' && c != '\\') {
                        state = ParseURLState.AUTHORITY;
                        idx--;
                    } else {
                        log.error("Parse error");
                    }
                    break;
                }

                case AUTHORITY: {
                    // If c is "@", run these substeps:
                    if (c == '@') {
                        if (atFlag) {
                            log.error("Parse error");
                            buffer.insert(0, "%40");
                        }
                        atFlag = true;

                        final StringBuilder usernameBuffer = new StringBuilder(buffer.length());
                        StringBuilder passwordBuffer = null;

                        for (int i = 0; i < buffer.length(); i++) {
                            final char otherCodePoint = buffer.charAt(i);
                            if (
                                    otherCodePoint == 0x0009 ||
                                    otherCodePoint == 0x000A ||
                                    otherCodePoint == 0x000D
                                ) {
                                log.error("Parse error");
                                continue;
                            }
                            if (!isURLCodePoint(otherCodePoint) && otherCodePoint != '%') {
                                log.error("Parse error");
                            }
                            if (otherCodePoint == '%') {
                                if (i + 2 >= buffer.length() ||
                                    !isASCIIHexDigit(buffer.charAt(i+1)) ||
                                    !isASCIIHexDigit(buffer.charAt(i + 2))) {
                                    log.error("Parse error");
                                }
                            }
                            if (otherCodePoint == ':' && passwordBuffer == null) {
                                passwordBuffer = new StringBuilder(buffer.length() - i);
                                continue;
                            }
                            if (passwordBuffer != null) {
                                utf8PercentEncode(otherCodePoint, EncodeSet.DEFAULT, passwordBuffer);
                            } else {
                                utf8PercentEncode(otherCodePoint, EncodeSet.DEFAULT, usernameBuffer);
                            }
                        }

                        username = usernameBuffer.toString();
                        if (passwordBuffer != null) {
                            password = passwordBuffer.toString();
                        }

                        buffer.setLength(0);

                    } else if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        idx -= buffer.length() + 1;
                        buffer.setLength(0);
                        state = ParseURLState.HOST;
                    } else {
                        buffer.append(c);
                    }
                    break;
                }

                case FILE_HOST: {

                    if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        idx--;
                        if (buffer.length() == 2 && isASCIIAlpha(buffer.charAt(0)) &&
                                (buffer.charAt(1) == ':' || buffer.charAt(1) == '|')) {
                            state = ParseURLState.RELATIVE_PATH;
                        } else if (buffer.length() == 0) {
                            state = ParseURLState.RELATIVE_PATH_START;
                        } else {
                            host = parseHost(buffer.toString());
                            if (host == null) {
                                log.error("Parse error - Invalid host");
                                return null;
                            }
                            buffer.setLength(0);
                            state = ParseURLState.RELATIVE_PATH_START;
                        }
                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        log.error("Parse error");
                    } else {
                        buffer.append(c);
                    }
                    break;
                }

                case HOST:
                case HOSTNAME: {
                    if (c == ':' && !bracketsFlag) {
                        host = parseHost(buffer.toString());
                        if (host == null) {
                            log.error("Parse error");
                            return null;
                        }
                        buffer.setLength(0);
                        state = ParseURLState.PORT;
                        if (stateOverride == ParseURLState.HOSTNAME) {
                            terminate = true;
                        }
                    } else if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        host = parseHost(buffer.toString());
                        if (host == null) {
                            log.error("Parse error");
                            return null;
                        }
                        buffer.setLength(0);
                        state = ParseURLState.RELATIVE_PATH_START;
                        if (stateOverride != null) {
                            terminate = true;
                        }
                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        log.error("Parse error");
                    } else {
                        if (c == '[') {
                            bracketsFlag = true;
                        } else if (c == ']') {
                            bracketsFlag = false;
                        }
                        buffer.append(c);
                    }
                    break;
                }

                case PORT: {
                    if (isASCIIDigit(c)) {
                        buffer.append(c);
                    } else if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        // Remove leading zeroes
                        while (buffer.charAt(0) == 0x0030 && buffer.length() > 1) {
                            buffer.deleteCharAt(0);
                        }
                        if (buffer.toString().equals(getDefaultPortForScheme(scheme))) {
                            buffer.setLength(0);
                        }
                        if (buffer.length() == 0) {
                            port = null;
                        } else {
                            port = Integer.valueOf(buffer.toString());
                        }
                        if (stateOverride != null) {
                            terminate = true;
                        } else {
                            buffer.setLength(0);
                            state = ParseURLState.RELATIVE_PATH_START;
                            idx--;
                        }
                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        log.error("Parse error");
                    } else {
                        buffer.append(c);
                    }
                    break;
                }

                case RELATIVE_PATH_START: {
                    if (c == '\\') {
                        log.error("Parse error");
                    } else {
                        state = ParseURLState.RELATIVE_PATH;
                        if (c != '/' && c != '\\') {
                            idx--;
                        }
                    }
                    break;
                }

                case RELATIVE_PATH: {
                    if (isEOF || c == '/' || c == '\\' || (stateOverride == null && (c == '?' || c == '#'))) {
                        if (c == '\\') {
                            log.error("Parse error");
                        }
                        final String lowerCasedBuffer = buffer.toString().toLowerCase(Locale.ENGLISH);
                        if ("%2e".equals(lowerCasedBuffer)) {
                            buffer.setLength(0);
                            buffer.append(".");
                        } else if (
                                ".%2e".equals(lowerCasedBuffer) ||
                                "%2e.".equals(lowerCasedBuffer) ||
                                "%2e%2e".equals(lowerCasedBuffer)
                                ) {
                            buffer.setLength(0);
                            buffer.append("..");
                        }
                        if ("..".equals(buffer.toString())) {
                            // Pop path
                            if (path.length > 0) {
                                path = Arrays.copyOf(path, path.length - 1);
                            }
                            if (c != '/' && c != '\\') {
                                path = Arrays.copyOf(path, path.length + 1);
                                path[path.length - 1] = "";
                            }
                            //FIX: It is not clear in the spec how a path should be pop'd

                        } else if (".".equals(buffer.toString()) && c != '/' && c != '\\') {
                            path = Arrays.copyOf(path, path.length + 1);
                            path[path.length - 1] = "";
                        } else if (!".".equals(buffer.toString())) {
                            if ("file".equals(scheme) && path.length == 0 &&
                                    buffer.length() == 2 &&
                                    isASCIIAlpha(buffer.charAt(0)) &&
                                    buffer.codePointAt(1) == '|') {
                                buffer.setCharAt(1, ':');
                            }
                            path = Arrays.copyOf(path, path.length + 1);
                            path[path.length - 1] = buffer.toString();
                        }
                        buffer.setLength(0);
                        if (c == '?') {
                            query = new StringBuilder();
                            state = ParseURLState.QUERY;
                        } else if (c == '#') {
                            fragment = new StringBuilder();
                            state = ParseURLState.FRAGMENT;
                        }

                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        log.error("Parse error");
                    } else {
                        if (!isURLCodePoint(c) && c != '%') {
                            log.error("Parse error");
                        }

                        if (c == '%') {
                            if (idx + 2 >= urlChars.length ||
                                    !isASCIIHexDigit(urlChars[idx+1]) ||
                                    !isASCIIHexDigit(urlChars[idx+2])) {
                                log.error("Parse error");
                            }
                        }

                        utf8PercentEncode(c, EncodeSet.DEFAULT, buffer);
                    }
                    break;
                }

                case QUERY: {
                    if (isEOF || (stateOverride == null && c == '#')) {
                        if (relativeFlag) {
                            encodingOverride = "utf-8";
                        }
                        final byte[] bytes = buffer.toString().getBytes();
                        for (final byte b : bytes) {
                            if (b < 0x21 || b > 0x7E || b == 0x22 || b == 0x23 || b == 0x3C || b == 0x3E || b == 0x60) {
                               percentEncode(b, query);
                            } else {
                                query.append((char)b);
                            }
                        }
                        buffer.setLength(0);
                        if (c == '#') {
                            fragment = new StringBuilder();
                            state = ParseURLState.FRAGMENT;
                        }
                    }  else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        log.error("Parse error");
                    } else {
                        if (!isURLCodePoint(c) && c != '%') {
                            log.error("Parse error");
                        }
                        if (c == '%') {
                            if (idx + 2 >= urlChars.length ||
                                    !isASCIIHexDigit(urlChars[idx+1]) ||
                                    !isASCIIHexDigit(urlChars[idx+2])) {
                                log.error("Parse error");
                            }
                        }
                        buffer.append(c);
                    }
                    break;
                }

                case FRAGMENT: {
                    if (isEOF) {
                        // Do nothing
                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        log.error("Parse error");
                    } else {
                        if (!isURLCodePoint(c) && c != '%') {
                            log.error("Parse error");
                        }
                        if (c == '%') {
                            if (idx + 2 >= urlChars.length ||
                                    !isASCIIHexDigit(urlChars[idx+1]) ||
                                    !isASCIIHexDigit(urlChars[idx+2])) {
                                log.error("Parse error");
                            }
                        }
                        utf8PercentEncode(c, EncodeSet.SIMPLE, fragment);
                    }
                    break;
                }

            }

            idx++;

        }

        return new URL(scheme, schemeData.toString(),
                username, password,
                host, port, path,
                (query == null)? null : query.toString(),
                (fragment == null)? null : fragment.toString(),
                relativeFlag);

    }

    public IPv6Address parseIPv6Address(final String ipString) {
        final short[] address = new short[8];
        int piecePointer = 0;
        Integer compressPointer = null;
        int idx = 0;
        final char[] input = ipString.toCharArray();
        boolean isEOF = idx == input.length;
        char c = (isEOF)? 0x00 : input[idx];

        if (!isEOF && c == ':') {
            if (idx + 1 >= input.length || input[idx+1] == ':') {
                log.error("Parse error - 1");
                return null;
            }
            idx += 2;
            piecePointer = 1;
            compressPointer = piecePointer;
        }

        boolean jumpToIpV4 = false;

        while (!isEOF) { // MAIN

            isEOF = idx == input.length;
            c = (isEOF)? 0x00 : input[idx];

            if (piecePointer == 8) {
                log.error("Parse error - 2");
                return null;
            }
            if (c == ':') {
                if (compressPointer != null) {
                    log.error("Parse error - 3");
                    return null;
                }
                idx++;
                piecePointer++;
                compressPointer = piecePointer;
                continue;
            }

            int value = 0;
            int length = 0;

            while (!isEOF && length < 4 && URLUtils.isASCIIHexDigit(c)) {
                value =  value * 0x10 + Integer.parseInt("" + c, 16);
                idx++;
                isEOF = idx == input.length;
                c = (isEOF)? 0x00 : input[idx];
                length++;
            }

            if (c == '.') {
                if (length == 0) {
                    log.error("Parse error - 4");
                    return null;
                }
                idx -= length;
                jumpToIpV4 = true;
                break;
            } else if (c == ':') {
                idx++;
                isEOF = idx == input.length;
                if (isEOF) {
                    log.error("Parse error - 5");
                    return null;
                }
            } else if (!isEOF) {
                log.error("Parse error - 6");
                return null;
            }

            address[piecePointer] = (short)value;
            piecePointer++;

        } // end while MAIN

        boolean jumpToFinale = false;

        // Step 7
        if (!jumpToIpV4 && isEOF) {
            jumpToFinale = true;
        }

        if (!jumpToFinale) {
            // Step 8 IPv4
            if (piecePointer > 6) {
                log.error("Parse error - 7");
                return null;
            }
        }

        // Step 9
        int dotsSeen = 0;

        if (!jumpToFinale) {
            // Step 10
            while (!isEOF) {
                // Step 10.1
                int value = 0;

                // Step 10.2
                if (!isASCIIDigit(c)) {
                    log.error("Parse error - 8");
                    return null;
                }

                // Step 10.3
                while (isASCIIDigit(c)) {
                    value = value * 0x10 + (c - 0x30);
                    idx++;
                    isEOF = idx == input.length;
                    c = (isEOF)? 0x00 : input[idx];
                }

                // Step 10.4
                if (value > 255) {
                    log.error("Parse error - 9");
                    return null;
                }

                // Step 10.5
                if (dotsSeen < 3 && c != '.') {
                    log.error("Parse error - 10");
                    return null;
                }

                // Step 10.6
                address[piecePointer] = (short) (address[piecePointer] * 0x100 + value);

                // Step 10.7
                if (dotsSeen == 0 || dotsSeen == 2) {
                    piecePointer++;
                }

                // Step 10.8
                idx++;
                isEOF = idx == input.length;
                c = (isEOF)? 0x00 : input[idx];

                // Step 10.9
                if (dotsSeen == 3 && !isEOF) {
                    log.error("Parse error - 11");
                    return null;
                }

                // Step 10.10
                dotsSeen++;
            }
        }

        // Step 11 Finale
        if (compressPointer != null) {
            // Step 11.1
            int swaps = piecePointer - compressPointer;
            // Step 11.2
            piecePointer = 7;
            // Step 11.3
            while (piecePointer != 0 && swaps != 0) {
                short swappedPiece = address[piecePointer];
                address[piecePointer] = address[compressPointer + swaps - 1];
                address[compressPointer + swaps - 1] = swappedPiece;
                piecePointer--;
                swaps--;
            }
        }
        // Step 12
        else if (piecePointer != 8) {
            log.error("Parse error");
            return null;
        }

        return new IPv6Address(address);
    }

    /**
     *
     * TODO: Generate IPv4Address instead of Domain when relevant?
     *
     * @param input
     * @return
     * @throws MalformedURLException
     */
    public Host parseHost(final String input) throws MalformedURLException {
        if (input.isEmpty()) {
            return null;
        }
        if (input.charAt(0) == '[') {
            if (input.charAt(input.length() - 1) != ']') {
                log.error("Parse error: Unmatched '['");
                return null;
            }
            parseIPv6Address(input.substring(1, input.length() - 1));
        }

        return Domain.parseDomain(input);
    }

}
