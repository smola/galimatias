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
import java.nio.charset.Charset;
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
 * - See  http://mxr.mozilla.org/mozilla-central/source/netwerk/base/src/nsURLParsers.cpp
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
 *       Add info about java.net.URI
 *       and Nutch https://github.com/apache/nutch/blob/2.1/src/java/org/apache/nutch/parse/OutlinkExtractor.java
 */
class URLParser {

    private static final Logger log = LoggerFactory.getLogger(URLParser.class);

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final URL base;
    private final String input;
    private final URL url;
    private final ParseURLState stateOverride;
    private URLParsingSettings settings;

    private int idx;
    private boolean isEOF;
    private int c;

    public URLParser(final String input) {
        this(null, input, null, null);
    }

    public URLParser(final URL base, final String input) {
        this(base, input, null, null);
    }

    public URLParser(final String input, final URL url, final ParseURLState stateOverride) {
        this(null, input, url, stateOverride);
    }

    public URLParser(final URL base, final String input, final URL url, final ParseURLState stateOverride) {
        this.base = base;
        this.input = input;
        this.url = url;
        this.stateOverride = stateOverride;
        this.settings = URLParsingSettings.create();
    }

    public URLParser settings(final URLParsingSettings settings) {
        this.settings = settings;
        return this;
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

    private void setIdx(final int i) {
        this.idx = i;
        this.isEOF = i >= input.length();
        this.c = (isEOF || idx < 0)? 0x00 : input.codePointAt(i);
    }

    private void incIdx() {
        final int charCount = Character.charCount(this.c);
        setIdx(this.idx + charCount);
    }

    private void decrIdx() {
        if (idx <= 0) {
            setIdx(idx - 1);
            return;
        }
        final int charCount = Character.charCount(this.input.codePointBefore(idx));
        setIdx(this.idx - charCount);
    }

    private char at(final int i) {
        if (i >= input.length()) {
            return 0x00;
        }
        return input.charAt(i);
    }

    // Based on http://src.chromium.org/viewvc/chrome/trunk/src/url/third_party/mozilla/url_parse.cc
    // http://url.spec.whatwg.org/#parsing
    //
    public URL parse() throws MalformedURLException {

        if (input == null) {
            throw new NullPointerException("urlString");
        }

        if (input.isEmpty()) {
            throw new MalformedURLException("urlString is empty");
        }

        final StringBuilder buffer = new StringBuilder(input.length()*2);

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

        setIdx(0);

        // Skip leading spaces
        // This is not defined in WHATWG URL spec, but it's sane to do it. Chromium does it too (on space, tabs and \n.
        // We do it on all Java whitespace.
        while (Character.isWhitespace(c)) {
            incIdx();
        }

        // TODO: Skip traling spaces

        ParseURLState state = (stateOverride == null)? ParseURLState.SCHEME_START : stateOverride;

        // WHATWG URL 5.2.8: Keep running the following state machine by switching on state, increasing pointer by one
        //                   after each time it is run, as long as pointer does not point past the end of input.
        boolean terminate = false;
        while (!terminate) {

            if (idx > input.length()) {
                break;
            }

            log.debug("STATE: {} | IDX: {} | C: {} | {}", state.name(), idx, c, new String(Character.toChars(c)));

            switch (state) {

                case SCHEME_START: {

                    // WHATWG URL .8.1: If c is an ASCII alpha, append c, lowercased, to buffer, and set state to scheme state.
                    if (isASCIIAlpha(c)) {
                        buffer.appendCodePoint(Character.toLowerCase(c));
                        state = ParseURLState.SCHEME;
                    } else {
                        // WHATWG URL .8.2: Otherwise, if state override is not given, set state to no scheme state,
                        //                  and decrease pointer by one.
                        if (stateOverride == null) {
                            state = ParseURLState.NO_SCHEME;
                            decrIdx();
                        } else {
                            throw new MalformedURLException("Scheme must start with alpha character.");
                        }
                    }
                    break;
                }
                case SCHEME: {
                    // WHATWG URL .8.1: If c is an ASCII alphanumeric, "+", "-", or ".", append c, lowercased, to buffer.
                    if (isASCIIAlphanumeric(c) || c == '+' || c == '-' || c == '.') {
                        buffer.appendCodePoint(Character.toLowerCase(c));
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

                        //XXX: This is a deviation from the URL Specification in its current form, in favour of
                        //     URIs as specified in RFC 3986. That is, if we find scheme://, we expect a hierarchical URI.
                        //     See https://www.w3.org/Bugs/Public/show_bug.cgi?id=24170
                        if (!relativeFlag) {
                            relativeFlag = input.regionMatches(idx + 1, "//", 0, 2);
                        }

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
                        idx = -1; // Note that it'll be incremented by 1 after the switch
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
                        if (c == '%' && (!isASCIIHexDigit(at(idx+1)) || !isASCIIHexDigit(at(idx+2)))) {
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
                        throw new MalformedURLException("Cannot build URL without scheme");
                    }
                    state = ParseURLState.RELATIVE;
                    idx--;
                    break;
                }

                case RELATIVE_OR_AUTHORITY: {
                    if (c == '/' && at(idx+1) == '/') {
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        idx++;
                    } else {
                        log.error("Parse error");
                        state = ParseURLState.RELATIVE;
                        idx--;
                    }
                    break;
                }

                case RELATIVE: {
                    relativeFlag = true;

                    if (!"file".equals(scheme)) {
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
                        if (!"file".equals(scheme) ||
                            !isASCIIAlpha(c) ||
                            (at(idx+1) != ':' && at(idx+1) != '|') ||
                            (idx + 1 == input.length() - 1) ||
                            (idx + 2 < input.length() &&
                                    at(idx+2) != '/' && at(idx+2) != '\\' && at(idx+2) != '?' && at(idx+2) != '#')
                                ) {

                            host = base.host();
                            port = base.port();
                            path = base.path();
                            // Pop path
                            if (path.length > 0) {
                                path = Arrays.copyOf(path, path.length - 1);
                            }
                        }
                        state = ParseURLState.RELATIVE_PATH;
                        idx--;
                    }
                    break;
                }

                case RELATIVE_SLASH: {
                    if (c == '/' || c == '\\') {
                        if (c == '\\') {
                            log.error("Parse error");
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
                            final char otherChar = buffer.charAt(i);
                            if (
                                    otherChar == 0x0009 ||
                                    otherChar == 0x000A ||
                                    otherChar == 0x000D
                                ) {
                                log.error("Parse error");
                                continue;
                            }
                            if (!isURLCodePoint(otherChar) && otherChar != '%') {
                                log.error("Parse error");
                            }
                            if (otherChar == '%') {
                                if (i + 2 >= buffer.length() || !isASCIIHexDigit(buffer.charAt(i+1)) || !isASCIIHexDigit(buffer.charAt(idx+2))) {
                                    log.error("Parse error");
                                }
                            }
                            if (otherChar == ':' && passwordBuffer == null) {
                                passwordBuffer = new StringBuilder(buffer.length() - i);
                                continue;
                            }
                            if (passwordBuffer != null) {
                                utf8PercentEncode(otherChar, EncodeSet.DEFAULT, passwordBuffer);
                            } else {
                                utf8PercentEncode(otherChar, EncodeSet.DEFAULT, usernameBuffer);
                            }
                        }

                        username = usernameBuffer.toString();
                        if (passwordBuffer != null) {
                            password = passwordBuffer.toString();
                        }

                        buffer.setLength(0);

                    } else if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        setIdx(idx - buffer.length() - 1);
                        buffer.setLength(0);
                        state = ParseURLState.HOST;
                    } else {
                        buffer.appendCodePoint(c);
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
                            host = Host.parseHost(buffer.toString());
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
                        buffer.appendCodePoint(c);
                    }
                    break;
                }

                case HOST:
                case HOSTNAME: {
                    if (c == ':' && !bracketsFlag) {
                        host = Host.parseHost(buffer.toString());
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
                        host = Host.parseHost(buffer.toString());
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
                        buffer.appendCodePoint(c);
                    }
                    break;
                }

                case PORT: {
                    if (isASCIIDigit(c)) {
                        buffer.appendCodePoint(c);
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
                        buffer.appendCodePoint(c);
                    }
                    break;
                }

                case RELATIVE_PATH_START: {
                    if (c == '\\') {
                        log.error("Parse error");
                    } else {
                        state = ParseURLState.RELATIVE_PATH;
                        if (c != '/' && c != '\\') {
                            decrIdx();
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
                            buffer.append('.');
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
                                    buffer.charAt(1) == '|') {
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

                        if (c == '%' && (!isASCIIHexDigit(at(idx+1)) || !isASCIIHexDigit(at(idx+2)))) {
                                log.error("Parse error");
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
                        final byte[] bytes = buffer.toString().getBytes(UTF_8);
                        for (int i = 0; i < bytes.length; i++) {
                            final byte b = bytes[i];
                            //XXX: Here we deviate from WHATWG URL and encode anything not valid in query as per RFC 3986.
                            //     Original condition for encoding was: (b < 0x21 || b > 0x7E || b == 0x22 || b == 0x23 || b == 0x3C || b == 0x3E || b == 0x60)
                            if (isQueryChar((char)b) ||
                                    (b == '%' && i + 2 < bytes.length &&
                                            isASCIIHexDigit((char)bytes[i+1]) &&
                                            isASCIIHexDigit((char)bytes[i+2]))) {
                                query.append((char) b);
                            } else {
                                percentEncode(b, query);
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
                        if (c == '%' && (!isASCIIHexDigit(at(idx+1)) || !isASCIIHexDigit(at(idx+2)))) {
                            log.error("Parse error");
                        }
                        buffer.appendCodePoint(c);
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
                        if (c == '%' && (!isASCIIHexDigit(at(idx+1)) || !isASCIIHexDigit(at(idx+2)))) {
                            log.error("Parse error");
                        }
                        utf8PercentEncode(c, EncodeSet.SIMPLE, fragment);
                    }
                    break;
                }

            }

            if (idx == -1) {
                setIdx(0);
            } else {
                incIdx();
            }

        }

        return new URL(scheme, schemeData.toString(),
                username, password,
                host, port, path,
                (query == null)? null : query.toString(),
                (fragment == null)? null : fragment.toString(),
                relativeFlag);

    }

    private static enum EncodeSet {
        SIMPLE,
        DEFAULT,
        PASSWORD,
        USERNAME
    }

    private boolean isUnreserved(final int c) {
        return isASCIIAlphanumeric(c) || c == '-' || c == '.' || c == '_' || c == '~'; //TODO: Older RFC
    }

    private boolean isSubdelim(final int c) {
        return c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')' || c == '*' || c == '+' || c == ',' || c == ';' || c == '=';
    }

    private boolean isPChar(final int c) {
        //XXX: "pct-encoded" is pchar, but we check for it before calling this.
        return isUnreserved(c) || isSubdelim(c) || c == ':' || c == '@';
    }

    private boolean isQueryChar(final int c) {
        switch (settings.standard()) {
            case WHATWG:
                return !(c < 0x21 || c > 0x7E || c == 0x22 || c == 0x23 || c == 0x3C || c == 0x3E || c == 0x60);
            case RFC_2396:
            case RFC_3986:
            default:
                return isPChar(c) || c == '/' || c == '?';
        }
    }

    private void utf8PercentEncode(final int c, final EncodeSet encodeSet, final StringBuilder buffer) {
        switch (encodeSet) {
            case SIMPLE:
                if (!isInSimpleEncodeSet(c)) {
                    buffer.appendCodePoint(c);
                    return;
                }
                break;
            case DEFAULT:
                if (!isInDefaultEncodeSet(c)) {
                    buffer.appendCodePoint(c);
                    return;
                }
                break;
            case PASSWORD:
                if (!isInPasswordEncodeSet(c)) {
                    buffer.appendCodePoint(c);
                    return;
                }
                break;
            case USERNAME:
                if (!isInUsernameEncodeSet(c)) {
                    buffer.appendCodePoint(c);
                    return;
                }
                break;
            default:
                throw new IllegalArgumentException("encodeSet");
        }


        final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
        for (final byte b : bytes) {
            percentEncode(b, buffer);
        }
    }

    private boolean isInSimpleEncodeSet(final int c) {
        return c < 0x0020 || c > 0x007E;
    }

    private boolean isInDefaultEncodeSet(final int c) {
        switch (settings.standard()) {
            case WHATWG:
                return isInSimpleEncodeSet(c) || c == '"' || c == '#' || c == '<' || c == '>' || c == '?' || c == '`';
            case RFC_3986: //TODO: Check
                return isInSimpleEncodeSet(c) || c == '"' || c == '#' || c == '<' || c == '>' || c == '?' || c == '`';
            case RFC_2396:
            default:
                return isInSimpleEncodeSet(c) || c == '"' || c == '#' || c == '<' || c == '>' || c == '?' || c == '`'
                        || c == '|' || c == '[' || c == ']' || c == '{' || c == '}' || c == '^';

        }
    }

    private boolean isInPasswordEncodeSet(final int c) {
        return isInDefaultEncodeSet(c) || c == '/' || c == '@' || c == '\\';
    }

    private boolean isInUsernameEncodeSet(final int c) {
        return isInPasswordEncodeSet(c) || c == ':';
    }


}
