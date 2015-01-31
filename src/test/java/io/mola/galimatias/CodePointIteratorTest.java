package io.mola.galimatias;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class CodePointIteratorTest {

    @Test
    public void test() {
        final String str = "teßt";
        final CodePointIterator cp = new CodePointIterator(str);
        assertThat(cp.startIdx()).isEqualTo(0);
        assertThat(cp.endIdx()).isEqualTo(str.length());
        assertThat(cp.idx()).isEqualTo(0);
        assertThat(cp.cp()).isEqualTo('t');
        assertThat(cp.isEOF()).isFalse();
        cp.next();
        assertThat(cp.cp()).isEqualTo('e');
        assertThat(cp.isEOF()).isFalse();
        cp.next();
        assertThat(cp.cp()).isEqualTo('ß');
        assertThat(cp.isEOF()).isFalse();
        cp.next();
        assertThat(cp.cp()).isEqualTo('t');
        assertThat(cp.isEOF()).isFalse();
        cp.next();
        assertThat(cp.isEOF()).isTrue();
    }

    @Test
    public void trim() {
        final String str = "  test   ";
        final CodePointIterator cp = new CodePointIterator(str);
        assertThat(cp.startIdx()).isEqualTo(0);
        assertThat(cp.endIdx()).isEqualTo(str.length());
        assertThat(cp.idx()).isEqualTo(0);
        assertThat(cp.cp()).isEqualTo(' ');
        cp.trim();
        assertThat(cp.startIdx()).isEqualTo(2);
        assertThat(cp.idx()).isEqualTo(2);
        assertThat(cp.endIdx()).isEqualTo("  test".length());
        assertThat(cp.isEOF()).isFalse();
    }

}
