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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class URLSearchParamsTest {

    @Test
    void queryParameters() throws GalimatiasParseException {
        URL url = URL.parse("http://example.com/?a=1");
        assertEquals("1", url.queryParameter("a"));
        assertNull(url.queryParameter("b"));
        assertEquals(Arrays.asList("1"), url.queryParameters("a"));
        assertEquals(Arrays.asList(), url.queryParameters("b"));

        url = URL.parse("http://example.com/?a=1&foo=bar&a=2");
        assertEquals("1", url.queryParameter("a"));
        assertEquals("bar", url.queryParameter("foo"));
        assertNull(url.queryParameter("b"));
        assertEquals(Arrays.asList("1", "2"), url.queryParameters("a"));
        assertEquals(Arrays.asList("bar"), url.queryParameters("foo"));
        assertEquals(Arrays.asList(), url.queryParameters("b"));

        url = URL.parse("http://example.com/?a");
        assertTrue(url.queryParameter("a").isEmpty());
        assertNull(url.queryParameter("b"));
        assertEquals(Arrays.asList(""), url.queryParameters("a"));
        assertEquals(Arrays.asList(), url.queryParameters("b"));

        url = URL.parse("http://example.com/?a=");
        assertEquals("", url.queryParameter("a"));
        assertNull(url.queryParameter("b"));
        assertEquals(Arrays.asList(""), url.queryParameters("a"));
        assertEquals(Arrays.asList(), url.queryParameters("b"));
    }

    @Test
    void queryParametersNull() throws GalimatiasParseException {
        final URL url = URL.parse("http://example.com/?a=");
        assertThrows(NullPointerException.class, () -> url.queryParameter(null));
        assertThrows(NullPointerException.class, () -> url.queryParameters(null));
    }

    @Test
    void throwsNullPointerExceptions() {
        assertThrows(NullPointerException.class, () -> new URLSearchParameters((List<NameValue>)null));

        URLSearchParameters params = new URLSearchParameters((String)null);
        assertThrows(NullPointerException.class, () -> params.get(null));
        assertThrows(NullPointerException.class, () -> params.getAll(null));
        assertThrows(NullPointerException.class, () -> params.has(null));
        assertThrows(NullPointerException.class, () -> params.with(null));
        assertThrows(NullPointerException.class, () -> params.withAppended(null));
        assertThrows(NullPointerException.class, () -> params.with("foo", null));
        assertThrows(NullPointerException.class, () -> params.with(null, "foo"));
        assertThrows(NullPointerException.class, () -> params.withAppended("foo", null));
        assertThrows(NullPointerException.class, () -> params.withAppended(null, "foo"));
        assertThrows(NullPointerException.class, () -> params.without(null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "foo"})
    void validConstructorString(final String query) throws NoSuchFieldException {
        final NameValue nameValue = new NameValue("foo", "bar");

        Field field = URLSearchParameters.class.getDeclaredField("nameValues");
        field.setAccessible(true);
        assertThrows(UnsupportedOperationException.class,
                () -> ((List<NameValue>) field.get(new URLSearchParameters(query))).add(nameValue)
                , "Internal list is not immutable");
        field.setAccessible(false);
    }

    @Test
    void validConstructorList() throws NoSuchFieldException {
        final NameValue nameValue = new NameValue("foo", "bar");

        Field field = URLSearchParameters.class.getDeclaredField("nameValues");
        field.setAccessible(true);
        assertThrows(UnsupportedOperationException.class,
                () -> ((List<NameValue>) field.get(new URLSearchParameters(new ArrayList<>()))).add(nameValue)
                , "Internal list is not immutable");
        field.setAccessible(false);
    }

    @Test
    void iterator() {
        final URLSearchParameters params = new URLSearchParameters("foo=1&bar=2&foo=3");
        final Iterator<NameValue> it = params.iterator();
        assertEquals(new NameValue("foo", "1"), it.next());
        assertEquals(new NameValue("bar", "2"), it.next());
        assertEquals(new NameValue("foo", "3"), it.next());
        assertFalse(it.hasNext());
    }

}
