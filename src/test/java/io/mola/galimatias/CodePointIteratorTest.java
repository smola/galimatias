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

}