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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * A parsed URL. Immutable.
 *
 * TODO: Add modifier methods.
 *
 * TODO: Study android.net.URI implementation. It has interesting API
 *       bits and tricks.
 *
 */
public class URL implements Serializable {

    private static final String[] EMPTY_PATH = new String[0];

    private final String scheme;
    private final String schemeData;
    private final String username;
    private final String password;
    private final Host host;
    private final Integer port;
    private final String[] path;
    private final String query;
    private final String fragment;

    private final boolean relativeFlag;

    URL(final String scheme, final String schemeData,
            final String username, final String password,
            final Host host, final Integer port,
            final String[] path,
            final String query, final String fragment,
            final boolean relativeFlag) {
        this.scheme = scheme;
        this.schemeData = schemeData;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        if (path != null && path.length > 0 && (path.length > 1 || !path[0].equals(""))) {
            this.path = Arrays.copyOf(path, path.length);
        } else {
            this.path = EMPTY_PATH;
        }
        this.query = query;
        this.fragment = fragment;
        this.relativeFlag = relativeFlag;
    }

    public String scheme() {
        return scheme;
    }

    public String schemeData() {
        return schemeData;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    /**
     * Mirrors {@link java.net.URL#getUserInfo()} behaviour.
     *
     * @return
     */
    public String userInfo() {
        if (username == null) {
            return null;
        }
        if (password == null) {
            return username;
        }
        return String.format("%s:%s", username, password);
    }

    public Host host() {
        return host;
    }

    public Integer port() {
        return port;
    }

    public String[] path() {
        return Arrays.copyOf(path, path.length);
    }

    public String pathString() {
        if (!relativeFlag) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        output.append('/');
        if (path.length > 0) {
            output.append(path[0]);
            for (int i = 1; i < path.length; i++) {
                output.append('/').append(path[i]);
            }
        }
        return output.toString();
    }

    public String query() {
        return query;
    }

    public String fragment() {
        return fragment;
    }

    protected String file() {
        final String pathString = pathString();
        if (pathString == null && query == null && fragment == null) {
            return "";
        }
        final StringBuilder output = new StringBuilder(
                ((pathString != null)? pathString.length() : 0) +
                ((query != null)? query.length() + 1 : 0) +
                ((fragment != null)? fragment.length() + 1 : 0)
                );
        if (pathString != null) {
            output.append(pathString);
        }
        if (query != null) {
            output.append('?').append(query);
        }
        if (fragment != null) {
            output.append('#').append(fragment);
        }
        return output.toString();
    }

    boolean relativeFlag() {
        return relativeFlag;
    }

    private static final URLParser DEFAULT_URL_PARSER = new URLParser();

    /**
     * Parses a URL by using the default {@link io.mola.galimatias.URLParser}.
     *
     * @param input
     * @return
     * @throws MalformedURLException
     */
    public static URL parse(final String input) throws MalformedURLException {
        return DEFAULT_URL_PARSER.parse(input);
    }

    public static URL parse(final URL base, final String input) throws MalformedURLException {
        return DEFAULT_URL_PARSER.parse(base, input);
    }

    /**
     * Converts to {@link java.net.URI}.
     *
     * @return
     */
    public java.net.URI toJavaURI() {
        try {
            return new URI(
                scheme, userInfo(), host.toString(), (port == null)? -1 : port, pathString(), query, fragment
            );
        } catch (URISyntaxException e) {
            // This should not happen
            throw new RuntimeException("BUG", e);
        }
    }

    /**
     * Converts to {@link java.net.URL}.
     *
     * @return
     */
    public java.net.URL toJavaURL() {
        try {
            return new java.net.URL(toString());
        } catch (MalformedURLException e) {
            // This should not happend
            throw new RuntimeException("BUG", e);
        }
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
            return DEFAULT_URL_PARSER.parse(uri.toString());
        } catch (MalformedURLException e) {
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
            return DEFAULT_URL_PARSER.parse(url.toString());
        } catch (MalformedURLException e) {
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

        if (relativeFlag) {
            output.append("//");
            if (username != null || password != null) {
                if (username != null) {
                    output.append(username);
                }
                if (password != null) {
                   output.append(':').append(password);
                }
                output.append('@');
            }
            if (host != null) {
                if (host instanceof IPv6Address) {
                    output.append('[').append(host).append(']');
                } else {
                    output.append(host);
                }
            }
            if (port != null) {
                output.append(':').append(port);
            }
            output.append('/');
            if (path.length > 0) {
                output.append(path[0]);
                for (int i = 1; i < path.length; i++) {
                    output.append('/').append(path[i]);
                }
            }
        } else {
            output.append(schemeData);
        }

        if (query != null) {
            output.append('?').append(query);
        }

        if (fragment != null) {
            output.append('#').append(fragment);
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
        return  relativeFlag == other.relativeFlag &&
                ((scheme == null)? other.scheme == null : scheme.equals(other.scheme)) &&
                ((username == null)? other.username == null : username.equals(other.username)) &&
                ((password == null)? other.password == null : password.equals(other.password)) &&
                ((host == null)? other.host == null : host.equals(other.host)) &&
                ((port == null)? other.port == null : port.equals(other.port)) &&
                ((fragment == null)? other.fragment == null : fragment.equals(fragment)) &&
                arrayEquals(path, other.path)
                ;
    }

    private static boolean arrayEquals(final String[] one, final String[] other) {
        if (one == other) {
            return true;
        }
        if (one.length != other.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (!one[i].equals(other[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = scheme != null ? scheme.hashCode() : 0;
        result = 31 * result + (schemeData != null ? schemeData.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(path);
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (fragment != null ? fragment.hashCode() : 0);
        result = 31 * result + (relativeFlag ? 1 : 0);
        return result;
    }

}
