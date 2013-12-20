package io.mola.galimatias;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

/**
 *  http://url.spec.whatwg.org/
 */
public class URLParser {

    public URLParser() {

    }

    /**
     * Parse URL states as defined by WHATWG URL spec.
     *
     * http://url.spec.whatwg.org/#scheme-start-state
     */
    private static enum ParseUrlState {
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

    // Based on http://src.chromium.org/viewvc/chrome/trunk/src/url/third_party/mozilla/url_parse.cc
    // http://url.spec.whatwg.org/#parsing
    //
    public URL parse(final String urlString) throws MalformedURLException {

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

        URL base = null;
        ParseUrlState stateOverride = null;
        String scheme = null;
        String host = null;
        Integer port = null;
        boolean relativeSchemeFlag = false;
        boolean relativeFlag = false;
        String path = null;
        String query = null;
        String fragment = null;

        int idx = 0;

        // Skip leading spaces
        // This is not defined in WHATWG URL spec, but it's sane to do it. Chromium does it too (on space, tabs and \n.
        // We do it on all Java whitespace.
        while (Character.isWhitespace(urlChars[idx])) {
            idx++;
        }

        // TODO: Skip traling spaces

        ParseUrlState state = ParseUrlState.SCHEME_START;

        // WHATWG URL 5.2.8: Keep running the following state machine by switching on state, increasing pointer by one
        //                   after each time it is run, as long as pointer does not point past the end of input.
        boolean terminate = false;
        while (!terminate) {

            final boolean isEOF = idx >= urlChars.length;
            final char c = (isEOF)? 0x00 : urlChars[idx];
            final int cp = (isEOF)? 0x00 : Character.codePointAt(urlChars, idx);

            switch (state) {

                case SCHEME_START: {
                    // WHATWG URL .8.1: If c is an ASCII alpha, append c, lowercased, to buffer, and set state to scheme state.
                    if (isASCIIAlpha(cp)) {
                        buffer.append(Character.toLowerCase(c));
                        state = ParseUrlState.SCHEME;
                        idx++;
                    } else {
                        // WHATWG URL .8.2: Otherwise, if state override is not given, set state to no scheme state,
                        //                  and decrease pointer by one.
                        state = ParseUrlState.NO_SCHEME;

                        //TODO: 3. Otherwise, parse error, terminate this algorithm.
                    }
                    break;
                }
                case SCHEME: {
                    // WHATWG URL .8.1: If c is an ASCII alphanumeric, "+", "-", or ".", append c, lowercased, to buffer.
                    if (isASCIIAlphanumeric(cp) || c == '+' || c == '-' || c == '.') {
                        buffer.append(Character.toLowerCase(c));
                        idx++;
                    }

                    // WHATWG URL .8.2: Otherwise, if c is ":", set url's scheme to buffer, buffer to the empty string,
                    //                  and then run these substeps:
                    else if (c == ':') {
                        scheme = buffer.toString();
                        buffer.setLength(0);

                        //TODO: WHATWG URL .1: If state override is given, terminate this algorithm.

                        // WHATWG URL .2: If url's scheme is a relative scheme, set url's relative flag.
                        relativeSchemeFlag = isRelativeScheme(scheme);

                        // WHATWG URL .3: If url's scheme is "file", set state to relative state.
                        if ("file".equals(scheme)) {
                            state = ParseUrlState.RELATIVE;
                        }
                        // WHATWG URL .4: Otherwise, if url's relative flag is set, base is not null and base's
                        //                     scheme is equal to url's scheme, set state to relative or authority state.
                        else if (relativeSchemeFlag && base != null && false) { // TODO: Complete condition here
                            state = ParseUrlState.RELATIVE_OR_AUTHORITY;
                        }
                        // WHATWG URL .5: Otherwise, if url's relative flag is set, set state to authority first slash state.
                        else if (relativeSchemeFlag) {
                            state = ParseUrlState.AUTHORITY_FIRST_SLASH;
                        }
                        // WHAT WG URL .6: Otherwise, set state to scheme data state.
                        else {
                            state = ParseUrlState.SCHEME_DATA;
                        }

                    }

                    // WHATWG URL: Otherwise, if state override is not given, set buffer to the empty string,
                    //                  state to no scheme state, and start over (from the first code point in input).
                    else if (stateOverride != null) {
                        buffer.setLength(0);
                        state = ParseUrlState.NO_SCHEME;
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
                        query = "";
                        state = ParseUrlState.QUERY;
                    }
                    // WHATWG URL: Otherwise, if c is "#", set url's fragment to the empty string and state to fragment state.
                    else if (c == '#') {
                        fragment = "";
                        state = ParseUrlState.FRAGMENT;
                    }
                    // WHATWG URL: Otherwise, run these substeps:
                    else {

                        //XXX: This seems to be missing in the WHATWG URL spec?
                        if (isEOF) {
                            terminate = true;
                        }

                        // WHATWG URL: If c is not the EOF code point, not a URL code point, and not "%", parse error.
                        else if (!isEOF && c != '%' && !isURLCodePoint(cp)) {
                            throw new MalformedURLException("Bad scheme data");
                        }

                        // WHATWG URL: If c is "%" and remaining does not start with two ASCII hex digits, parse error.
                        else if (c == '%' && ( idx >= urlChars.length - 2 || !isASCIIHexDigit(urlChars[idx+1]) || !isASCIIHexDigit(urlChars[idx+2]))) {
                            throw new MalformedURLException("Invalid percent-encoded code");

                        }

                        // WHATWG URL: If c is none of EOF code point, U+0009, U+000A, and U+000D, utf-8 percent encode
                        //             c using the simple encode set, and append the result to url's scheme data.
                        else if (!isEOF && cp != 0x0009 && cp != 0x000A && cp != 0x000D) {
                            utf8PercentEncode(cp, EncodeSet.SIMPLE, buffer);
                        }

                    }

                    break;
                }

                case NO_SCHEME: {
                    if (base == null || !isRelativeScheme(base.scheme())) {
                        throw new MalformedURLException();
                    }
                    state = ParseUrlState.RELATIVE;
                    idx--;
                    break;
                }

                case RELATIVE_OR_AUTHORITY: {
                    if (c == '/' && idx < urlChars.length - 1 && urlChars[idx+1] == '/') {
                        state = ParseUrlState.AUTHORITY_IGNORE_SLASHES;
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
                        query = (base == null)? null : base.queryString();
                    } else if (c == '/' || c == '\\') {
                        if (c == '\\') {
                            //TODO: Log parse error
                        }
                        state = ParseUrlState.RELATIVE_SLASH;
                    } else if (c == '?') {
                        host = (base == null)? null : base.host();
                        port = (base == null)? null : base.port();
                        path = (base == null)? null : base.path();
                        query = "";
                        state = ParseUrlState.QUERY;
                    } else if (c == '#') {
                        host = (base == null)? null : base.host();
                        port = (base == null)? null : base.port();
                        path = (base == null)? null : base.path();
                        query = (base == null)? null : base.queryString();
                        fragment = "";
                        state = ParseUrlState.FRAGMENT;
                    } else {
                        //TODO: file: and Windows drive quirk
                        state = ParseUrlState.RELATIVE_PATH;
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
                            state = ParseUrlState.FILE_HOST;
                        } else {
                            state = ParseUrlState.AUTHORITY_IGNORE_SLASHES;
                        }
                    } else {
                        if (!"file".equals(scheme)) {
                            host = base.host();
                            port = base.port();
                        }
                        state = ParseUrlState.RELATIVE_PATH;
                        idx--;
                    }
                    break;
                }

                case AUTHORITY_FIRST_SLASH: {
                    if (c == '/') {
                        state = ParseUrlState.AUTHORITY_SECOND_SLASH;
                    } else {
                        //TODO: Log error
                        state = ParseUrlState.AUTHORITY_IGNORE_SLASHES;
                        idx--;
                    }
                    break;
                }

                case AUTHORITY_SECOND_SLASH: {
                    if (c == '/') {
                        state = ParseUrlState.AUTHORITY_IGNORE_SLASHES;
                    } else {
                        //TODO: Log error
                        state = ParseUrlState.AUTHORITY_IGNORE_SLASHES;
                        idx--;
                    }
                    break;
                }

                case AUTHORITY_IGNORE_SLASHES: {
                    if (c != '/' && c != '\\') {
                        state = ParseUrlState.AUTHORITY;
                    } else {
                        //TODO: Log error
                    }
                    break;
                }

                case AUTHORITY: {
                    if (c == '@') {
                        //TODO
                    } //TODO...
                }

            }

        }

    }

    private boolean isASCIIAlphaUppercase(final int cp) {
        return cp >= 0x0061 && cp <= 0x007A;
    }

    private boolean isASCIIAlphaLowercase(final int cp) {
        return cp >= 0x0041 && cp <= 0x005A;
    }

    private boolean isASCIIAlpha(final int cp) {
        return isASCIIAlphaLowercase(cp) || isASCIIAlphaUppercase(cp);
    }

    private boolean isASCIIDigit(final int cp) {
        return cp >= 0x0030 && cp <= 0x0039;
    }

    private boolean isASCIIAlphanumeric(final int cp) {
        return isASCIIAlpha(cp) || isASCIIDigit(cp);
    }

    private boolean isASCIIHexDigit(final int cp) {
        return (cp >= 0x0041 && cp <= 0x0046) || (cp >= 0x0061 && cp <= 0x0066) || isASCIIDigit(cp);
    }

    private boolean isURLCodePoint(final int cp) {
        return
                isASCIIAlphanumeric(cp) ||
                cp == '!' ||
                cp == '$' ||
                cp == '&' ||
                cp == '\'' ||
                cp == '(' ||
                cp == ')' ||
                cp == '*' ||
                cp == '+' ||
                cp == ',' ||
                cp == '-' ||
                cp == '.' ||
                cp == '/' ||
                cp == ':' ||
                cp == ';' ||
                cp == '=' ||
                cp == '?' ||
                cp == '@' ||
                cp == '_' ||
                cp == '~' ||
                (cp >= 0x00A0 && cp <= 0xD7FF) ||
                (cp >= 0xE000 && cp <= 0xFDCF) ||
                (cp >= 0xFDF0 && cp <= 0xFFEF) ||
                (cp >= 0x10000 && cp <= 0x1FFFD) ||
                (cp >= 0x20000 && cp <= 0x2FFFD) ||
                (cp >= 0x30000 && cp <= 0x3FFFD) ||
                (cp >= 0x40000 && cp <= 0x4FFFD) ||
                (cp >= 0x50000 && cp <= 0x5FFFD) ||
                (cp >= 0x60000 && cp <= 0x6FFFD) ||
                (cp >= 0x70000 && cp <= 0x7FFFD) ||
                (cp >= 0x80000 && cp <= 0x8FFFD) ||
                (cp >= 0x90000 && cp <= 0x9FFFD) ||
                (cp >= 0xA0000 && cp <= 0xAFFFD) ||
                (cp >= 0xB0000 && cp <= 0xBFFFD) ||
                (cp >= 0xC0000 && cp <= 0xCFFFD) ||
                (cp >= 0xD0000 && cp <= 0xDFFFD) ||
                (cp >= 0xE0000 && cp <= 0xEFFFD) ||
                (cp >= 0xF0000 && cp <= 0xFFFFD) ||
                (cp >= 0x100000 && cp <= 0x10FFFD);
    }

    private static enum EncodeSet {
        SIMPLE,
        DEFAULT,
        PASSWORD,
        USERNAME
    }

    private static boolean isInSimpleEncodeSet(final int cp) {
        return cp < 0x0020 || cp > 0x007E;
    }

    private static boolean isInDefaultEncodeSet(final int cp) {
        return isInSimpleEncodeSet(cp) || cp == '"' || cp == '#' || cp == '<' || cp == '>' || cp == '?' || cp == '`';
    }

    private static boolean isInPasswordEncodeSet(final int cp) {
        return isInDefaultEncodeSet(cp) || cp == '/' || cp == '@' || cp == '\\';
    }

    private static boolean isInUsernameEncodeSet(final int cp) {
        return isInPasswordEncodeSet(cp) || cp == ':';
    }

    private static final char[] _hex = "0123456789ABCDEF".toCharArray();
    private static void byteToHex(final byte b, StringBuilder buffer) {
        int i = b & 0xFF;
        buffer.append(_hex[i >>> 4]);
        buffer.append(_hex[i & 0x0F]);
    }

    private static void percentEncode(final byte b, StringBuilder buffer) {
        buffer.append('%');
        byteToHex(b, buffer);
    }

    private static void utf8PercentEncode(final int cp, final EncodeSet encodeSet, StringBuilder buffer) {
        if (encodeSet == EncodeSet.SIMPLE) {
            if (!isInSimpleEncodeSet(cp)) {
                buffer.append((char)cp);
                return;
            }
        } else if (encodeSet == EncodeSet.DEFAULT) {
            if (!isInDefaultEncodeSet(cp)) {
                buffer.append((char)cp);
                return;
            }
        } else if (encodeSet == EncodeSet.PASSWORD) {
            if (!isInPasswordEncodeSet(cp)) {
                buffer.append((char)cp);
                return;
            }
        } else if (encodeSet == EncodeSet.USERNAME) {
            if (!isInUsernameEncodeSet(cp)) {
                buffer.append((char)cp);
                return;
            }
        } else {
            throw new IllegalArgumentException("encodeSet");
        }

        //FIXME: Let bytes be the result of running utf-8 encode on code point.

        //FIXME: Percent encode each byte in bytes, and then return them concatenated, in the same order.

        percentEncode((byte) cp, buffer);
    }

    private static final List<String> RELATIVE_SCHEMES = Arrays.asList(
        "ftp", "file", "gopher", "http", "https", "ws", "wss"
    );
    private boolean isRelativeScheme(final String scheme) {
        return RELATIVE_SCHEMES.contains(scheme);
    }



    private String parseHost(char[] buffer, int startIdx) throws MalformedURLException {

        // WHATWG URL 4.3.1: If input is the empty string, return failure.
        if (buffer.length <= startIdx) {
            throw new MalformedURLException("buffer.length <= startIdx");
        }

        int hostStartIdx = 0;
        int hostEndIdx = 0;

        // WHATWG URL 4.3.2: If input starts with "[", run these substeps:
        if (buffer[0] == '[') {
            // WHATWG URL 4.3.2.1: If input does not end with "]", parse error, return failure.
            // WHATWG URL 4.3.2.2: Return the result of parsing input with its leading "[" and trailing "]" removed.
            int currentIdx = 1;
            hostStartIdx = 1;
            hostEndIdx = 1;
            while (currentIdx < buffer.length) {
                if (buffer[currentIdx] != ']') {
                    hostEndIdx = currentIdx;
                } else {
                    break;
                }
                currentIdx++;
            }
            if (currentIdx == buffer.length) {
                throw new MalformedURLException("Found '[' at the begining of host, but no matching ']' at the end.");
            }
        }

        // WHATWG URL 4.3.3: Let host be the result of running utf-8's decoder on the percent decoding of input.


    }

    private int _hexDecode(final char c1, final char c2) {
        //TODO: Some micro-optimization here?
        return Integer.parseInt(new String(new char[]{c1, c2}), 16);
    }

}
