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
 * Exception thrown by parsers.
 */
public class GalimatiasParseException extends Exception {

    private ParseIssue parseIssue = ParseIssue.UNSPECIFIED;
    private int position = -1;

    private GalimatiasParseException() {}

    public static Builder builder() {
        return new Builder();
    }

    public GalimatiasParseException(final String message) {
        super(message);
    }

    public GalimatiasParseException(final String message, final int position) {
        super(message);
        this.position = position;
    }

    public GalimatiasParseException(final String message, final ParseIssue parseIssue, final int position, final Throwable exception) {
        super(message, exception);

        if (parseIssue != null) {
            this.parseIssue = parseIssue;
        }

        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public ParseIssue getParseIssue() {
        return parseIssue;
    }

    public static class Builder {
        private String message;
        private ParseIssue parseIssue;
        private int position;
        private Throwable cause;

        private Builder() {}

        public Builder withMessage(final String message) {
            this.message = message;
            return this;
        }

        public Builder withPosition(final int position) {
            this.position = position;
            return this;
        }

        public Builder withParseIssue(final ParseIssue parseIssue) {
            this.parseIssue = parseIssue;
            return this;
        }

        public Builder withCause(final Throwable cause) {
            this.cause = cause;
            return this;
        }

        public GalimatiasParseException build() {
            return new GalimatiasParseException(message, parseIssue, position, cause);
        }
    }
}
