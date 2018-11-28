/**
 * Copyright (c) 2013-2014, 2018 Santiago M. Mola <santi@mola.io>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.mola.galimatias;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static io.mola.galimatias.CodePoints.*;
import static io.mola.galimatias.PercentEncoding.*;

final class URLParser {

    private static final int EOF = -1;

    private final String input;
    private final URL base;
    private final MutableURL url;
    private final URLParsingSettings settings;
    private final State stateOverride;

    private final StringBuilder buffer;
    private final CodePointIterator it;

    private boolean illegalWhiteSpaceEmitted = false;
    private boolean expectedSlashEmitted = false;

    private State state;

    public URLParser(final String input) {
        this(null, null, input, null, null);
    }

    public URLParser(final URL base, final String input) {
        this(null, base, input, null, null);
    }

    public URLParser(final String input, final URL url, final State stateOverride) {
        this(null, null, input, url, stateOverride);
    }

    public URLParser(final URLParsingSettings settings, final URL base, final String input, final URL url, final State stateOverride) {
        if (input == null) {
            throw new NullPointerException("null input");
        }


        this.settings = (settings == null) ? URLParsingSettings.create() : settings;
        this.input = input;
        this.it = new CodePointIterator(input);
        this.buffer = new StringBuilder(input.length());
        this.base = base;
        this.url = mutableURL(url, stateOverride);
        this.stateOverride = stateOverride;
    }

    public enum State {
        SCHEME_START,
        SCHEME,
        NO_SCHEME,
        RELATIVE,
        RELATIVE_SLASH,
        AUTHORITY,
        FILE_HOST,
        HOST,
        PORT,
        QUERY,
        FRAGMENT,
        FILE,
        SPECIAL_RELATIVE_OR_AUTHORITY,
        SPECIAL_AUTHORITY_SLASHES,
        SPECIAL_AUTHORITY_IGNORE_SLASHES,
        PATH_OR_AUTHORITY,
        CANNOT_BE_A_BASE_URL_PATH,
        PATH,
        HOSTNAME,
        FILE_SLASH,
        PATH_START
    }

    public URL parse() throws GalimatiasParseException {
        trimLeading();
        trimTrailing();

        // The spec suggests to remove all tabs and newspaces from input.
        // In order to report original positions for validation, we keep them and
        // skip them as needed with safeNext().

        state = (stateOverride == null) ? State.SCHEME_START : stateOverride;

        while (state != null) {
            switch (state) {
                case SCHEME_START:
                case SCHEME:
                    state = parseScheme();
                    break;
                case NO_SCHEME:
                    state = parseNoScheme();
                    break;
                case SPECIAL_RELATIVE_OR_AUTHORITY:
                    state = parseSpecialRelativeOrAuthority();
                    break;
                case PATH_OR_AUTHORITY:
                    state = parsePathOrAuthority();
                    break;
                case RELATIVE:
                    state = parseRelative();
                    break;
                case RELATIVE_SLASH:
                    state = parseRelativeSlash();
                    break;
                case SPECIAL_AUTHORITY_SLASHES:
                    state = parseSpecialAuthoritySlashes();
                    break;
                case SPECIAL_AUTHORITY_IGNORE_SLASHES:
                    state = parseSpecialAuthorityIgnoreSlashes();
                    break;
                case AUTHORITY:
                    state = parseAuthority();
                    break;
                case HOST:
                case HOSTNAME:
                    state = parseHost();
                    break;
                case PORT:
                    state = parsePort();
                    break;
                case FILE:
                    state = parseFile();
                    break;
                case FILE_SLASH:
                    state = parseFileSlash();
                    break;
                case FILE_HOST:
                    state = parseFileHost();
                    break;
                case PATH_START:
                    state = parsePathStart();
                    break;
                case PATH:
                    state = parsePath();
                    break;
                case CANNOT_BE_A_BASE_URL_PATH:
                    state = parseCannotBeABaseURLPath();
                    break;
                case QUERY:
                    state = parseQuery();
                    break;
                case FRAGMENT:
                    state = parseFragment();
                    break;
            }
        }

        return url.toURL();
    }

    private int safeNext() throws GalimatiasParseException {
        return safeNext(it);
    }

    private int safeNext(final CodePointIterator it) throws GalimatiasParseException {
        while (it.hasNext()) {
            final int cp = it.next();
            if (!isASCIITabOrNewline(cp)) {
                return cp;
            }

            if (!illegalWhiteSpaceEmitted) {
                handleIllegalWhitespaceError();
                illegalWhiteSpaceEmitted = true;
            }
        }
        return EOF;
    }

    private void trimLeading() throws GalimatiasParseException {
        boolean errorEmitted = false;
        while (it.hasNext()) {
            it.mark();
            final int cp = it.next();
            if (!isC0ControlOrSpace(cp)) {
                it.reset();
                break;
            }

            if (!errorEmitted) {
                errorEmitted = true;
                handleUnexpectedLeadingSpace();
            }
        }
    }

    private void trimTrailing() throws GalimatiasParseException {
        boolean errorEmitted = false;
        int newLength = it.length();
        while (newLength > it.position()) {
            final int cp = it.at(newLength - 1);
            if (!isC0ControlOrSpace(cp)) {
                break;
            }

            if (!errorEmitted) {
                errorEmitted = true;
                handleUnexpectedTralingSpace();
            }

            newLength--;
        }

        it.setLength(newLength);
    }

    private State parseScheme() throws GalimatiasParseException {
        //SPEC: scheme start state
        it.mark();
        int c = safeNext();
        if (!isASCIIAlpha(c)) {
            if (stateOverride != null) {
                handleFatalError("Scheme must start with alpha character.");
            }

            it.reset();
            return State.NO_SCHEME;
        }

        buffer.appendCodePoint(Character.toLowerCase(c));

        //SPEC: scheme state
        while (true) {
            c = safeNext();

            if (isASCIIAlphanumeric(c) || c == 0x2B /* + */ || c == 0x2D /* - */ || c == 0x2E /* . */) {
                buffer.appendCodePoint(Character.toLowerCase(c));
                continue;
            }

            if (c == 0x3A /* : */) {
                if (stateOverride != null) {
                    if (isSpecialScheme(url.scheme()) != isSpecialScheme(buffer.toString())) {
                        return null;
                    }

                    if ((includesCredentials() || url.port().isPresent()) && "file".equals(buffer.toString())) {
                        return null;
                    }

                    if ("file".equals(url.scheme()) && url.host() == null) { //TODO: do we allow "empty host"?
                        return null;
                    }
                }

                url.setScheme(buffer.toString());

                if (stateOverride != null) {
                    if (url.port().equals(URLUtils.getDefaultPortForScheme(url.scheme()))) {
                        url.setPort(Optional.empty());
                    }
                    return null;
                }

                buffer.setLength(0);

                if ("file".equals(url.scheme())) {
                    it.mark();
                    final boolean remainingDoubleSlash = safeNext() == '/' && safeNext() == '/';
                    it.reset();
                    if (!remainingDoubleSlash) {
                        handleError("Expected double slash (\"//\")");
                    }
                    return State.FILE;
                }

                if (base != null && isSpecialScheme(url.scheme()) && url.scheme().equals(base.scheme())) {
                    return State.SPECIAL_RELATIVE_OR_AUTHORITY;
                }

                if (isSpecialScheme(url.scheme())) {
                    return State.SPECIAL_AUTHORITY_SLASHES;
                }

                it.mark();
                final boolean remainingIsSlash = safeNext() == 0x2F /* / */;
                it.reset();
                if (remainingIsSlash) {
                    safeNext();
                    return State.PATH_OR_AUTHORITY;
                }

                url.setCannotBeABaseURL(true);
                return State.CANNOT_BE_A_BASE_URL_PATH;
            }

            if (stateOverride == null) {
                it.reset();
                buffer.setLength(0);
                return State.NO_SCHEME;
            }

            handleFatalMissingSchemeError();
        }
    }

    private State parseNoScheme() throws GalimatiasParseException {
        //SPEC: no scheme state
        it.mark();
        final int c = safeNext();
        if (base == null || (base.cannotBeABaseURL() && c != 0x23 /* # */)) {
            handleFatalMissingSchemeError("Cannot parse relative URL without a base URL");
        }

        if (base.cannotBeABaseURL() && c == 0x23 /* # */) {
            url.setScheme(base.scheme());
            url.setPath(base.pathSegments());
            url.setQuery(base.query().replaceFirst("^\\?", ""));
            url.setFragment("");
            url.setCannotBeABaseURL(base.cannotBeABaseURL());
            return State.FRAGMENT;
        }

        it.reset();

        if (!"file".equals(base.scheme())) {
            return State.RELATIVE;
        }

        return State.FILE;
    }

    private State parseSpecialRelativeOrAuthority() throws GalimatiasParseException {
        //SPEC: special relative or authority state
        it.mark();
        final int c = safeNext();
        if (c == 0x2F /* / */ && safeNext() == 0x2F) {
            return State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
        }

        handleError("Relative scheme (" + url.scheme() + ") is not followed by \"://\"");
        it.reset();
        return State.RELATIVE;
    }

    private State parsePathOrAuthority() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#path-or-authority-state
        it.mark();
        final int c = safeNext();
        if (c == 0x2F /* / */) {
            return State.AUTHORITY;
        }

        it.reset();
        return State.PATH;
    }

    private State parseRelative() throws GalimatiasParseException {
        //SPEC: relative state
        url.setScheme(base.scheme());
        it.mark();
        final int c = safeNext();
        switch (c) {
            case EOF:
                url.setUsername(base.username());
                url.setPassword(base.password());
                url.setHost(base.host());
                url.setPort(base.port());
                url.setPath(base.pathSegments());
                url.setQuery(base.query().replaceFirst("^\\?", ""));
                return null;
            case 0x2F /* / */:
                return State.RELATIVE_SLASH;
            case 0x3F /* ? */:
                url.setUsername(base.username());
                url.setPassword(base.password());
                url.setHost(base.host());
                url.setPort(base.port());
                url.setPath(base.pathSegments());
                url.setQuery("");
                return State.QUERY;
            case 0x23 /* # */:
                url.setUsername(base.username());
                url.setPassword(base.password());
                url.setHost(base.host());
                url.setPort(base.port());
                url.setPath(base.pathSegments());
                url.setQuery(base.query().replaceFirst("^\\?", ""));
                url.setFragment("");
                return State.FRAGMENT;
        }

        if (isSpecialScheme(url.scheme()) && c == 0x5c /* \ */) {
            handleError("error"); //TODO: which error?
            return State.RELATIVE_SLASH;
        }

        url.setUsername(base.username());
        url.setPassword(base.password());
        url.setHost(base.host());
        url.setPort(base.port());
        url.setPath(base.pathSegments());
        url.removeLastPath();

        it.reset();
        return State.PATH;
    }

    private State parseRelativeSlash() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#relative-slash-state
        it.mark();
        final int c = safeNext();
        if (isSpecialScheme(url.scheme()) && (c == 0x2F /* / */ || c == 0x5C /* \ */)) {
            if (c == 0x5C /* \ */) {
                handleBackslashAsDelimiterError(); //TODO: which error?
            }

            return State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
        }

        if (c == 0x2F /* / */) {
            return State.AUTHORITY;
        }

        url.setUsername(base.username());
        url.setPassword(base.password());
        url.setHost(base.host());
        url.setPort(base.port());
        it.reset();
        return State.PATH;
    }

    private State parseSpecialAuthoritySlashes() throws GalimatiasParseException {
        //SPEC: special authority slashes state
        it.mark();
        final int c = safeNext();
        if (c == 0x2F /* / */ && safeNext() == 0x2F /* / */) {
            return State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
        }

        expectedSlashEmitted = true;
        handleError("Expected a slash (\"/\")");
        it.reset();
        return State.SPECIAL_AUTHORITY_IGNORE_SLASHES;
    }

    private State parseSpecialAuthorityIgnoreSlashes() throws GalimatiasParseException {
        //SPEC: special authority ignore slashes
        while (true) {
            it.mark();
            final int c = safeNext();
            if (c != 0x2F /* / */ && c != 0x5C) {
                it.reset();
                return State.AUTHORITY;
            }

            if (!expectedSlashEmitted) {
                handleError("Too many slashes or backslashes");
            }
        }
    }

    private State parseAuthority() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#authority-state

        // Try to parse username:password, then backtrack to host parsing
        // if we cannot find it.

        boolean atFlag = false;
        boolean passwordTokenSeenFlag = false;
        it.mark();
        while (true) {
            final int c = safeNext();

            if (c == 0x40 /* @ */) {
                handleIllegalCharacterError("User or password contains an at symbol (\"@\") not percent-encoded");
                if (atFlag) {
                    buffer.insert(0, "%40");
                }
                atFlag = true;
                passwordTokenSeenFlag = decodeUserinfo(passwordTokenSeenFlag);
                buffer.setLength(0);
                it.mark();
                continue;
            }

            if (c == EOF || c == 0x2F /* / */ || c == 0x3F /* ? */ || c == 0x23 /* # */
                    || (isSpecialScheme(url.scheme()) && c == 0x5C /* \ */)) {
                if (atFlag && buffer.length() == 0) {
                    handleFatalIllegalCharacterError("Illegal character in host: “:” is not allowed");
                }

                it.reset();
                buffer.setLength(0);
                return State.HOST;
            }

            buffer.appendCodePoint(c);
        }
    }

    private boolean decodeUserinfo(boolean passwordTokenSeenFlag) throws GalimatiasParseException {
        final CodePointIterator cit = new CodePointIterator(buffer.toString());
        final StringBuilder userinfoBuffer = new StringBuilder(buffer.length());
        while (cit.hasNext()) {
            final int cp = safeNext(cit);
            if (cp == 0x3A /* : */ && !passwordTokenSeenFlag) {
                passwordTokenSeenFlag = true;
                url.setUsername(url.username() + userinfoBuffer.toString());
                userinfoBuffer.setLength(0);
                continue;
            }

            utf8PercentEncode(cp, userinfoBuffer, userinfoPercentEncodeSet);
        }

        if (passwordTokenSeenFlag) {
            url.setPassword(url.password() + userinfoBuffer.toString());
        } else {
            url.setUsername(url.username() + userinfoBuffer.toString());
        }

        return passwordTokenSeenFlag;
    }

    private State parseHost() throws GalimatiasParseException {
        //SPEC: host state, hostname state

        if (stateOverride != null && "file".equals(url.scheme())) {
            return State.FILE_HOST;
        }

        boolean bracketFlag = false;
        while (true) {
            it.mark();
            final int c = safeNext();
            if (c == 0x3A /* : */ && !bracketFlag) {
                if (buffer.length() == 0) {
                    handleFatalIllegalCharacterError("Illegal character in host: “:” is not allowed");
                }

                Host host = null;
                try {
                    host = Host.parseHost(buffer.toString(), !isSpecialScheme(url.scheme()));
                } catch (GalimatiasParseException ex) {
                    handleFatalInvalidHostError(ex);
                }

                url.setHost(Optional.ofNullable(host));
                buffer.setLength(0);
                if (stateOverride == State.HOSTNAME) {
                    return null;
                }

                return State.PORT;
            }

            if (c == EOF || c == 0x2F /* / */ || c == 0x3F /* ? */ || c == 0x23 /* # */
                    || (isSpecialScheme(url.scheme()) && c == 0x5C /* \ */)) {

                it.reset();

                if (isSpecialScheme(url.scheme()) && buffer.length() == 0) {
                    handleFatalInvalidHostError("empty host");
                }

                if (stateOverride != null && buffer.length() == 0
                        && (includesCredentials() || url.port().isPresent())) {
                    handleFatalInvalidHostError("bad host"); //TODO: better error
                    return null;
                }

                Host host = null;
                try {
                    host = Host.parseHost(buffer.toString(), !isSpecialScheme(url.scheme()));
                } catch (GalimatiasParseException ex) {
                    handleFatalInvalidHostError(ex);
                }

                url.setHost(Optional.ofNullable(host));
                buffer.setLength(0);

                if (stateOverride != null) {
                    return null;
                }

                return State.PATH_START;
            }

            if (c == 0x5B /* [ */) {
                bracketFlag = true;
            } else if (c == 0x5D /* ] */) {
                bracketFlag = false;
            }

            buffer.appendCodePoint(c);
        }
    }

    private State parsePort() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#port-state
        while (true) {
            it.mark();
            final int c = safeNext();
            if (isASCIIDigit(c)) {
                buffer.appendCodePoint(c);
            } else if (c == EOF || c == 0x2F /* / */ || c == 0x3F /* ? */ || c == 0x23 /* # */
                    || (isSpecialScheme(url.scheme()) && c == 0x5C /* \ */)
                    || stateOverride != null) {
                if (buffer.length() > 0) {
                    final Optional<Integer> port = Optional.of(Integer.parseUnsignedInt(buffer.toString(), 10));
                    if (port.get() > Math.pow(2, 16) - 1) {
                        handleFatalError("port out of range");
                    }
                    final boolean isDefaultPort = port.equals(URLUtils.getDefaultPortForScheme(url.scheme()));
                    url.setPort(isDefaultPort ? Optional.empty() : port);
                    buffer.setLength(0);
                }

                if (stateOverride != null) {
                    return null;
                }

                it.reset();
                return State.PATH_START;
            } else {
                handleFatalIllegalCharacterError("Illegal character in port: “" + (char)c + "” is not allowed");
            }
        }
    }

    private State parseFile() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#file-state
        url.setScheme("file");
        while (true) {
            it.mark();
            final int c = safeNext();
            if (c == 0x2F /* / */ || c == 0x5C /* \ */) {
                if (c == 0x5C /* \ */) {
                    handleBackslashAsDelimiterError();
                }

                return State.FILE_SLASH;
            }

            if (base != null && "file".equals(base.scheme())) {
                switch (c) {
                    case EOF:
                        url.setHost(base.host());
                        url.setPath(base.pathSegments());
                        url.setQuery(base.query().replaceFirst("^\\?", ""));
                        return null;
                    case 0x3F /* ? */:
                        url.setHost(base.host());
                        url.setPath(base.pathSegments());
                        url.setQuery("");
                        return State.QUERY;
                    case 0x23 /* # */:
                        url.setHost(base.host());
                        url.setPath(base.pathSegments());
                        url.setQuery(base.query().replaceFirst("^\\?", ""));
                        url.setFragment("");
                        return State.FRAGMENT;
                    default:
                        final CodePointIterator cit = new CodePointIterator(it);
                        cit.setPosition(it.position() - 1);
                        if (!startsWithWindowsDriveLetter(cit)) {
                            url.setHost(base.host());
                            url.setPath(base.pathSegments());
                            shortenPath();
                        }

                        handleIllegalCharacterError("Unexpected character in file path"); //TODO: better error
                        it.reset();
                        return State.PATH;
                }
            }

            it.reset();
            return State.PATH;
        }
    }

    private State parseFileSlash() throws GalimatiasParseException {
        //SPEC: file slash state
        it.mark();
        final int c = safeNext();
        if (c == 0x2F /* / */ || c == 0x5C /* \ */) {
            if (c == 0x5C /* \ */) {
                handleBackslashAsDelimiterError();
            }

            return State.FILE_HOST;
        }

        final CodePointIterator cit = new CodePointIterator(it);
        cit.setPosition(it.position() - 1);
        if (base != null && "file".equals(base.scheme()) && !startsWithWindowsDriveLetter(cit)) {
            if (startsWithWindowsDriveLetter(base.pathSegments())) {
                url.addLastPath(base.pathSegments().get(0));
            } else {
                url.setHost(base.host());
            }
        }

        it.reset();
        return State.PATH;
    }

    private State parseFileHost() throws GalimatiasParseException {
        //SPEC: file host state
        while (true) {
            it.mark();
            final int c = safeNext();
            if (c == EOF || c == 0x2F /* / */ || c == 0x5C /* \ */ || c == 0x3F /* ? */ || c == 0x23 /* # */) {
                it.reset();
                //FIXME: avoid multiple copies of buffer/iter to check Windows drive
                if (stateOverride == null && buffer.length() == 2 && startsWithWindowsDriveLetter(new CodePointIterator(buffer.toString()))) {
                    handleError("unexpected Windows drive");
                    return State.PATH;
                }

                if (buffer.length() == 0) {
                    url.setHost(Optional.of(OpaqueHost.parseOpaqueHost("")));
                    return (stateOverride == null) ? State.PATH : null;
                }

                Host host = null;
                try {
                    host = Host.parseHost(buffer.toString(), !isSpecialScheme(url.scheme()));
                } catch (GalimatiasParseException ex) {
                    handleFatalInvalidHostError(ex);
                }

                if ("localhost".equals(host.toString())) {
                    host = OpaqueHost.parseOpaqueHost("");
                }

                url.setHost(Optional.of(host));
                if (stateOverride != null) {
                    return null;
                }

                buffer.setLength(0);
                return State.PATH_START;
            }

            buffer.appendCodePoint(c);
        }
    }

    private State parsePathStart() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#path-start-state
        it.mark();
        final int c = safeNext();
        if (isSpecialScheme(url.scheme())) {
            if (c == 0x5C /* \ */) {
                handleBackslashAsDelimiterError();
            }

            if (c != 0x2F /* / */ && c != 0x5C /* \ */) {
                it.reset();
            }

            return State.PATH;
        }

        if (stateOverride == null && c == 0x3F /* ? */) {
            url.setQuery("");
            return State.QUERY;
        }

        if (stateOverride == null && c == 0x23 /* # */) {
            url.setFragment("");
            return State.FRAGMENT;
        }

        if (c != EOF) {
            if (c != 0x2F /* / */) {
                it.reset();
            }

            return State.PATH;
        }

        return null;
    }

    private State parsePath() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#path-state
        while (true) {
            final int c = safeNext();
            if (c == EOF || c == 0x2F /* / */
                    || (isSpecialScheme(url.scheme()) && c == 0x5C /* \ */)
                    || (stateOverride == null && (c == 0x3F /* ? */ || c == 0x23 /* # */))) {
                if (isSpecialScheme(url.scheme()) && c == 0x5C /* \ */) {
                    handleBackslashAsDelimiterError();
                }

                if (isDoubleDotPathSegment(buffer.toString())) {
                    shortenPath();
                    if (c != 0x2F /* / */ && c != 0x5C /* \ */) {
                        url.addLastPath("");
                    }
                } else if (isSingleDotPathSegment(buffer.toString()) && c != 0x2F /* / */ && c != 0x5C /* \ */) {
                    url.addLastPath("");
                } else if (!isSingleDotPathSegment(buffer.toString())) {
                    if ("file".equalsIgnoreCase(url.scheme())
                            && url.path().isEmpty() && isWindowsDriveLetter(buffer.toString())) {
                        if (url.host().map(Host::toHostString).map((h) -> !h.isEmpty()).orElse(false)) {
                            handleError("unexpected Windows drive");
                            url.setHost(Optional.of(OpaqueHost.parseOpaqueHost("")));
                        }

                        buffer.setCharAt(1, ':');
                    }

                    url.addLastPath(buffer.toString());
                }

                buffer.setLength(0);

                if ("file".equals(url.scheme()) && (c == EOF || c == 0x3F /* ? */ || c == 0x23 /* # */)) {
                    while (url.path().size() > 1 && url.path().get(0).isEmpty()) {
                        handleError("empty path"); //FIXME: reported position is probably wrong here
                        url.removeFirstPath();
                    }
                }

                if (c == 0x3F /* ? */) {
                    url.setQuery("");
                    return State.QUERY;
                }

                if (c == 0x23 /* # */) {
                    url.setFragment("");
                    return State.FRAGMENT;
                }

                if (c == EOF) {
                    return null;
                }
            } else {
                if (!isURLCodePoint(c) && c != 0x25 /* % */) {
                    handleIllegalCharacterError("Illegal character in path segment: “"+CodePoints.toString(c)+"” is not allowed");
                }

                if (c == 0x25 /* % */) {
                    it.mark();
                    if (!isASCIIHexDigit(safeNext())) {
                        handleInvalidPercentEncodingError();

                    } else if (!isASCIIHexDigit(safeNext())) {
                        handleInvalidPercentEncodingError();
                    }
                    it.reset();
                }

                utf8PercentEncode(c, buffer, pathPercentEncodeSet);
            }
        }
    }

    private State parseCannotBeABaseURLPath() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#cannot-be-a-base-url-path-state
        //FIXME: can't we reuse buffer?
        final StringBuilder pathBuffer = new StringBuilder((url.path().isEmpty()) ? "" : url.path().get(0));
        while (true) {
            final int c = safeNext();
            if (c == 0x3F /* ? */) {
                url.setQuery("");
                if (pathBuffer.length() > 0) {
                    url.appendToFirstPath(pathBuffer.toString());
                }
                return State.QUERY;
            }

            if (c == 0x23 /* # */) {
                url.setFragment("");
                if (pathBuffer.length() > 0) {
                    url.appendToFirstPath(pathBuffer.toString());
                }
                return State.FRAGMENT;
            }

            if (c == EOF) {
                //note: short-circuit EOF
                if (pathBuffer.length() > 0) {
                    url.appendToFirstPath(pathBuffer.toString());
                }
                return null;
            }

            if (c != 0x25 /* % */ && !isURLCodePoint(c)) {
                handleIllegalCharacterError("Illegal character in scheme data: “” is not allowed");
            }

            if (c == 0x25 /* % */) {
                it.mark();
                if (!isASCIIHexDigit(safeNext())) {
                    handleInvalidPercentEncodingError();

                } else if (!isASCIIHexDigit(safeNext())) {
                    handleInvalidPercentEncodingError();
                }
                it.reset();
            }

            utf8PercentEncode(c, pathBuffer, C0ControlPercentEncodeSet);
        }
    }

    private State parseQuery() throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#query-state
        //TODO: encoding handling
        //FIXME: can't we reuse buffer?
        final StringBuilder queryBuffer = new StringBuilder(url.query());
        while (true) {
            final int c = safeNext();
            if (stateOverride == null && c == 0x23 /* # */) {
                url.setFragment("");
                if (queryBuffer.length() > 0) {
                    url.setQuery(queryBuffer.toString());
                }
                return State.FRAGMENT;
            }

            if (c == EOF) {
                //note: short-circuit EOF
                if (queryBuffer.length() > 0) {
                    url.setQuery(queryBuffer.toString());
                }
                return null;
            }

            if (c != 0x25 /* % */ && !isURLCodePoint(c)) {
                handleIllegalCharacterError("Illegal character in query: “"+CodePoints.toString(c)+"” is not allowed");
            }

            if (c == 0x25 /* % */) {
                it.mark();
                if (!isASCIIHexDigit(safeNext())) {
                    handleInvalidPercentEncodingError();

                } else if (!isASCIIHexDigit(safeNext())) {
                    handleInvalidPercentEncodingError();
                }
                it.reset();
            }

            //TODO: encoding handling

            final byte[] bytes = new String(Character.toChars(c)).getBytes(StandardCharsets.UTF_8);

            //NOTE: &# handling from the spec is not applicable here.

            for (final byte b : bytes) {
                if (b < 0x21 /* ! */
                        || b > 0x7E /* ~ */
                        || b == 0x22 /* " */
                        || b == 0x23 /* # */
                        || b == 0x3C /* < */
                        || b == 0x3E /* > */
                        || (b == 0x27 /* ' */ && isSpecialScheme(url.scheme()))) {
                    percentEncode(b, queryBuffer);
                } else {
                    queryBuffer.appendCodePoint(b);
                }
            }
        }
    }

    private State parseFragment() throws GalimatiasParseException {
        buffer.setLength(0);
        while (true) {
            final int c = safeNext();
            if (c == EOF) {
                url.setFragment(buffer.toString());
                return null;
            }

            if (c == 0x00) {
                handleIllegalCharacterError("unexpected NULL in fragment");
                continue;
            }

            if (c != 0x25 /* % */ && !isURLCodePoint(c)) {
                handleIllegalCharacterError("Illegal character in fragment: “"+CodePoints.toString(c)+"” is not allowed");
            }

            if (c == 0x25 /* % */) {
                it.mark();
                if (!isASCIIHexDigit(safeNext())) {
                    handleInvalidPercentEncodingError();

                } else if (!isASCIIHexDigit(safeNext())) {
                    handleInvalidPercentEncodingError();
                }
                it.reset();
            }

            utf8PercentEncode(c, buffer, fragmentPercentEncodeSet);
        }
    }

    private MutableURL mutableURL(final URL url, final State stateOverride) {
        final MutableURL mutableURL = new MutableURL(url);
        if (stateOverride == null) {
            return mutableURL;
        }

        switch (stateOverride) {
            case PATH_START:
                mutableURL.setPath(new LinkedList<>());
                break;
            case QUERY:
                mutableURL.setQuery("");
                break;
            case FRAGMENT:
                mutableURL.setFragment("");
                break;
        }
        return mutableURL;
    }

    private void shortenPath() {
        // https://url.spec.whatwg.org/#shorten-a-urls-path
        final List<String> path = url.path();
        if (path.isEmpty()) {
            return;
        }

        if ("file".equals(url.scheme()) && path.size() == 1 && isNormalizedWindowsDriveLetter(path.get(0))) {
            return;
        }

        url.removeLastPath();
    }

    private boolean isSpecialScheme(final String scheme) {
        return "ftp".equals(scheme)
                || "file".equals(scheme)
                || "gopher".equals(scheme)
                || "http".equals(scheme)
                || "https".equals(scheme)
                || "ws".equals(scheme)
                || "wss".equals(scheme);
    }

    private boolean startsWithWindowsDriveLetter(final CodePointIterator it) throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#start-with-a-windows-drive-letter
        final CodePointIterator cit = new CodePointIterator(it);
        final int length = it.length() - it.position();
        if (length < 2) {
            return false;
        }

        int c = safeNext(cit);
        if (!isASCIIAlpha(c)) {
            return false;
        }

        c = safeNext(cit);
        if (c != 0x3A /* : */ && c != 0x7C /* | */) {
            return false;
        }

        if (length == 2) {
            return true;
        }

        c = safeNext(cit);
        return c == 0x2F /* / */ || c == 0x5C /* \ */ || c == 0x3F /* ? */ || c == 0x23 /* # */;
    }

    private boolean startsWithWindowsDriveLetter(final List<String> path) {
        return !path.isEmpty() && isNormalizedWindowsDriveLetter(path.get(0));
    }

    private boolean isNormalizedWindowsDriveLetter(final String s) {
        // https://url.spec.whatwg.org/#normalized-windows-drive-letter
        return s.length() == 2
                && isASCIIAlpha(s.charAt(0))
                && s.charAt(1) == ':';
    }

    private boolean isWindowsDriveLetter(final String s) {
        // https://url.spec.whatwg.org/#windows-drive-letter
        return s.length() == 2
                && isASCIIAlpha(s.charAt(0))
                && (s.charAt(1) == ':' || s.charAt(1) == '|');
    }

    private boolean isDoubleDotPathSegment(final String s) {
        // https://url.spec.whatwg.org/#double-dot-path-segment
        return "..".equals(s)
                || ".%2e".equalsIgnoreCase(s)
                || "%2e.".equalsIgnoreCase(s)
                || "%2e%2e".equalsIgnoreCase(s);
    }

    private boolean isSingleDotPathSegment(final String s) {
        // https://url.spec.whatwg.org/#single-dot-path-segment
        return ".".equals(s)
                || "%2e".equalsIgnoreCase(s);
    }

    private boolean includesCredentials() {
        return !"".equals(url.username()) || !"".equals(url.password());
    }

    private void handleError(GalimatiasParseException parseException) throws GalimatiasParseException {
        this.settings.errorHandler().error(parseException);
    }

    private void handleError(String message) throws GalimatiasParseException {
        handleError(new GalimatiasParseException(message, it.position() - 1));
    }

    private GalimatiasParseException handleFatalError(GalimatiasParseException parseException) throws GalimatiasParseException {
        this.settings.errorHandler().fatalError(parseException);
        throw parseException;
    }

    private void handleFatalError(String message) throws GalimatiasParseException {
        handleFatalError(new GalimatiasParseException(message, it.position()));
    }

    private void handleUnexpectedLeadingSpace() throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Unexpected leading space")
                .withParseIssue(ParseIssue.UNEXPECTED_LEADING_SPACE)
                .withPosition(it.position() - 1)
                .build());
    }

    private void handleUnexpectedTralingSpace() throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Unexpected trailing space")
                .withParseIssue(ParseIssue.UNEXPECTED_TRAILING_SPACE)
                .withPosition(it.length() - 1)
                .build());
    }

    private void handleInvalidPercentEncodingError() throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Percentage (\"%\") is not followed by two hexadecimal digits")
                .withParseIssue(ParseIssue.INVALID_PERCENT_ENCODING)
                .withPosition(it.position() - 1)
                .build());
    }

    private void handleBackslashAsDelimiterError() throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Backslash (\"\\\") used as path segment delimiter")
                .withParseIssue(ParseIssue.BACKSLASH_AS_DELIMITER)
                .withPosition(it.position() - 1)
                .build());
    }

    private void handleIllegalWhitespaceError() throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Tab, new line or carriage return found")
                .withParseIssue(ParseIssue.ILLEGAL_WHITESPACE)
                .withPosition(it.position() - 1)
                .build());
    }

    private void handleIllegalCharacterError(String message) throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage(message)
                .withParseIssue(ParseIssue.ILLEGAL_CHARACTER)
                .withPosition(it.position() - 1)
                .build());
    }

    private void handleFatalMissingSchemeError() throws GalimatiasParseException {
        handleFatalMissingSchemeError("Missing scheme");
    }

    private void handleFatalMissingSchemeError(String message) throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage(message)
                .withPosition(it.position() - 1)
                .withParseIssue(ParseIssue.MISSING_SCHEME)
                .build());
    }

    private void handleFatalIllegalCharacterError(String message) throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage(message)
                .withParseIssue(ParseIssue.ILLEGAL_CHARACTER)
                .withPosition(it.position() - 1)
                .build());
    }

    private void handleFatalInvalidHostError(final String message) throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage("Invalid host: " + message)
                .withParseIssue(ParseIssue.INVALID_HOST)
                .withPosition(it.position() - 1)
                .build());
    }

    private void handleFatalInvalidHostError(final Exception exception) throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage("Invalid host: " + exception.getMessage())
                .withParseIssue(ParseIssue.INVALID_HOST)
                .withPosition(it.position() - 1)
                .withCause(exception)
                .build());
    }

}
