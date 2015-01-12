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

import io.mola.galimatias.theories.FooOrEmptyOrNullString;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.*;

@RunWith(Theories.class)
public class URLSearchParamsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void queryParameters() throws GalimatiasParseException {
        URL url = URL.parse("http://example.com/?a=1");
        assertThat(url.queryParameter("a")).isEqualTo("1");
        assertThat(url.queryParameter("b")).isNull();
        assertThat(url.queryParameters("a")).isEqualTo(Arrays.asList("1"));
        assertThat(url.queryParameters("b")).isEqualTo(Arrays.asList());

        url = URL.parse("http://example.com/?a=1&foo=bar&a=2");
        assertThat(url.queryParameter("a")).isEqualTo("1");
        assertThat(url.queryParameter("foo")).isEqualTo("bar");
        assertThat(url.queryParameter("b")).isNull();
        assertThat(url.queryParameters("a")).isEqualTo(Arrays.asList("1", "2"));
        assertThat(url.queryParameters("foo")).isEqualTo(Arrays.asList("bar"));
        assertThat(url.queryParameters("b")).isEqualTo(Arrays.asList());

        url = URL.parse("http://example.com/?a");
        assertThat(url.queryParameter("a")).isEmpty();
        assertThat(url.queryParameter("b")).isNull();
        assertThat(url.queryParameters("a")).isEqualTo(Arrays.asList(""));
        assertThat(url.queryParameters("b")).isEqualTo(Arrays.asList());

        url = URL.parse("http://example.com/?a=");
        assertThat(url.queryParameter("a")).isEqualTo("");
        assertThat(url.queryParameter("b")).isNull();
        assertThat(url.queryParameters("a")).isEqualTo(Arrays.asList(""));
        assertThat(url.queryParameters("b")).isEqualTo(Arrays.asList());

        try {
            url.queryParameter(null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }
        try {
            url.queryParameters(null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }
    }

    @Test
    public void throwsNullPointerExceptions() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            new URLSearchParameters((List<NameValue>)null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        URLSearchParameters params = new URLSearchParameters((String)null);

        try {
            params.get(null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.getAll(null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.has(null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.with(null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.withAppended(null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.with("foo", null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.with(null, "foo");
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.withAppended("foo", null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.withAppended(null, "foo");
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }

        try {
            params.without(null);
            fail("Did not throw NPE");
        } catch (NullPointerException ex) { }
    }

    @Theory
    public void validConstructorString(@FooOrEmptyOrNullString final String query) throws NoSuchFieldException, IllegalAccessException {
        final NameValue nameValue = new NameValue("foo", "bar");

        Field field = URLSearchParameters.class.getDeclaredField("nameValues");
        field.setAccessible(true);

        try {
            URLSearchParameters params = new URLSearchParameters(query);
            ((List<NameValue>) field.get(params)).add(nameValue);
            fail("Internal list is not immutable");
        } catch (UnsupportedOperationException ex) { }

        field.setAccessible(false);
    }

    @Test
    public void validConstructorList() throws NoSuchFieldException, IllegalAccessException {
        final NameValue nameValue = new NameValue("foo", "bar");

        Field field = URLSearchParameters.class.getDeclaredField("nameValues");
        field.setAccessible(true);

        try {
            URLSearchParameters params = new URLSearchParameters(new ArrayList<NameValue>());
            ((List<NameValue>) field.get(params)).add(nameValue);
            fail("Internal list is not immutable");
        } catch (UnsupportedOperationException ex) { }

        field.setAccessible(false);
    }

    @Theory
    public void withNameValues(@TestURL.TestURLs final TestURL testURL) throws NoSuchFieldException, IllegalAccessException {
        assumeNotNull(testURL.parsedURL);
        final URLSearchParameters params = testURL.parsedURL.searchParameters();
        final String key = "__FOO__";
        final NameValue nameValue1 = new NameValue(key, "bar");
        final NameValue nameValue2 = new NameValue(key, "baz");
        assertThat(params.with(nameValue1).getAll(key))
                .isEqualTo(Arrays.asList("bar"));
        assertThat(params.with(nameValue1).with(nameValue2).getAll(key))
                .isEqualTo(Arrays.asList("baz"));
        assertThat(params.with(nameValue1.name(), nameValue1.value()).getAll(key))
                .isEqualTo(Arrays.asList("bar"));
        assertThat(params.with(nameValue1.name(), nameValue1.value()).with(nameValue2.name(), nameValue2.value()).getAll(key))
                .isEqualTo(Arrays.asList("baz"));
    }

    @Theory
    public void withAppendedNameValues(@TestURL.TestURLs final TestURL testURL) throws NoSuchFieldException, IllegalAccessException {
        assumeNotNull(testURL.parsedURL);
        final URLSearchParameters params = testURL.parsedURL.searchParameters();
        final String key = "__FOO__";
        final NameValue nameValue1 = new NameValue(key, "bar");
        final NameValue nameValue2 = new NameValue(key, "baz");
        assertThat(params.withAppended(nameValue1).getAll(key))
                .isEqualTo(Arrays.asList("bar"));
        assertThat(params.withAppended(nameValue1).withAppended(nameValue2).getAll(key))
                .isEqualTo(Arrays.asList("bar", "baz"));
        assertThat(params.withAppended(nameValue1.name(), nameValue1.value()).getAll(key))
                .isEqualTo(Arrays.asList("bar"));
        assertThat(params.withAppended(nameValue1.name(), nameValue1.value()).withAppended(nameValue2.name(), nameValue2.value()).getAll(key))
                .isEqualTo(Arrays.asList("bar", "baz"));
    }

    @Theory
    public void get(@TestURL.TestURLs final TestURL testURL) throws NoSuchFieldException, IllegalAccessException {
        assumeNotNull(testURL.parsedURL);
        final URLSearchParameters params = testURL.parsedURL.searchParameters();
        final String key = "__FOO__";
        final NameValue nameValue1 = new NameValue(key, "bar");
        final NameValue nameValue2 = new NameValue(key, "baz");
        assertThat(params.withAppended(nameValue1).get(key))
                .isEqualTo("bar");
        assertThat(params.withAppended(nameValue1).withAppended(nameValue2).get(key))
                .isEqualTo("bar");
        assertThat(params.withAppended(nameValue1).get("__BAR__"))
                .isNull();
        assertThat(params.withAppended(nameValue1).withAppended(nameValue2).get("__BAR__"))
                .isNull();
    }

    @Theory
    public void has(@TestURL.TestURLs final TestURL testURL) {
        assumeNotNull(testURL.parsedURL);
        final URLSearchParameters params = testURL.parsedURL.searchParameters();
        assertThat(params.has("__FOO__")).isFalse();
        for (final NameValue nv : params) {
            assertThat(params.has(nv.name())).isTrue();
        }
    }

    @Theory
    public void without(@TestURL.TestURLs final TestURL testURL) {
        assumeNotNull(testURL.parsedURL);
        final URLSearchParameters params = testURL.parsedURL.searchParameters();
        assertThat((Object)params.without("__FOO__")).isEqualTo(params);
        for (final NameValue nv : params) {
            assertThat((Object)params.without(nv.name()).get(nv.name())).isNull();
        }
    }

    @Test
    public void iterator() {
        final URLSearchParameters params = new URLSearchParameters("foo=1&bar=2&foo=3");
        final Iterator<NameValue> it = params.iterator();
        assertThat(it.next()).isEqualTo(new NameValue("foo", "1"));
        assertThat(it.next()).isEqualTo(new NameValue("bar", "2"));
        assertThat(it.next()).isEqualTo(new NameValue("foo", "3"));
        assertThat(it.hasNext()).isFalse();
    }

}
