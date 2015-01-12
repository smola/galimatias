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

import static org.fest.assertions.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class FormURLEncodedParserTest {

    //TODO: add encode-parse-encode tests

    @Test
    public void parse() {
        List<NameValue> result;
        final FormURLEncodedParser parser = FormURLEncodedParser.getInstance();

        result = parser.parse("foo=123");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("foo", "123")));

        result = parser.parse("foo=123&bar=456");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("foo", "123"), new NameValue("bar", "456")));

        result = parser.parse("=123");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("", "123")));

        result = parser.parse("foo");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("foo", "")));

        result = parser.parse("foo=");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("foo", "")));

        result = parser.parse("=123");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("", "123")));

        result = parser.parse("foo");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("foo", "")));

        result = parser.parse("foo=");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("foo", "")));

        result = parser.parse("a+=b+");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("a ", "b ")));

        result = parser.parse("a%20=b%20");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("a%20", "b%20")));

        result = parser.parse("©=ß");
        assertThat(result).isEqualTo(Arrays.asList(new NameValue("©", "ß")));
    }

    @Test
    public void encode() {
        String result;
        final FormURLEncodedParser parser = FormURLEncodedParser.getInstance();

        result = parser.encode(Arrays.asList(new NameValue("foo", "123")));
        assertThat(result).isEqualTo("foo=123");

        result = parser.encode(Arrays.asList(new NameValue("foo", "123"), new NameValue("bar", "456")));
        assertThat(result).isEqualTo("foo=123&bar=456");

        result = parser.encode(Arrays.asList(new NameValue("", "123")));
        assertThat(result).isEqualTo("=123");

        result = parser.encode(Arrays.asList(new NameValue("foo", "")));
        assertThat(result).isEqualTo("foo=");

        result = parser.encode(Arrays.asList(new NameValue("", "123")));
        assertThat(result).isEqualTo("=123");

        result = parser.encode(Arrays.asList(new NameValue("a ", "b ")));
        assertThat(result).isEqualTo("a =b ");

        result = parser.encode(Arrays.asList(new NameValue("a%20", "b%20")));
        assertThat(result).isEqualTo("a%2520=b%2520");

        result = parser.encode(Arrays.asList(new NameValue("©", "ß")));
        assertThat(result).isEqualTo("%C2%A9=%C3%9F");
    }

}
