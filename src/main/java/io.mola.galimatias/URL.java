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
import java.util.Arrays;

/**
 * A parsed URL. Immutable.
 *
 */
public class URL implements Serializable {

    final static String HTTP = "http";
    final static String HTTPS = "https";
    final static String DATA = "data";

    private final static int DEFAULT_HTTP_PORT = 80;
    private final static int DEFAULT_HTTPS_PORT = 443;

    private String scheme;
    private String schemeData;
    private String username;
    private String password;
    private Host host;
    private Integer port;
    private String[] path;
    private String query;
    private String fragment;

    private boolean relativeFlag;
    private boolean isAbsolute;

    private transient String fullURL;

    URL() {

    }

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

    public Host host() {
        return host;
    }

    public Integer port() {
        return port;
    }

    public String[] path() {
        return Arrays.copyOf(path, path.length);
    }

    public String queryString() {
        return query;
    }

    public String fragment() {
        return fragment;
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
                if ((username != null && username.isEmpty()) || password != null) {
                    output.append((username == null) ? "" : username);
                    if (password != null) {
                       output.append(':').append(password);
                    }
                    output.append('@');
                    //FIXME: It is not clear the empty string / null behaviour of username.
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
