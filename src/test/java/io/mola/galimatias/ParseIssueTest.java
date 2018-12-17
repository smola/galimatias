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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ParseIssueTest {

    @Parameters(name = "{0}: {1}, fatal: {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "www.example.com", ParseIssue.MISSING_SCHEME, true },
                { "http://www.example.com/%", ParseIssue.INVALID_PERCENT_ENCODING, false },
                { "http://www.example.com\\path", ParseIssue.BACKSLASH_AS_DELIMITER, false },
                { "http://us`er:pass@www.example.com/path", ParseIssue.ILLEGAL_CHARACTER, false },
                { "http://:\uD83D\uDCA9@example.com/bar", ParseIssue.ILLEGAL_CHARACTER, false },
                { "http://www.exam\tple.com/path", ParseIssue.ILLEGAL_WHITESPACE, false },
                { "http://user:pass@/path", ParseIssue.INVALID_HOST, true }
        });
    }

    private ParseIssue parseIssue;
    private boolean errorIsFatal;
    private URLParser parser;
    private GalimatiasParseException errorException;
    private GalimatiasParseException fatalErrorException;

    public ParseIssueTest(String url, ParseIssue parseIssue, boolean errorIsFatal) {
        this.parseIssue = parseIssue;
        this.errorIsFatal = errorIsFatal;

        this.parser = new URLParser(url);
        this.parser.settings(URLParsingSettings.create().withErrorHandler(new ErrorHandler() {
            @Override
            public void error(GalimatiasParseException error) throws GalimatiasParseException {
                errorException = error;
                throw error;
            }

            @Override
            public void fatalError(GalimatiasParseException error) {
                fatalErrorException = error;
            }
        }));
    }

    @Test
    public void handlesGalimiatiasParseExceptionWithCorrectParseIssue() throws GalimatiasParseException {
        try {
            this.parser.parse();
        } catch (GalimatiasParseException ignored) {}

        if (errorIsFatal) {
            assertNotNull("Fatal error expected", fatalErrorException);
            assertEquals(parseIssue, fatalErrorException.getParseIssue());
        } else {
            assertNotNull("Non-fatal error expected", errorException);
            assertEquals(parseIssue, errorException.getParseIssue());
        }
    }
}
