/**
 * Copyright (c) 2018 Santiago M. Mola <santi@mola.io>
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

import java.util.*;

import static io.mola.galimatias.URLUtils.*;

final class MutableURL {

    private final URL url;
    private String scheme;
    private boolean schemeModified;
    private String username;
    private boolean usernameModified;
    private String password;
    private boolean passwordModified;
    private Optional<Host> host;
    private boolean hostModified;
    private Optional<Integer> port;
    private boolean portModified;
    private LinkedList<String> path;
    private boolean pathModified;
    private String query;
    private boolean queryModified;
    private String fragment;
    private boolean fragmentModified;
    private boolean cannotBeABaseURL;
    private boolean cannotBeABaseURLModified;

    public MutableURL(URL url) {
        this.url = url;
        this.host = Optional.empty();
        this.port = Optional.empty();
    }

    public String scheme() {
        return defaultEmpty((schemeModified || url == null) ? scheme : url.scheme());
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
        this.schemeModified = true;
    }

    public String username() {
        return defaultEmpty((usernameModified || url == null) ? username : url.username());
    }

    public void setUsername(final String username) {
        this.username = username;
        this.usernameModified = true;
    }

    public String password() {
        return defaultEmpty((passwordModified || url == null) ? password : url.password());
    }

    public void setPassword(final String password) {
        this.password = password;
        this.passwordModified = true;
    }

    public Optional<Host> host() {
        return (hostModified || url == null) ? host : url.host();
    }

    public void setHost(final Optional<Host> host) {
        this.host = host;
        this.hostModified = true;
    }

    public Optional<Integer> port() {
        return (portModified || url == null) ? port : url.port();
    }

    public void setPort(final Optional<Integer> port) {
        this.port = port;
        this.portModified = true;
    }

    private final List<String> EMPTY_LIST = Collections.unmodifiableList(Collections.emptyList());

    public List<String> path() {
        if (pathModified) {
            return Collections.unmodifiableList(path);
        }

        if (url != null) {
            return url.pathSegments();
        }

        return EMPTY_LIST;
    }

    public void setPath(final List<String> path) {
        this.path = (path instanceof LinkedList)? (LinkedList<String>)path : new LinkedList<>(path);
        this.pathModified = true;
    }

    public void appendToFirstPath(final String s) {
        if (!pathModified) {
            if (url == null) {
                this.path = new LinkedList<>();
            } else {
                this.path = new LinkedList<>(url.pathSegments());
            }
            this.pathModified = true;
        }

        if (this.path.isEmpty()) {
            this.path.add(s);
        } else {
            this.path.set(0, this.path.get(0) + s);
        }
    }

    public void addLastPath(final String segment) {
        if (!this.pathModified) {
            this.path = (url == null)? new LinkedList<>() : new LinkedList<>(url.pathSegments());
            this.pathModified = true;
        }
        this.path.addLast(segment);
    }

    public void removeLastPath() {
        if (!this.pathModified) {
            this.path = (url == null)? new LinkedList<>() : new LinkedList<>(url.pathSegments());
        }
        this.pathModified = true;
        if (this.path.isEmpty()) {
            return;
        }
        this.path.removeLast();
    }

    public void removeFirstPath() {
        if (!this.pathModified) {
            this.path = (url == null)? new LinkedList<>() : new LinkedList<>(url.pathSegments());
        }
        this.pathModified = true;
        if (this.path.isEmpty()) {
            return;
        }
        this.path.removeFirst();
    }

    public String query() {
        return defaultEmpty((queryModified || url == null) ? query : url.query().replaceFirst("^?", ""));
    }

    public void setQuery(final String query) {
        this.query = query;
        this.queryModified = true;
    }

    public String fragment() {
        return defaultEmpty((fragmentModified || url == null) ? fragment : url.fragment().replaceFirst("^#", ""));
    }

    public void setFragment(final String fragment) {
        this.fragment = fragment;
        this.fragmentModified = true;
    }

    public boolean cannotBeABaseURL() {
        return (cannotBeABaseURLModified || url == null) ? cannotBeABaseURL : url.cannotBeABaseURL();
    }

    public void setCannotBeABaseURL(final boolean cannotBeABaseURL) {
        this.cannotBeABaseURL = cannotBeABaseURL;
        this.cannotBeABaseURLModified = true;
    }

    public URL toURL() {
        return new URL(
                scheme(),
                username(),
                password(),
                host(),
                port(),
                path(),
                query(),
                fragment(),
                !cannotBeABaseURL()
        );
    }
}
