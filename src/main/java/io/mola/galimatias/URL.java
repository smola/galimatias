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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.mola.galimatias.PercentEncoding.*;
import static io.mola.galimatias.URLUtils.defaultEmpty;

/**
 * A parsed URL. Immutable.
 *
 */
public class URL implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String scheme;
    private final String username;
    private final String password;
    private final Optional<Host> host;
    private final Optional<Integer> port;
    private final List<String> path;
    private final String query;
    private final String fragment;

    private final boolean isHierarchical;

    URL(final String scheme,
            final String username, final String password,
            final Optional<Host> host, final Optional<Integer> port,
            final List<String> path,
            final String query, final String fragment,
            final boolean isHierarchical) {
        if (scheme == null) {
            throw new NullPointerException("scheme cannot be null");
        }
        this.scheme = scheme;
        this.username = defaultEmpty(username);
        this.password = password;
        this.host = host;

        // This should be done by the parser, so this is just a defensive check.
        this.port = (port.equals(URLUtils.getDefaultPortForScheme(scheme)))? Optional.empty() : port;

        //FIXME: This should probably be like this right from the parser.
        this.path = (!isHierarchical && path.isEmpty())? Arrays.asList("") : path;

        this.query = query;
        this.fragment = fragment;
        this.isHierarchical = isHierarchical;
    }

    public String scheme() {
        return scheme;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    /**
     * Gets user info component (i.e. user:pass). This will
     * return an empty string if neither user or password
     * are set.
     *
     * @return
     */
    public String userInfo() {
        if (!hasUserInfo()) {
            return "";
        }

        if (password == null) {
            return username;
        }

        return String.format("%s:%s", username, password);
    }

    private boolean hasUserInfo() {
        return (username != null && !"".equals(username)) || (password != null && !"".equals(password));
    }

    public Optional<Host> host() {
        return host;
    }

    public String authority() {
        if (!isHierarchical) {
            return null;
        }
        if (host == null) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        final String userInfo = userInfo();
        if (!userInfo.isEmpty()) {
            output.append(userInfo()).append('@');
        }
        output.append(host.map(Host::toHostString).orElse(""));
        if (port.isPresent()) {
            output.append(':').append(port.get());
        }
        return output.toString();
    }

    public Optional<Integer> port() {
        return (port.isPresent())? port : defaultPort();
    }

    public Optional<Integer> defaultPort() {
        return URLUtils.getDefaultPortForScheme(scheme);
    }

    public String path() {
        if (!isHierarchical) {
            return path.get(0);
        }

        if (path.isEmpty()) {
            return "";
        }

        final StringBuilder output = new StringBuilder();
        for (final String p : path) {
            output.append('/').append(p);
        }
        return output.toString();
    }

    public List<String> pathSegments() {
        return Collections.unmodifiableList(path);
    }

    public String query() {
        if (query == null || query.isEmpty()) {
            return "";
        }

        return "?" + query;
    }

    /**
     * Gets the first query parameter value for a given name.
     *
     * @see {@link #queryParameters(String)}
     *
     * @param name Parameter name.
     * @return The first parameter value or null if there parameter is not present.
     */
    public String queryParameter(final String name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (query == null || query.isEmpty()) {
            return null;
        }
        int start = 0;
        do {
            final int nextAmpersand = query.indexOf('&', start);
            final int end = (nextAmpersand == -1)? query.length() : nextAmpersand;
            int nextEquals = query.indexOf('=', start);
            if (nextEquals == -1 || nextEquals > end) {
                nextEquals = end;
            }
            final int thisNameLength = nextEquals - start;
            final int thisValueLength = end - nextEquals;
            if (thisNameLength == name.length() && query.regionMatches(start, name, 0, name.length())) {
                if (thisValueLength == 0) {
                    return "";
                }
                return query.substring(nextEquals + 1, end);
            }
            if (nextAmpersand == -1) {
                break;
            }
            start = nextAmpersand + 1;
        } while (true);
        return null;
    }

    /**
     * Gets all query parameter values for a given name.
     *
     * @param name Parameter name.
     * @return A {@link java.util.List} with all parameter values or null if the parameter is not present.
     */
    public List<String> queryParameters(final String name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (query == null || query.isEmpty()) {
            return null;
        }
        int start = 0;
        final List<String> result = new ArrayList<String>();
        do {
            final int nextAmpersand = query.indexOf('&', start);
            final int end = (nextAmpersand == -1) ? query.length() : nextAmpersand;
            int nextEquals = query.indexOf('=', start);
            if (nextEquals == -1 || nextEquals > end) {
                nextEquals = end;
            }
            final int thisNameLength = nextEquals - start;
            final int thisValueLength = end - nextEquals;
            if (thisNameLength == name.length() && query.regionMatches(start, name, 0, name.length())) {
                if (thisValueLength == 0) {
                    result.add("");
                } else {
                    result.add(query.substring(nextEquals + 1, end));
                }
            }
            if (nextAmpersand == -1) {
                break;
            }
            start = nextAmpersand + 1;
        } while (true);
        return result;
    }

    public URLSearchParameters searchParameters() {
        return new URLSearchParameters(query);
    }

    public String fragment() {
        if (fragment == null || fragment.isEmpty()) {
            return "";
        }

        return "#" + fragment;
    }

    public String file() {
        if (path == null && query == null) {
            return "";
        }
        final StringBuilder output = new StringBuilder();
        output.append(path());
        if (query != null) {
            output.append('?').append(query);
        }
        return output.toString();
    }

    /**
     * Whether this is a hierarchical URL or not. That is, a URL that allows multiple path segments.
     *
     * The term <em>hierarchical</em> comes form the URI standard
     * (<a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>).
     * Other libraries might refer to it as <em>relative</em> or <em>cannot-be-a-base-URL</em>.
     * The later is the current WHATWG URL standard
     * (see <a href="https://github.com/whatwg/url/issues/89">whatwg/url#89</a> for the rationale).

     * @return
     */
    public boolean isHierarchical() {
        return isHierarchical;
    }

    /**
     * Shorthand for <code>!{@link #isHierarchical}</code>.
     */
    public boolean isOpaque() {
        return !isHierarchical;
    }

    private static String pathSegmentsToString(final Iterable<String> segments) {
        if (segments == null) {
            return null;
        }
        return String.join("/", segments);
    }

    public boolean cannotBeABaseURL() {
        return isOpaque();
    }

    private static List<String> pathStringToSegments(String path) {
        if (path == null) {
            return new ArrayList<String>();
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        final String[] segments = path.split("/", -1);
        final List<String> result = new ArrayList<String>(segments.length + 1);
        if (segments.length == 0) {
            result.add("");
            return result;
        }
        result.addAll(Arrays.asList(segments));
        return result;
    }

    /**
     * Resolves a relative reference to an absolute URL.
     *
     * This is just a convenience method equivalent to:
     *
     * <pre>
     * <code>
     *  URL base = URL.parse("http://base.com");
     *  String relativeReference = "/foo/bar";
     *  URL absoluteURL = base.resolve(relativeReference);
     * </code>
     * </pre>
     *
     * @param input Relative reference.
     * @return Resolved absolute URL.
     * @throws GalimatiasParseException
     */
    public URL resolve(final String input) throws GalimatiasParseException {
        return new URLParser(this, input).parse();
    }

    /**
     * Returns a relative URL reference for the given URL.
     *
     * Behaves as @{link java.net.URI#relativize(URL)}.
     *
     * @param url Absolute URL.
     * @return Relative reference.
     */
    public String relativize(final URL url) {
        if (this.isOpaque() || url.isOpaque()) {
            return url.toString();
        }
        if (!this.scheme().equals(url.scheme())) {
            return url.toString();
        }
        if (this.authority() == null ^ url.authority() == null) {
            return url.toString();
        }
        if (this.authority() != null && !this.authority().equals(url.authority())) {
            return url.toString();
        }

        if (url.path.size() < path.size()) {
            return url.toString();
        }

        for (int i = 0; i < path.size(); i++) {
            if (!url.path.get(i).equals(path.get(i))) {
                return url.toString();
            }
        }

        StringBuilder output = new StringBuilder();
        output.append(String.join("/", url.path.subList(path.size(), url.path.size())));

        if (url.query() != null) {
            output.append('?').append(url.query());
        }
        if (url.fragment() != null) {
            output.append('#').append(url.fragment());
        }
        return output.toString();
    }

    /**
     * Parses a URL by using the default parsing options.
     *
     * @param input
     * @return
     * @throws GalimatiasParseException
     */
    public static URL parse(final String input) throws GalimatiasParseException {
        return new URLParser(input).parse();
    }

    public static URL parse(final URL base, final String input) throws GalimatiasParseException {
        return new URLParser(base, input).parse();
    }

    public static URL parse(final URLParsingSettings settings, final String input) throws GalimatiasParseException {
        return new URLParser(settings, null, input, null, null).parse();
    }

    public static URL parse(final URLParsingSettings settings, final URL base, final String input) throws GalimatiasParseException {
        return new URLParser(settings, base, input, null, null).parse();
    }

    /**
     * Gets a URL object from a relative scheme and a host.
     *
     * @param scheme
     * @param host
     * @return
     * @throws GalimatiasParseException
     */
    public static URL buildHierarchical(final String scheme, final String host) throws GalimatiasParseException {
        if (!URLUtils.isRelativeScheme(scheme)) {
            throw new GalimatiasParseException("Scheme is not relative: " + scheme);
        }
        return new URLParser(scheme + "://" + host).parse();
    }

    /**
     * Gets a URL object for file:// scheme.
     *
     * @return
     * @throws GalimatiasParseException
     */
    public static URL buildFile() throws GalimatiasParseException {
        return new URLParser("file://").parse();
    }

    /**
     * Gets a URL object from a non-relative scheme.
     *
     * @param scheme
     * @return
     * @throws GalimatiasParseException
     */
    public static URL buildOpaque(final String scheme) throws GalimatiasParseException {
        if (URLUtils.isRelativeScheme(scheme)) {
            throw new GalimatiasParseException("Scheme is relative: " + scheme);
        }
        return new URLParser(scheme + ":").parse();
    }

    public URL withScheme(final String newScheme) throws GalimatiasParseException {
        if (this.scheme.equalsIgnoreCase(newScheme)) {
            return this;
        }
        if (newScheme == null) {
            throw new NullPointerException("null scheme");
        }
        if (newScheme.isEmpty()) {
            throw new GalimatiasParseException("empty scheme");
        }
        if (URLUtils.isRelativeScheme(newScheme) == URLUtils.isRelativeScheme(this.scheme)) {
            return new URLParser(newScheme + ":", this, URLParser.State.SCHEME_START).parse();
        }
        return new URLParser(toString().replaceFirst(this.scheme, newScheme)).parse();
    }

    public URL withUsername(String username) throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#set-the-username
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set username on opaque URL");
        }
        final String newUsername = parseUserinfo(username);
        if (this.username.equals(username)) {
            return this;
        }
        return new URL(this.scheme, newUsername, this.password, this.host, this.port, this.path, this.query, this.fragment, true);
    }

    public URL withPassword(final String password) throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#set-the-password
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set password on opaque URL");
        }
        final String newPassword = parseUserinfo(password);
        if (this.password.equals(newPassword)) {
            return this;
        }
        return new URL(this.scheme, this.username, newPassword, this.host, this.port, this.path, this.query, this.fragment, true);
    }

    private static String parseUserinfo(final String userinfo) throws GalimatiasParseException {

        // https://url.spec.whatwg.org/#set-the-password
        if (userinfo == null) {
            return "";
        }

        final StringBuilder buffer = new StringBuilder(userinfo.length());
        final CodePointIterator it = new CodePointIterator(userinfo);
        while (it.hasNext()) {
            utf8PercentEncode(it.next(), buffer, userinfoPercentEncodeSet);
        }

        return buffer.toString();
    }

    public URL withHost(final String host) throws GalimatiasParseException {
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set host on opaque URL");
        }

        if (host == null) {
            throw new NullPointerException("null host");
        }

        if (this.host != null && this.host.toString().equals(host)) {
            return this;
        }

        return new URLParser(host, this, URLParser.State.HOST).parse();
    }

    public URL withHost(final Optional<Host> host) throws GalimatiasParseException {
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set host on opaque URL");
        }
        if (host == null) {
            throw new NullPointerException("null host");
        }
        if (this.host != null && this.host.equals(host)) {
            return this;
        }

        return withHost(host.map(Host::toHostString).orElse(""));
    }

    public URL withPort(final Optional<Integer> port) throws GalimatiasParseException {
        // Since we are just getting the parsed port as int, we don't need to call the parser:
        // https://url.spec.whatwg.org/#dom-url-port
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set port on opaque URL");
        }
        if (port == this.port) {
            return this;
        }
        if (!this.port.isPresent() && port.equals(defaultPort())) {
            return this;
        }
        if (port().equals(port)) {
            return this;
        }
        if (port.isPresent()) {
            if (port.get() <= 0) {
                throw new IllegalArgumentException("Cannot set port to zero or negative");
            }
            if (port.get() > 65535) {
                throw new IllegalArgumentException("Canoot set port higher than 65535");
            }
        }
        return new URL(this.scheme, this.username, this.password, this.host, port, this.path, this.query, this.fragment, true);
    }

    public URL withPath(final String path) throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#dom-url-pathname
        if (!isHierarchical) {
            throw new GalimatiasParseException("Cannot set path on opaque URL");
        }

        if (path == null) {
            throw new NullPointerException("path cannot be null");
        }

        return new URLParser(path, this, URLParser.State.PATH_START).parse();
    }

    public URL withQuery(final String query) throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#dom-url-search
        if (this.query == query) {
            return this;
        }
        if (this.query != null && this.query.equals(query)) {
            return this;
        }
        if (query == null || query.isEmpty()) {
            return new URL(this.scheme, this.username, this.password, this.host, this.port, this.path, query, this.fragment, isHierarchical);
        }
        final String parseQuery = (query.charAt(0) == '?')? query.substring(1) : query;
        return new URLParser(parseQuery, this, URLParser.State.QUERY).parse();
    }

    public URL withFragment(final String fragment) throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#dom-url-hash

        //if ("javascript".equals(scheme)) {
        //    throw new GalimatiasParseException("Cannot set fragment on 'javascript:' URL");
        //}
        if (this.fragment == fragment) {
            return this;
        }
        if (this.fragment != null && this.fragment.equals(fragment)) {
            return this;
        }
        if (fragment == null || fragment.isEmpty()) {
            return new URL(this.scheme, this.username, this.password, this.host, this.port, this.path, this.query, fragment, isHierarchical);
        }
        final String parseFragment = (fragment.charAt(0) == '#')? fragment.substring(1) : fragment;
        return new URLParser(parseFragment, this, URLParser.State.FRAGMENT).parse();
    }

    /**
     * Converts to {@link java.net.URI}.
     *
     * Conversion to {@link java.net.URI} will throw
     * {@link java.net.URISyntaxException} if the URL contains
     * unescaped unsafe characters as defined in RFC 2396.
     * In order to prevent this, force RFC 2396 compliance when
     * parsing the URL. For example:
     *
     * NOTE 1: This will not make distinction between no user and password and just empty
     *         user and no password.
     *      <pre>
     *          <code>
     *              URL.parse("http://example.com").toJavaURI().toString() -> "http://example.com"
     *              URL.parse("http://@example.com").toJavaURI().toString() -> "http://example.com"
     *          </code>
     *      </pre>
     *
     * TODO: Check if this exception can actually be thrown
     *
     * @return
     */
    public java.net.URI toJavaURI() throws URISyntaxException {
        if (isHierarchical) {
            return new URI(scheme(),
                    (!"".equals(userInfo()))? percentDecode(userInfo()) : null,
                    (host() != null)? host().toString() : null,
                    port.orElse(-1),
                    (path() != null)? percentDecode(path()) : null,
                    (query() != null)? percentDecode(query()) : null,
                    (fragment() != null)? percentDecode(fragment()) : null
            );
        }
        return new URI(scheme(),
                percentDecode(path()) + ((query() == null)? "" : "?" + percentDecode(query())),
                (fragment() != null)? percentDecode(fragment()) : null
        );
    }

    /**
     * Converts to {@link java.net.URL}.
     *
     * This method is guaranteed to not throw an exception
     * for URL protocols http, https, ftp, file and jar.
     *
     * It might or might not throw {@link java.net.MalformedURLException}
     * for other URL protocols.
     *
     * @return
     */
    public java.net.URL toJavaURL() throws MalformedURLException {
        return new java.net.URL(toString());
    }

    /**
     * Construct a URL from a {@link java.net.URI}.
     *
     * @param uri
     * @return
     */
    public static URL fromJavaURI(java.net.URI uri) {
        //TODO: Let's do this more efficient.
        try {
            return new URLParser(uri.toString()).parse();
        } catch (GalimatiasParseException e) {
            // This should not happen.
            throw new RuntimeException("BUG", e);
        }
    }

    /**
     * Construct a URL from a {@link java.net.URL}.
     *
     * @param url
     * @return
     */
    public static URL fromJavaURL(java.net.URL url) {
        //TODO: Let's do this more efficient.
        try {
            return new URLParser(url.toString()).parse();
        } catch (GalimatiasParseException e) {
            // This should not happen.
            throw new RuntimeException("BUG", e);
        }
    }

    /**
     * Serializes the URL.
     *
     * Note that the "exclude fragment flag" (as in WHATWG standard) is not implemented.
     *
     */
    @Override
    public String toString() {
        final StringBuilder output = new StringBuilder();

        output.append(scheme).append(':');

        if (host.isPresent()) {
            output.append("//");
            if (hasUserInfo()) {
                output.append(username());
                if (!"".equals(password())) {
                    output.append(':').append(password);
                }
                output.append('@');
            }
            output.append(host.get().toHostString());
            if (port.isPresent()) {
                output.append(':').append(port.get());
            }
        } else if ("file".equals(scheme)) {
            output.append("//");
        }


        if (!isHierarchical) {
            output.append(path.get(0));
        } else {
            for (final String p : path) {
                output.append('/').append(p);
            }
        }

        if (query != null) {
            output.append('?').append(query);
        }

        if (fragment != null) {
            output.append('#').append(fragment);
        }

        return output.toString();
    }

    /**
     * Serializes the URL to a human-readable representation. That is,
     * percent-decoded and with IDN domains in its Unicode representation.
     *
     * @return
     */
    public String toHumanString() {
        final StringBuilder output = new StringBuilder();

        output.append(scheme).append(':');

        if (isHierarchical) {
            output.append("//");
            final String userInfo = userInfo();
            if (!userInfo.isEmpty()) {
                output.append(percentDecode(userInfo)).append('@');
            }
            if (host.isPresent()) {
                final Host hostValue = host.get();
                if (hostValue instanceof IPv6Address) {
                    output.append(hostValue.toHostString());
                } else {
                    output.append(hostValue.toHumanString());
                }
            }
            if (port.isPresent()) {
                output.append(':').append(port.get());
            }
            if (path != null) {
                output.append(percentDecode(path()));
            }
        } else {
            output.append(percentDecode(path()));
        }

        if (query != null) {
            output.append('?').append(percentDecode(query));
        }

        if (fragment != null) {
            output.append('#').append(percentDecode(fragment));
        }

        return output.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof URL)) {
            return false;
        }
        final URL other = (URL) obj;
        return  isHierarchical == other.isHierarchical &&
                ((scheme == null)? other.scheme == null : scheme.equals(other.scheme)) &&
                ((username == null)? other.username == null : username.equals(other.username)) &&
                ((password == null)? other.password == null : password.equals(other.password)) &&
                ((host == null)? other.host == null : host.equals(other.host)) &&
                port == other.port &&
                ((path == null)? other.host == null : path.equals(other.path)) &&
                ((fragment == null)? other.fragment == null : fragment.equals(other.fragment)) &&
                ((query == null)? other.query == null : query.equals(other.query))
                ;
    }

    @Override
    public int hashCode() {
        int result = scheme != null ? scheme.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port.hashCode();
        result = 31 * result + (path != null? path.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() + 1 : 0);
        result = 31 * result + (fragment != null ? fragment.hashCode() + 1 : 0);
        result = 31 * result + (isHierarchical ? 1 : 0);
        return result;
    }

}
