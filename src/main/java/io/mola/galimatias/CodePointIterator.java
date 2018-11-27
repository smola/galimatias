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
