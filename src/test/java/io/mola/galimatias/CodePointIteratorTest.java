/**
 * Copyright (c) 2018 Santiago M. Mola <santi@mola.io>
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

import static org.junit.jupiter.api.Assertions.*;

class CodePointIteratorTest {

    @Test
    void nullInput() {
        assertThrows(NullPointerException.class, () -> new CodePointIterator((String)null));
        assertThrows(NullPointerException.class, () -> new CodePointIterator((CodePointIterator)null));
    }

    @Test
    void emptyInput() {
        String input = "";
        CodePointIterator it = new CodePointIterator(input);
        assertFalse(it.hasNext());
    }

    @Test
    void someInput() {
        String input = "a\uD834\uDD1E";
        CodePointIterator it = new CodePointIterator(input);
        assertTrue(it.hasNext());
        assertEquals(0x61, it.next());
        assertTrue(it.hasNext());
        assertEquals(0x1D11E, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void currentWithEmptyInput() {
        String input = "";
        CodePointIterator it = new CodePointIterator(input);
        assertFalse(it.hasNext());
        assertEquals(0, it.current());
    }


    @Test
    void currentWithSomeInput() {
        String input = "a\uD834\uDD1E";
        CodePointIterator it = new CodePointIterator(input);
        assertTrue(it.hasNext());
        assertEquals(0x61, it.next());
        assertEquals(0x61, it.current());
        assertTrue(it.hasNext());
        assertEquals(0x1D11E, it.next());
        assertEquals(0x1D11E, it.current());
        assertFalse(it.hasNext());
    }

    @Test
    void position() {
        String input = "a\uD834\uDD1E";
        CodePointIterator it = new CodePointIterator(input);
        assertEquals(0, it.position());
        assertTrue(it.hasNext());
        assertEquals(0x61, it.next());
        assertEquals(1, it.position());
        assertTrue(it.hasNext());
        assertEquals(0x1D11E, it.next());
        assertEquals(3, it.position());
        assertFalse(it.hasNext());
    }

}