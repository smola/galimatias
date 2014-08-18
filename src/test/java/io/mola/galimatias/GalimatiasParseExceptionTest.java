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
