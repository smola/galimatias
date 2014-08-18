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

import org.junit.Test;

import static org.junit.Assert.*;

public class GalimatiasParseExceptionTest {

    @Test
    public void defaultsParseIssueToUnspecified() {
        GalimatiasParseException exception = new GalimatiasParseException("message");
        assertEquals(ParseIssue.UNSPECIFIED, exception.getParseIssue());
    }

    @Test
    public void defaultsParseIssueToUnspecifiedWhenPositionIsProvided() {
        GalimatiasParseException exception = new GalimatiasParseException("message", 1);
        assertEquals(ParseIssue.UNSPECIFIED, exception.getParseIssue());
    }


    @Test
    public void defaultsPositionToNegativeOne() {
        GalimatiasParseException exception = new GalimatiasParseException("message");
        assertEquals(-1, exception.getPosition());
    }

    @Test
    public void setsParseIssueToUnspecifiedWhenNullIsProvided() {
        GalimatiasParseException exception =  new GalimatiasParseException("message", null, 1, new RuntimeException());
        assertEquals(ParseIssue.UNSPECIFIED, exception.getParseIssue());
    }

    @Test
    public void setsAllFieldsCorrectlyWhenFullConstructorIsUsed() {
        final String message = "message";
        final ParseIssue parseIssue = ParseIssue.INVALID_PERCENT_ENCODING;
        final int position = 1;
        final Throwable cause = new RuntimeException();

        GalimatiasParseException exception = new GalimatiasParseException(message, parseIssue, position, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(parseIssue, exception.getParseIssue());
        assertEquals(position, exception.getPosition());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void setsAllFieldsCorrectlyWhenBuilderIsUsed() {
        final String message = "message";
        final ParseIssue parseIssue = ParseIssue.INVALID_PERCENT_ENCODING;
        final int position = 1;
        final Throwable cause = new RuntimeException();

        GalimatiasParseException exception = GalimatiasParseException.builder()
                .withMessage(message)
                .withParseIssue(parseIssue)
                .withPosition(position)
                .withCause(cause)
                .build();

        assertEquals(message, exception.getMessage());
        assertEquals(parseIssue, exception.getParseIssue());
        assertEquals(position, exception.getPosition());
        assertEquals(cause, exception.getCause());
    }
}
