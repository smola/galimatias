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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;


class FormURLEncodedParserTest {

    //TODO: add encode-parse-encode tests

    @Test
    void parse() {
        List<NameValue> result;

        result = FormURLEncodedParser.parse("foo=123");
        assertEquals(Arrays.asList(new NameValue("foo", "123")), result);

        result = FormURLEncodedParser.parse("foo=123&bar=456");
        assertEquals(Arrays.asList(new NameValue("foo", "123"), new NameValue("bar", "456")), result);

        result = FormURLEncodedParser.parse("=123");
        assertEquals(Arrays.asList(new NameValue("", "123")), result);

        result = FormURLEncodedParser.parse("foo");
        assertEquals(Arrays.asList(new NameValue("foo", "")), result);

        result = FormURLEncodedParser.parse("foo=");
        assertEquals(Arrays.asList(new NameValue("foo", "")), result);

        result = FormURLEncodedParser.parse("=123");
        assertEquals(Arrays.asList(new NameValue("", "123")), result);

        result = FormURLEncodedParser.parse("foo");
        assertEquals(Arrays.asList(new NameValue("foo", "")), result);

        result = FormURLEncodedParser.parse("foo=");
        assertEquals(Arrays.asList(new NameValue("foo", "")), result);

        result = FormURLEncodedParser.parse("a+=b+");
        assertEquals(Arrays.asList(new NameValue("a ", "b ")), result);

        result = FormURLEncodedParser.parse("a%20=b%20");
        assertEquals(Arrays.asList(new NameValue("a%20", "b%20")), result);

        result = FormURLEncodedParser.parse("©=ß");
        assertEquals(Arrays.asList(new NameValue("©", "ß")), result);
    }

    @Test
    void encode() {
        String result;

        result = FormURLEncodedParser.encode(Arrays.asList(new NameValue("foo", "123")));
        assertEquals("foo=123", result);

        result = FormURLEncodedParser.encode(Arrays.asList(new NameValue("foo", "123"), new NameValue("bar", "456")));
        assertEquals("foo=123&bar=456", result);

        result = FormURLEncodedParser.encode(Arrays.asList(new NameValue("", "123")));
        assertEquals("=123", result);

        result = FormURLEncodedParser.encode(Arrays.asList(new NameValue("foo", "")));
        assertEquals("foo=", result);

        result = FormURLEncodedParser.encode(Arrays.asList(new NameValue("", "123")));
        assertEquals("=123", result);

        result = FormURLEncodedParser.encode(Arrays.asList(new NameValue("a ", "b ")));
        assertEquals("a =b ", result);

        result = FormURLEncodedParser.encode(Arrays.asList(new NameValue("a%20", "b%20")));
        assertEquals("a%2520=b%2520", result);

        result = FormURLEncodedParser.encode(Arrays.asList(new NameValue("©", "ß")));
        assertEquals("%C2%A9=%C3%9F", result);
    }

}
