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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseIssueTest {

    static Stream<Arguments> data() {
        return Arrays.stream(new Object[][] {
                { "www.example.com", ParseIssue.MISSING_SCHEME, true },
                { "http://www.example.com/%", ParseIssue.INVALID_PERCENT_ENCODING, false },
                { "http://www.example.com\\path", ParseIssue.BACKSLASH_AS_DELIMITER, false },
                { "http://us`er:pass@www.example.com/path", ParseIssue.ILLEGAL_CHARACTER, false },
                { "http://:\uD83D\uDCA9@example.com/bar", ParseIssue.ILLEGAL_CHARACTER, false },
                { "http://www.exam\tple.com/path", ParseIssue.ILLEGAL_WHITESPACE, false },
                { "http://user:pass@/path", ParseIssue.INVALID_HOST, true }
        }).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("data")
    void handlesGalimiatiasParseExceptionWithCorrectParseIssue(String url, ParseIssue parseIssue, boolean errorIsFatal) {
        final List<GalimatiasParseException> errorExceptions = new ArrayList<>();
        final List<GalimatiasParseException> fatalExceptions = new ArrayList<>();

        final URLParsingSettings settings = URLParsingSettings.create().withErrorHandler(new ErrorHandler() {
            @Override
            public void error(GalimatiasParseException error) throws GalimatiasParseException {
                errorExceptions.add(error);
                throw error;
            }

            @Override
            public void fatalError(GalimatiasParseException error) {
                fatalExceptions.add(error);
            }
        });


        try {
            URL.parse(settings, url);
        } catch (GalimatiasParseException ignored) {}

        if (errorIsFatal) {
            assertEquals(1, fatalExceptions.size());
            assertEquals(parseIssue, fatalExceptions.get(0).getParseIssue());
        } else {
            assertEquals(1, errorExceptions.size());
            assertEquals(parseIssue, errorExceptions.get(0).getParseIssue());
        }
    }
}
