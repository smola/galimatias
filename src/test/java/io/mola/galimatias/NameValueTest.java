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

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class NameValueTest {

    static Stream<Arguments> nameValues() {
        return Arrays.stream(new String[][]{
                        new String[]{"foo", "foo"},
                        new String[]{"foo", "bar"},
                        new String[]{"bar", "bar"},
                        new String[]{"bar", "foo"},
                        new String[]{"foo", null},
                        new String[]{null, "foo"},
                        new String[]{null, null}
                }).map((arr) -> Arguments.of(arr[0], arr[1]));
    }

    static Stream<Arguments> nameValuesPairs() {
        return nameValues().flatMap(
                (arg) -> nameValues().map((oargs) -> Arguments.of(arg.get()[0], arg.get()[1], oargs.get()[0], oargs.get()[1]))
        );
    }

    @ParameterizedTest
    @MethodSource("nameValues")
    void nullsThrow(final String name, final String value) {
        assumeTrue(name == null || value == null);
        assertThrows(NullPointerException.class, () -> new NameValue(name, value));
    }

    @ParameterizedTest
    @MethodSource("nameValues")
    void getters(final String name, final String value) {
        assumeTrue(name != null && value != null);
        final NameValue nameValue = new NameValue(name, value);
        assertAll(
                () -> assertEquals(name, nameValue.name()),
                () -> assertEquals(value, nameValue.value())
        );
    }

    @ParameterizedTest
    @MethodSource("nameValuesPairs")
    void equalsForUnequals(final String name1, final String value1,
                                  final String name2, final String value2) {
        assumeTrue(name1 != null);
        assumeTrue(name2 != null);
        assumeTrue(value1 != null);
        assumeTrue(value2 != null);
        assumeTrue(!name1.equals(name2) || !value1.equals(value2));
        final NameValue nameValue1 = new NameValue(name1, value1);
        final NameValue nameValue2 = new NameValue(name2, value2);
        assertNotEquals(nameValue2, nameValue1);
        assertNotEquals(nameValue2.toString(), nameValue1.toString());
    }

    @ParameterizedTest
    @MethodSource("nameValues")
    void equalsForEquals(final String name, final String value) {
        assumeTrue(name != null && value != null);
        final NameValue nameValue1 = new NameValue(name, value);
        final NameValue nameValue2 = new NameValue(name, value);
        assertEquals(nameValue2, nameValue1);
        assertEquals(nameValue2.hashCode(), nameValue1.hashCode());
        assertEquals(nameValue2.toString(), nameValue1.toString());
    }

}
