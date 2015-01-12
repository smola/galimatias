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

import io.mola.galimatias.theories.AnyUrlyString;
import io.mola.galimatias.theories.FooOrNullString;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fest.assertions.Assertions.*;
import static org.junit.Assume.*;
import static org.junit.Assert.*;

@RunWith(Theories.class)
public class NameValueTest {

    private static final Logger log = LoggerFactory.getLogger(NameValueTest.class);

    @Theory
    public void nullsThrow(@FooOrNullString final String name, @FooOrNullString final String value) {
        assumeTrue(name == null || value == null);
        try {
            new NameValue(name, value);
            fail("null was not thrown");
        } catch (NullPointerException ex) { }
    }

    @Theory
    public void getters(@AnyUrlyString final String name, @AnyUrlyString final String value) {
        final NameValue nameValue = new NameValue(name, value);
        assertThat(nameValue.name()).isEqualTo(name);
        assertThat(nameValue.value()).isEqualTo(value);
    }

    @Theory
    public void equalsForUnequals(@AnyUrlyString final String name1, @AnyUrlyString final String value1,
                                  @AnyUrlyString final String name2, @AnyUrlyString final String value2) {
        assumeTrue(!name1.equals(name2) || !value1.equals(value2));
        final NameValue nameValue1 = new NameValue(name1, value1);
        final NameValue nameValue2 = new NameValue(name2, value2);
        assertThat(nameValue1).isNotEqualTo(nameValue2);
        if (nameValue1.hashCode() == nameValue2.hashCode()) {
            log.warn("hashCode collision for {} and {}", nameValue1, nameValue2);
        }
        assertThat(nameValue1.toString()).isNotEqualTo(nameValue2.toString());
    }

    @Theory
    public void equalsForEquals(@AnyUrlyString final String name, @AnyUrlyString final String value) {
        final NameValue nameValue1 = new NameValue(name, value);
        final NameValue nameValue2 = new NameValue(name, value);
        assertThat(nameValue1).isEqualTo(nameValue2);
        assertThat(nameValue1.hashCode()).isEqualTo(nameValue2.hashCode());
        assertThat(nameValue1.toString()).isEqualTo(nameValue2.toString());
    }

}
