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


import java.util.ArrayList;
import java.util.List;

import static io.mola.galimatias.URLUtils.*;
import static java.lang.Character.*;

final class URLParser {

    private URLParsingSettings settings;

    private static final ThreadLocal<URLParser> instance = new ThreadLocal<URLParser>() {
        @Override
        protected URLParser initialValue() {
            return new URLParser();
        }
    };

    String scheme;
    private final StringBuilder schemeData;
    private final StringBuilder username;
    private final StringBuilder password;
    private boolean hasPassword;
    Host host;
    int port;
    boolean relativeFlag;
    private List<String> pathSegments;
    private final StringBuilder query;
    private boolean hasQuery;
    private final StringBuilder fragment;
    private boolean hasFragment;

    private final StringBuilder buffer;

    private URLParser() {
        schemeData = new StringBuilder();
        pathSegments = new ArrayList<String>();
        username = new StringBuilder();
        password = new StringBuilder();
        query = new StringBuilder();
        fragment = new StringBuilder();
        buffer = new StringBuilder();
    }

    public static URLParser getInstance() {
        return instance.get();
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
        PORT,
        RELATIVE_PATH_START,
        RELATIVE_PATH,
        QUERY,
        FRAGMENT
    }

    public URL parse(final String input) throws GalimatiasParseException {
        return parse(null, input, null, null);
    }

    public URL parse(final URL base, final String input) throws GalimatiasParseException {
        return parse(base, input, null, null);
    }

    public URL parse (final String input, final URL url, final ParseURLState stateOverride) throws GalimatiasParseException {
        return parse(null, input, url, stateOverride);
    }

    public URL parse(final URL base, final String input, final URL url, final ParseURLState stateOverride) throws GalimatiasParseException {
        return parse(base, input, url, stateOverride, URLParsingSettings.create());
    }

    /**
     *
     * http://url.spec.whatwg.org/#parsing
     *
     * @param base
     * @param input
     * @param url
     * @param stateOverride
     * @return
     * @throws GalimatiasParseException
     */
    public URL parse(final URL base, final String input, final URL url, final ParseURLState stateOverride, final URLParsingSettings settings) throws GalimatiasParseException {

        if (input == null) {
            throw new NullPointerException("null input");
        }

        this.settings = settings;
        final CodePointIterator c = new CodePointIterator(input);
        buffer.setLength(0);

        setURL(url);

        // Pre-update steps
        if (stateOverride == ParseURLState.RELATIVE_PATH_START) {
            pathSegments.clear();
        } else if (stateOverride == ParseURLState.QUERY) {
            query.setLength(0);
            hasQuery = true;
        } else if (stateOverride == ParseURLState.FRAGMENT) {
            fragment.setLength(0);
            hasFragment = true;
        }

        if (url == null) {
            c.trim();
        }

        ParseURLState state = (stateOverride == null)? ParseURLState.SCHEME_START : stateOverride;

        boolean atFlag = false;
        boolean bracketsFlag = false;

        boolean terminate = false;
        do {
            //log.trace("STATE: {} | IDX: {} | C: {} | {}", state.name(), idx, c, new String(Character.toChars(c)));
            switch (state) {

                case SCHEME_START: {

                    // WHATWG URL .8.1: If c is an ASCII alpha, append c, lowercased, to buffer, and set state to scheme state.
                    if (isASCIIAlpha(c.cp())) {
                        buffer.appendCodePoint(toLowerCase(c.cp()));
                        state = ParseURLState.SCHEME;
                    }
                    // WHATWG URL .8.2: Otherwise, if state override is not given, set state to no scheme state,
                    //                  and decrease pointer by one.
                    else if (stateOverride == null) {
                        state = ParseURLState.NO_SCHEME;
                        c.prev();
                    } else {
                        handleFatalError(c.idx(), "Scheme must start with alpha character.");
                    }
                    break;
                }
                case SCHEME: {
                    // WHATWG URL .8.1: If c is an ASCII alphanumeric, "+", "-", or ".", append c, lowercased, to buffer.
                    if (isASCIIAlphanumeric(c.cp()) || c.is('+') || c.is('-') || c.is('.')) {
                        buffer.appendCodePoint(toLowerCase(c.cp()));
                    }

                    // WHATWG URL .8.2: Otherwise, if c is ":", set url's scheme to buffer, buffer to the empty string,
                    //                  and then run these substeps:
                    else if (c.is(':')) {
                        scheme = buffer.toString();
                        buffer.setLength(0);

                        // WHATWG URL .1: If state override is given, terminate this algorithm.
                        if (stateOverride != null) {
                            terminate = true;
                            break;
                        }

                        // WHATWG URL .2: If url's scheme is a relative scheme, set url's relative flag.
                        if (isRelativeScheme(scheme)) {
                            relativeFlag = true;
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

                    // WHATWG URL: 3. Otherwise, if state override is not given, set buffer to the empty string,
                    //                  state to no scheme state, and start over (from the first code point in input).
                    else if (stateOverride == null) {
                        buffer.setLength(0);
                        state = ParseURLState.NO_SCHEME;
                        c.reset();
                    }

                    // WHATWG URL: 4. Otherwise, if c is the EOF code point, terminate this algorithm.
                    else if (c.isEOF()) {
                        terminate = true;
                    }

                    // WHATWG URL: Otherwise, parse error, terminate this algorithm.
                    else {
                        handleFatalIllegalCharacterError(c.idx(), "Illegal character in scheme");
                    }

                    break;
                }

                case SCHEME_DATA: {

                    // WHATWG URL: 1. If c is "?", set url's query to the empty string and state to query state.
                    if (c.is('?')) {
                        query.setLength(0);
                        hasQuery = true;
                        state = ParseURLState.QUERY;
                    }
                    // WHATWG URL: 2.. Otherwise, if c is "#", set url's fragment to the empty string and state to fragment state.
                    else if (c.is('#')) {
                        fragment.setLength(0);
                        hasFragment = true;
                        state = ParseURLState.FRAGMENT;
                    }
                    // WHATWG URL: 3. Otherwise, run these substeps:
                    else {

                        // WHATWG URL: .1 If c is not the EOF code point, not a URL code point, and not "%", parse error.
                        if (!c.isEOF() && !c.is('%') && !isURLCodePoint(c.cp())) {
                            handleIllegalCharacterError(c.idx(), "Illegal character in scheme data: not a URL code point");
                        }

                        // WHATWG URL: .2 If c is "%" and remaining does not start with two ASCII hex digits, parse error.
                        if (c.is('%')) {
                            if (!isASCIIHexDigit(c.atOffset(1)) || !isASCIIHexDigit(c.atOffset(2))) {
                                handleInvalidPercentEncodingError(c.idx());
                            } else {
                                schemeData.append('%')
                                        .append(toUpperCase(c.atOffset(1)))
                                        .append(toUpperCase(c.atOffset(2))); //TODO: Optionally convert to uppercase
                                c.setIdx(c.idx() + 2);
                                break;
                            }
                        }

                        // WHATWG URL: .3 If c is none of EOF code point, U+0009, U+000A, and U+000D, utf-8 percent encode
                        //             c using the simple encode set, and append the result to url's scheme data.
                        if (!c.isEOF() && !c.is(0x0009) && !c.is(0x000A) && !c.is(0x000D)) {
                            utf8PercentEncode(c.cp(), EncodeSet.SIMPLE, schemeData);
                        }
                        //TODO: Shouldn't the else produce a warning?

                    }

                    break;
                }

                case NO_SCHEME: {
                    if (base == null || !isRelativeScheme(base.scheme())) {
                        handleFatalMissingSchemeError(c.idx());
                    }
                    state = ParseURLState.RELATIVE;
                    c.prev();
                    break;
                }

                case RELATIVE_OR_AUTHORITY: {
                    if (c.is('/') && c.atOffset(1) == '/') {
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        c.next();
                    } else {
                        handleError(c.idx(), "Relative scheme (" + scheme + ") is not followed by \"://\"");
                        state = ParseURLState.RELATIVE;
                        c.prev();
                    }
                    break;
                }

                case RELATIVE: {
                    relativeFlag = true;

                    if (!"file".equals(scheme)) {
                        scheme = base.scheme();
                    }

                    if (c.isEOF()) {
                        if (base != null) {
                            host = base.host();
                            port = (base.port() == base.defaultPort())? -1 : base.port();
                            pathSegments.clear();
                            pathSegments.addAll(base.pathSegments());
                            query.setLength(0);
                            hasQuery = base.query() != null;
                            if (hasQuery) {
                                query.append(base.query());
                            }
                        } else {
                            host = null;
                            port = -1;
                            pathSegments.clear();
                            query.setLength(0);
                            hasQuery = false;
                        }
                    } else if (c.is('/') || c.is('\\')) {
                        if (c.is('\\')) {
                            handleBackslashAsDelimiterError(c.idx());
                        }
                        state = ParseURLState.RELATIVE_SLASH;
                    } else if (c.is('?')) {
                        if (base != null) {
                            host = base.host();
                            port = (base.port() == base.defaultPort())? -1 : base.port();
                            pathSegments.clear();
                            pathSegments.addAll(base.pathSegments());
                        } else {
                            host = null;
                            port = -1;
                            pathSegments.clear();
                        }
                        query.setLength(0);
                        hasQuery = true;
                        state = ParseURLState.QUERY;
                    } else if (c.is('#')) {
                        if (base != null) {
                            host = base.host();
                            port = (base.port() == base.defaultPort())? -1 : base.port();
                            pathSegments.clear();
                            pathSegments.addAll(base.pathSegments());
                            query.setLength(0);
                            hasQuery = base.query() != null;
                            if (hasQuery) {
                                query.append(base.query());
                            }
                        } else {
                            host = null;
                            port = -1;
                            pathSegments.clear();
                            query.setLength(0);
                            hasQuery = false;
                        }
                        fragment.setLength(0);
                        hasFragment = true;
                        state = ParseURLState.FRAGMENT;
                    } else {
                        if (!"file".equals(scheme) ||
                            !isASCIIAlpha(c.cp()) ||
                            (c.atOffset(1) != ':' && c.atOffset(1) != '|') ||
                                //TODO: It seems there is bug with URLs getting up to here with just a remaining CP (with surrogate)
                            (c.idx() + 1 == c.endIdx() - 1) ||
                            (c.idx() + 2 < c.endIdx() &&
                                    c.at(c.idx() + 2) != '/' && c.at(c.idx() + 2) != '\\' && c.at(c.idx() + 2) != '?' && c.at(c.idx()+2) != '#')
                                ) {

                            if (base != null) {
                                host = base.host();
                                port = (base.port() == base.defaultPort())? -1 : base.port();
                                pathSegments.clear();
                                pathSegments.addAll(base.pathSegments());
                            } else {
                                host = null;
                                port = -1;
                                pathSegments.clear();
                            }
                            // Pop path
                            if (!pathSegments.isEmpty()) {
                                pathSegments.remove(pathSegments.size() - 1);
                            }
                        }
                        state = ParseURLState.RELATIVE_PATH;
                        //TODO: idx-- instead of decrIdx
                        c.prev();
                    }
                    break;
                }

                case RELATIVE_SLASH: {
                    if (c.is('/') || c.is('\\')) {
                        if (c.is('\\')) {
                            handleBackslashAsDelimiterError(c.idx());
                        }
                        if ("file".equals(scheme)) {
                            state = ParseURLState.FILE_HOST;
                        } else {
                            state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        }
                    } else {
                        if (!"file".equals(scheme)) {
                            if (base != null) {
                                host = base.host();
                                port = (base.port() == base.defaultPort())? -1 : base.port();
                            } else {
                                host = null;
                                port = -1;
                            }
                        }
                        state = ParseURLState.RELATIVE_PATH;
                        //TODO: idx-- instead of decrIdx
                        c.prev();
                    }
                    break;
                }

                case AUTHORITY_FIRST_SLASH: {
                    if (c.cp() == '/') {
                        state = ParseURLState.AUTHORITY_SECOND_SLASH;
                    } else {
                        handleError(c.idx(), "Expected a slash (\"/\")");
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        c.prev();
                    }
                    break;
                }

                case AUTHORITY_SECOND_SLASH: {
                    if (c.is('/')) {
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                    } else {
                        handleError(c.idx(), "Expected a slash (\"/\")");
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        c.prev();
                    }
                    break;
                }

                case AUTHORITY_IGNORE_SLASHES: {
                    if (!c.is('/') && !c.is('\\')) {
                        state = ParseURLState.AUTHORITY;
                        c.prev();
                    } else {
                        handleError(c.idx(), "Unexpected slash or backslash");
                    }
                    break;
                }

                case AUTHORITY: {
                    // 1. If c is "@", run these substeps:
                    if (c.is('@')) {

                        if (atFlag) {
                            handleError(c.idx(), "User or password contains an at symbol (\"@\") not percent-encoded");
                            buffer.insert(0, "%40");
                        }
                        atFlag = true;

                        //TODO: There was probably an undetected codepoint counting bug here
                        for (final CodePointIterator otherC = new CodePointIterator(buffer);
                                !otherC.isEOF(); otherC.next()) {

                            if (otherC.is(0x0009) || otherC.is(0x000A) || otherC.is(0x000D)) {
                                //FIXME: c.idx() is not the actual index here!
                                handleIllegalWhitespaceError(c.idx());
                                continue;
                            }
                            if (!isURLCodePoint(otherC.cp()) && !otherC.is('%')) {
                                //FIXME: c.idx() is not the actual index here!
                                handleIllegalCharacterError(c.idx(), "Illegal character in user or password: not a URL code point");
                            }
                            if (otherC.is('%')) {
                                if (!isASCIIHexDigit(otherC.atOffset(1)) || !isASCIIHexDigit(otherC.atOffset(2))) {
                                    //FIXME: c.idx() is not the actual index here!
                                    handleInvalidPercentEncodingError(c.idx());
                                } else if (isASCIIHexDigit(otherC.atOffset(1)) && isASCIIHexDigit(otherC.atOffset(2))) {
                                    buffer.setCharAt(otherC.idx() + 1, toUpperCase(otherC.atOffset(1)));
                                    buffer.setCharAt(otherC.idx() + 2, toUpperCase(otherC.atOffset(2))); //TODO: Optionally convert to uppercase
                                }
                            }
                            if (otherC.is(':') && !hasPassword) {
                                password.setLength(0);
                                hasPassword = true;
                                continue;
                            }
                            if (hasPassword) {
                                utf8PercentEncode(otherC.cp(), EncodeSet.DEFAULT, password);
                            } else {
                                utf8PercentEncode(otherC.cp(), EncodeSet.DEFAULT, username);
                            }
                        }

                        buffer.setLength(0);

                    } else if (c.isEOF() || c.is('/') || c.is('\\') || c.is('?') || c.is('#')) {
                        c.setIdx(c.idx() - buffer.length() - 1);
                        buffer.setLength(0);
                        state = ParseURLState.HOST;
                    } else {
                        buffer.appendCodePoint(c.cp());
                    }
                    break;
                }

                case FILE_HOST: {

                    if (c.isEOF() || c.is('/') || c.is('\\') || c.is('?') || c.is('#')) {
                        //TODO: idx-- and no decrIdx
                        c.prev();
                        if (buffer.length() == 2 && isASCIIAlpha(buffer.charAt(0)) &&
                                (buffer.charAt(1) == ':' || buffer.charAt(1) == '|')) {
                            state = ParseURLState.RELATIVE_PATH;
                        } else if (buffer.length() == 0) {
                            state = ParseURLState.RELATIVE_PATH_START;
                        } else {
                            try {
                                host = Host.parseHost(buffer.toString());
                            } catch (GalimatiasParseException ex) {
                                handleFatalInvalidHostError(c.idx(), ex);
                            }
                            buffer.setLength(0);
                            state = ParseURLState.RELATIVE_PATH_START;
                        }
                    } else if (c.is(0x0009) || c.is(0x000A) || c.is(0x000D)) {
                        handleIllegalWhitespaceError(c.idx());
                    } else {
                        buffer.appendCodePoint(c.cp());
                    }
                    break;
                }

                case HOST: {
                    if (c.is(':') && !bracketsFlag) {
                        try {
                            host = Host.parseHost(buffer.toString());
                        } catch (GalimatiasParseException ex) {
                            handleFatalInvalidHostError(c.idx(), ex);
                        }
                        buffer.setLength(0);
                        state = ParseURLState.PORT;
                        if (stateOverride == ParseURLState.HOST) {
                            terminate = true;
                        }
                    } else if (c.isEOF() || c.is('/') || c.is('\\') || c.is('?') || c.is('#')) {
                        c.prev();
                        try {
                            host = Host.parseHost(buffer.toString());
                        } catch (GalimatiasParseException ex) {
                            handleFatalInvalidHostError(c.idx(), ex);
                        }
                        buffer.setLength(0);
                        state = ParseURLState.RELATIVE_PATH_START;
                        if (stateOverride != null) {
                            terminate = true;
                        }
                    } else if (c.is(0x0009) || c.is(0x000A) || c.is(0x000D)) {
                        handleIllegalWhitespaceError(c.idx());
                    } else {
                        if (c.is('[')) {
                            bracketsFlag = true;
                        } else if (c.is(']')) {
                            bracketsFlag = false;
                        }
                        buffer.appendCodePoint(c.cp());
                    }
                    break;
                }

                case PORT: {
                    if (isASCIIDigit(c.cp())) {
                        buffer.appendCodePoint(c.cp());
                    } else if (c.isEOF() || c.is('/') || c.is('\\') || c.is('?') || c.is('#')) {
                        // Remove leading zeroes
                        while (buffer.length() > 1 && buffer.charAt(0) == 0x0030) {
                            buffer.deleteCharAt(0);
                        }
                        //XXX: This is redundant with URL constructor
                        if (buffer.toString().equals(getDefaultPortForScheme(scheme))) {
                            buffer.setLength(0);
                        }
                        if (buffer.length() == 0) {
                            port = -1;
                        } else {
                            port = Integer.parseInt(buffer.toString());
                        }
                        if (stateOverride != null) {
                            terminate = true;
                            break;
                        }
                        buffer.setLength(0);
                        state = ParseURLState.RELATIVE_PATH_START;
                        //TODO: idx-- and no decrIdx
                        c.prev();
                    } else if (c.is(0x0009) || c.is(0x000A) || c.is(0x000D)) {
                        handleIllegalWhitespaceError(c.idx());
                    } else {
                        handleFatalIllegalCharacterError(c.idx(), "Illegal character in port");
                    }
                    break;
                }

                case RELATIVE_PATH_START: {
                    if (c.is('\\')) {
                        handleBackslashAsDelimiterError(c.idx());
                    }
                    state = ParseURLState.RELATIVE_PATH;
                    if (!c.is('/') && !c.is('\\')) {
                        c.prev();
                    }
                    break;
                }

                case RELATIVE_PATH: {
                    if (c.isEOF() || c.is('/') || c.is('\\') || (stateOverride == null && (c.is('?') || c.is('#')))) {
                        if (c.is('\\')) {
                            handleBackslashAsDelimiterError(c.idx());
                        }
                        String bufferString = buffer.toString();
                        if ("%2e".equalsIgnoreCase(bufferString)) {
                            buffer.setLength(0);
                            buffer.append('.');
                            bufferString = ".";
                        } else if (
                                ".%2e".equalsIgnoreCase(bufferString) ||
                                "%2e.".equalsIgnoreCase(bufferString) ||
                                "%2e%2e".equalsIgnoreCase(bufferString)
                                ) {
                            buffer.setLength(0);
                            buffer.append("..");
                            bufferString = "..";
                        }
                        if ("..".equals(bufferString)) {
                            // Pop path
                            if (!pathSegments.isEmpty()) {
                                pathSegments.remove(pathSegments.size() - 1);
                            }
                            if (!c.is('/') && !c.is('\\')) {
                                pathSegments.add("");
                            }

                        } else if (".".equals(bufferString) && !c.is('/') && !c.is('\\')) {
                            pathSegments.add("");
                        } else if (!".".equals(bufferString)) {
                            if ("file".equals(scheme) && pathSegments.isEmpty() &&
                                    buffer.length() == 2 &&
                                    isASCIIAlpha(buffer.charAt(0)) &&
                                    buffer.charAt(1) == '|') {
                                buffer.setCharAt(1, ':');
                            }
                            pathSegments.add(buffer.toString());
                        }
                        buffer.setLength(0);
                        if (c.is('?')) {
                            query.setLength(0);
                            hasQuery = true;
                            state = ParseURLState.QUERY;
                        } else if (c.is('#')) {
                            fragment.setLength(0);
                            hasFragment = true;
                            state = ParseURLState.FRAGMENT;
                        }

                    } else if (c.is(0x0009) || c.is(0x000A) || c.is(0x000D)) {
                        handleIllegalWhitespaceError(c.idx());
                    } else {
                        if (!isURLCodePoint(c.cp()) && !c.is('%')) {
                            handleIllegalCharacterError(c.idx(), "Illegal character in path segment: not a URL code point");
                        }

                        if (c.is('%')) {
                            if (!isASCIIHexDigit(c.atOffset(1)) || !isASCIIHexDigit(c.atOffset(2))) {
                                handleInvalidPercentEncodingError(c.idx());
                            } else {
                                buffer.append('%')
                                        .append(toUpperCase(c.atOffset(1)))
                                        .append(toUpperCase(c.atOffset(2))); //TODO: Optionally convert to Uppercase
                                c.setIdx(c.idx() + 2);
                                break;
                            }
                        }

                        utf8PercentEncode(c.cp(), EncodeSet.DEFAULT, buffer);
                    }
                    break;
                }

                case QUERY: {

                    if (c.isEOF() || (stateOverride == null && c.is('#'))) {
                        final byte[] bytes = buffer.toString().getBytes(UTF_8);
                        for (final byte b : bytes) {
                            if (b < 0x21 || b > 0x7E || b == 0x22 || b == 0x23 || b == 0x3C || b == 0x3E || b == 0x60) {
                                percentEncode(b, query);
                            } else {
                                query.append((char) b);
                            }
                        }
                        buffer.setLength(0);
                        if (c.is('#')) {
                            fragment.setLength(0);
                            hasFragment = true;
                            state = ParseURLState.FRAGMENT;
                        }
                    }  else if (c.is(0x0009) || c.is(0x000A) || c.is(0x000D)) {
                        handleIllegalWhitespaceError(c.idx());
                    } else {
                        if (!isURLCodePoint(c.cp()) && !c.is('%')) {
                            handleIllegalCharacterError(c.idx(), "Illegal character in query: not a URL code point");
                        }
                        if (c.is('%')) {
                            if (!isASCIIHexDigit(c.atOffset(1)) || !isASCIIHexDigit(c.atOffset(2))) {
                                handleInvalidPercentEncodingError(c.idx());
                            } else {
                                buffer.append('%')
                                        .append(toUpperCase(c.atOffset(1)))
                                        .append(toUpperCase(c.atOffset(2))); //TODO: Optionally convert to upper
                                c.setIdx(c.idx() + 2);
                                break;
                            }
                        }
                        buffer.appendCodePoint(c.cp());
                    }
                    break;
                }

                case FRAGMENT: {

                    if (c.isEOF()) {
                        // Do nothing
                    } else if (c.is(0x0009) || c.is(0x000A) || c.is(0x000D)) {
                        handleIllegalWhitespaceError(c.idx());
                    } else {
                        if (!isURLCodePoint(c.cp()) && !c.is('%')) {
                            handleIllegalCharacterError(c.idx(), "Illegal character in path segment: not a URL code point");
                        }
                        if (c.is('%')) {
                            if (!isASCIIHexDigit(c.atOffset(1)) || !isASCIIHexDigit(c.atOffset(2))) {
                                handleInvalidPercentEncodingError(c.idx());
                            } else {
                                fragment.append('%')
                                        .append(toUpperCase(c.atOffset(1)))
                                        .append(toUpperCase(c.atOffset(2))); //TODO: Optionally convert to upper
                                c.setIdx(c.idx() + 2);
                                break;
                            }
                        }

                        utf8PercentEncode(c.cp(), EncodeSet.SIMPLE, fragment);

                    }
                    break;
                }

            }

            if (c.isEOF()) {
                break;
            }

            c.next();

        } while (!terminate);

        return new URL(scheme, schemeData.toString(),
                username.toString(), hasPassword? password.toString() : null,
                host, port,
                pathSegments,
                hasQuery? query.toString() : null,
                hasFragment? fragment.toString() : null,
                relativeFlag);

    }

    public String parseUsername(final String input) {
        buffer.setLength(0);
        final CodePointIterator c = new CodePointIterator(input);
        while (!c.isEOF()) {
            utf8PercentEncode(c.cp(), EncodeSet.USERNAME, buffer);
            c.next();
        }
        return buffer.toString();
    }

    public String parsePassword(final String input) {
        buffer.setLength(0);
        final CodePointIterator c = new CodePointIterator(input);
        while (!c.isEOF()) {
            utf8PercentEncode(c.cp(), EncodeSet.PASSWORD, buffer);
            c.next();
        }
        return buffer.toString();
    }

    private static enum EncodeSet {
        SIMPLE,
        DEFAULT,
        PASSWORD,
        USERNAME
    }

    private void utf8PercentEncode(final int c, final EncodeSet encodeSet, final StringBuilder buffer) {
        if (encodeSet != null) {
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
            }
        }
        final byte[] bytes = new String(toChars(c)).getBytes(UTF_8);
        for (final byte b : bytes) {
            percentEncode(b, buffer);
        }
    }

    private boolean isInSimpleEncodeSet(final int c) {
        return c < 0x0020 || c > 0x007E;
    }

    private boolean isInDefaultEncodeSet(final int c) {
        return isInSimpleEncodeSet(c) || c == ' ' || c == '"' || c == '#' || c == '<' || c == '>' || c == '?' || c == '`';
    }

    private boolean isInPasswordEncodeSet(final int c) {
        return isInDefaultEncodeSet(c) || c == '/' || c == '@' || c == '\\';
    }

    private boolean isInUsernameEncodeSet(final int c) {
        return isInPasswordEncodeSet(c) || c == ':';
    }

    private void setURL(final URL url) {
        schemeData.setLength(0);
        username.setLength(0);
        password.setLength(0);
        query.setLength(0);
        relativeFlag = false;

        if (url == null) {
            scheme = "";
            hasPassword = false;
            host = null;
            port = -1;
            pathSegments.clear();
            hasQuery = false;
            hasFragment = false;
        } else {
            scheme = url.scheme();
            schemeData.append(url.schemeData());
            username.append(url.username());
            password.append(url.password());
            host = url.host();
            port = url.port();
            relativeFlag = url.isHierarchical();
            pathSegments.clear();
            pathSegments.addAll(url.pathSegments());
            hasQuery = url.query() != null;
            if (hasQuery) {
                query.append(url.query());
            }
            hasFragment = url.fragment() != null;
            if (hasFragment) {
                fragment.append(url.fragment());
            }
        }
    }

    private void handleError(final GalimatiasParseException parseException) throws GalimatiasParseException {
        this.settings.errorHandler().error(parseException);
    }

    private void handleError(final int idx, final String message) throws GalimatiasParseException {
        handleError(new GalimatiasParseException(message, idx));
    }

    private void handleFatalError(GalimatiasParseException parseException) throws GalimatiasParseException {
        this.settings.errorHandler().fatalError(parseException);
        throw parseException;
    }

    private void handleFatalError(final int idx, final String message) throws GalimatiasParseException {
        handleFatalError(new GalimatiasParseException(message, idx));
    }

    private void handleInvalidPercentEncodingError(final int idx) throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Percentage (\"%\") is not followed by two hexadecimal digits")
                .withParseIssue(ParseIssue.INVALID_PERCENT_ENCODING)
                .withPosition(idx)
                .build());
    }

    private void handleBackslashAsDelimiterError(final int idx) throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Backslash (\"\\\") used as path segment delimiter")
                .withParseIssue(ParseIssue.BACKSLASH_AS_DELIMITER)
                .withPosition(idx)
                .build());
    }

    private void handleIllegalWhitespaceError(final int idx) throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Tab, new line or carriage return found")
                .withParseIssue(ParseIssue.ILLEGAL_WHITESPACE)
                .withPosition(idx)
                .build());
    }

    private void handleIllegalCharacterError(final int idx, final String message) throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage(message)
                .withParseIssue(ParseIssue.ILLEGAL_CHARACTER)
                .withPosition(idx)
                .build());
    }

    private void handleFatalMissingSchemeError(final int idx) throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage("Missing scheme")
                .withPosition(idx)
                .withParseIssue(ParseIssue.MISSING_SCHEME)
                .build());
    }

    private void handleFatalIllegalCharacterError(final int idx, String message) throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage(message)
                .withParseIssue(ParseIssue.ILLEGAL_CHARACTER)
                .withPosition(idx)
                .build());
    }

    private void handleFatalInvalidHostError(final int idx, final Exception exception) throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage("Invalid host: " + exception.getMessage())
                .withParseIssue(ParseIssue.INVALID_HOST)
                .withPosition(idx)
                .withCause(exception)
                .build());
    }


}
