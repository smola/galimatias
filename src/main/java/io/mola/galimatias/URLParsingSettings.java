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

/**
 * Provides settings for URL parsing.
 *
 * This class is immutable and all its attributes are immutable
 * by default too.
 */
public final class URLParsingSettings {

    private static URLParsingSettings DEFAULT = new URLParsingSettings();

    private ErrorHandler errorHandler;
    private String defaultScheme;

    private URLParsingSettings() {
        this(DefaultErrorHandler.getInstance(), null);
    }

    private URLParsingSettings(final ErrorHandler errorHandler, final String defaultScheme) {
        this.errorHandler = errorHandler;
        this.defaultScheme = defaultScheme;
    }

    /**
     * Gets the @{link ErrorHandler}.
     *
     * Defaults to @{link DefaultErrorHandler}.
     *
     * @return the @{link ErrorHandler}
     */
    public ErrorHandler errorHandler() {
        return this.errorHandler;
    }

    /**
     * Gets the default scheme. By default: null.
     *
     * @see @{link URLParsingSettings#withDefaultScheme}.
     *
     * @return the default scheme
     */
    public String defaultScheme() { return defaultScheme; }

    public static URLParsingSettings create() {
        return DEFAULT;
    }

    /**
     * Sets an error handler.
     *
     * @see @{link ErrorHandler}, @{link DefaultErrorHandler}, @{link StrictErrorHandler}.
     *
     * @param errorHandler
     */
    public URLParsingSettings withErrorHandler(final ErrorHandler errorHandler) {
        return new URLParsingSettings(errorHandler, defaultScheme);
    }

    /**
     * Sets a default scheme. If this option is set to a scheme (e.g. http),
     * and no base URL is used, every parsed URL will be forced to be an absolute URL.
     *
     * @param defaultScheme a URL scheme (e.g. http, https)
     */
    public URLParsingSettings withDefaultScheme(final String defaultScheme) {
        return new URLParsingSettings(errorHandler, defaultScheme);
    }

}
