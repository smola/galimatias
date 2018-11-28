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

final class CodePointIterator {

    final private String string;
    private int pos;
    private int cur;
    private int length;
    private int mark;

    public CodePointIterator(final String string) {
        if (string == null) {
            throw new NullPointerException("string cannot be null");
        }
        this.string = string;
        this.pos = 0;
        this.cur = 0;
        this.length = string.length();
    }

    public CodePointIterator(final CodePointIterator it) {
        this.string = it.string;
        this.pos = it.pos;
        this.cur = it.cur;
        this.length = it.length;
        this.mark = it.mark;
    }

    public boolean hasNext() {
        return pos < length;
    }

    public int next() {
        cur = string.codePointAt(pos);
        pos += Character.charCount(cur);
        return cur;
    }

    public int current() {
        return cur;
    }

    public int position() {
        return pos;
    }

    public void mark() {
        this.mark = pos;
    }

    public void reset() {
        this.pos = this.mark;
    }

    public int at(final int i) {
        return string.codePointAt(i);
    }

    public int length() {
        return string.length();
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public void setPosition(final int pos) {
        this.pos = pos;
    }
}
