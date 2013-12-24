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
 */
public class URL implements Serializable {

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
    private final boolean isAbsolute;

    private transient String fullURL;

    URL(final String scheme, final String schemeData,
            final String username, final String password,
            final Host host, final Integer port,
            final String[] path,
            final String query, final String fragment,
            final boolean relativeFlag) {
        this.isAbsolute = true;
        this.scheme = scheme;
        this.schemeData = schemeData;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        if (path != null) {
            this.path = Arrays.copyOf(path, path.length);
        } else {
            this.path = new String[0];
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
        if (relativeFlag) {
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
            return new java.net.URL(
              scheme, host.toString(), (port == null)? -1 : port, file()
            );
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
    public static URL fromJavaURI(java.net.URL url) {
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
        if (fullURL == null) {
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
                    output.append(host);
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

            fullURL = output.toString();
        }

        return fullURL;
    }

}
