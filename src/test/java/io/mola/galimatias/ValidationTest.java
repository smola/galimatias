package io.mola.galimatias;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.mola.galimatias.ParseIssue.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationTest {

    static Stream<Case> data() {
        return Arrays.stream(new Case[]{
                c(null, "a", e(MISSING_SCHEME, 0, "Missing scheme")),
                c(null, "http://", e(INVALID_HOST, 6, "Invalid host: empty host")),
                c("http://a", "http:", e(UNSPECIFIED, 5, "Relative scheme (http) is not followed by \"://\"")),
                c(null, "s:\u001B", e(ILLEGAL_CHARACTER, 2, "Illegal character in scheme data: “\u001B” is not allowed")),

                c(null, "http:a", e(UNSPECIFIED, 5, "Expected a slash (\"/\")")),
                c(null, "http:/a", e(UNSPECIFIED, 6, "Expected a slash (\"/\")")),
                c(null, "http:///a", e(UNSPECIFIED, 7, "Unexpected slash or backslash")),
                c(null, "http://\\a", e(UNSPECIFIED, 7, "Unexpected slash or backslash")),

                c(null, "s:%", e(INVALID_PERCENT_ENCODING, 2, "Percentage (\"%\") is not followed by two hexadecimal digits")),
                c(null, "http://%@a", e(INVALID_PERCENT_ENCODING, 8, "Percentage (\"%\") is not followed by two hexadecimal digits")),
                c(null, "http://a/%", e(INVALID_PERCENT_ENCODING, 9, "Percentage (\"%\") is not followed by two hexadecimal digits")),
                c(null, "http://a/p%", e(INVALID_PERCENT_ENCODING, 10, "Percentage (\"%\") is not followed by two hexadecimal digits")),
                c(null, "http://a?p%", e(INVALID_PERCENT_ENCODING, 10, "Percentage (\"%\") is not followed by two hexadecimal digits")),
                c(null, "http://a#p%", e(INVALID_PERCENT_ENCODING, 10, "Percentage (\"%\") is not followed by two hexadecimal digits")),

                c(null, "http://a\\p", e(BACKSLASH_AS_DELIMITER, 8, "Backslash (\"\\\") used as path segment delimiter")),
                c(null, "http://a/p\\q", e(BACKSLASH_AS_DELIMITER, 10, "Backslash (\"\\\") used as path segment delimiter")),

                c(null, "http://@@a", e(UNSPECIFIED, 8, "User or password contains an at symbol (\"@\") not percent-encoded")),
                //FIXME: Actual position is 8, but parser reports 10 (@)
                c(null, "http://u`:@a", e(ILLEGAL_CHARACTER, 10, "Illegal character in user or password: “`” is not allowed")),
                c(null, "http://:\uD83D\uDCA9@a", e(ILLEGAL_CHARACTER, 10, "Illegal character in user or password: “\uD83D\uDCA9” is not allowed")),
                c(null, "http://a:1x", e(ILLEGAL_CHARACTER, 10, "Illegal character in port: “x” is not allowed")),
                c(null, "http://a/p\u001B", e(ILLEGAL_CHARACTER, 10, "Illegal character in path segment: “\u001B” is not allowed")),
                c(null, "http://a?p\u001B", e(ILLEGAL_CHARACTER, 10, "Illegal character in query: “\u001B” is not allowed")),
                c(null, "http://a#p\u001B", e(ILLEGAL_CHARACTER, 10, "Illegal character in fragment: “\u001B” is not allowed")),

                c(null, "http://\ta", e(ILLEGAL_WHITESPACE, 7, "Tab, new line or carriage return found")),
                c(null, "http://\t@a", e(ILLEGAL_WHITESPACE, 8, "Tab, new line or carriage return found")),
                c(null, "http://a:1\t1", e(ILLEGAL_WHITESPACE, 10, "Tab, new line or carriage return found")),
                c(null, "http://a/\tp", e(ILLEGAL_WHITESPACE, 9, "Tab, new line or carriage return found")),
                c(null, "http://a?\tq", e(ILLEGAL_WHITESPACE, 9, "Tab, new line or carriage return found")),
                c(null, "http://a#\tf", e(ILLEGAL_WHITESPACE, 9, "Tab, new line or carriage return found"))
        });
    }

    @ParameterizedTest
    @MethodSource("data")
    void validation(final Case c) throws GalimatiasParseException {
        final List<GalimatiasParseException> errors = new ArrayList<>();
        final URLParsingSettings settings = URLParsingSettings.create().withErrorHandler(new ErrorHandler() {
            @Override
            public void error(GalimatiasParseException error) throws GalimatiasParseException {
                errors.add(error);
            }

            @Override
            public void fatalError(GalimatiasParseException error) {
                errors.add(error);
            }
        });
        final URL base = (c.base == null)? null : URL.parse(c.base);
        try {
            URL.parse(settings, base, c.input);
        } catch (GalimatiasParseException ex) {

        }
        assertEquals(
                c.errors.stream().map(ValidationTest::toString).collect(Collectors.toList()),
                errors.stream().map(ValidationTest::toString).collect(Collectors.toList())
        );
    }

    static class Case {
        final String base;
        final String input;
        final List<GalimatiasParseException> errors;
        Case(final String base, final String input, final GalimatiasParseException ...errors) {
            this.base = base;
            this.input = input;
            this.errors = Arrays.asList(errors);
        }

        @Override
        public String toString() {
            return String.format("%s %s", base, input);
        }

    }

    static Case c(final String base, final String input, final GalimatiasParseException ...errors)  {
        return new Case(base, input, errors);
    }

    static GalimatiasParseException e(final ParseIssue issue, final int position, final String message) {
        return GalimatiasParseException.builder().withParseIssue(issue).withPosition(position).withMessage(message).build();
    }

    static String toString(final GalimatiasParseException error) {
        return String.format("%s %d %s", error.getParseIssue().toString(), error.getPosition(), error.getMessage());
    }

}
