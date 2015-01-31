package io.mola.galimatias;

import static java.lang.Character.*;

final class CodePointIterator {

    private final CharSequence input;
    private int startIdx;
    private int endIdx;
    private int idx;
    private boolean isEOF;
    private int cp;

    public CodePointIterator(final CharSequence input) {
        this.input = input;
        startIdx = 0;
        endIdx = input.length();
        setIdx(startIdx);
    }

    public void setIdx(final int i) {
        this.idx = i;
        this.isEOF = i >= endIdx;
        this.cp = (isEOF || idx < startIdx)? 0x00 : codePointAt(input, i);
    }

    public void next() {
        if (idx == -1) {
            setIdx(startIdx);
        } else {
            final int charCount = charCount(this.cp);
            setIdx(this.idx + charCount);
        }
    }

    public void prev() {
        if (idx <= startIdx) {
            setIdx(startIdx - 1);
            return;
        }
        final int charCount = charCount(codePointBefore(input, idx));
        setIdx(this.idx - charCount);
    }

    public void reset() {
        idx = -1;
        isEOF = false;
    }

    public int idx() {
        return idx;
    }

    public int startIdx() {
        return startIdx;
    }

    public int endIdx() {
        return endIdx;
    }

    public char at(final int i) {
        if (i >= endIdx) {
            return 0x00;
        }
        return input.charAt(i);
    }

    public char atOffset(final int incr) {
        if (idx + incr >= endIdx) {
            return 0x00;
        }
        return input.charAt(idx + incr);
    }

    public int cp() {
        return cp;
    }

    public boolean is(int cp) {
        return this.cp == cp;
    }

    public boolean is(char c) {
        return this.cp == c;
    }

    public boolean isEOF() {
        return isEOF;
    }

    public void trim() {
        while (isWhitespace(cp)) {
            next();
            startIdx++;
        }
        while (endIdx > startIdx && isWhitespace(input.charAt(endIdx - 1))) {
            endIdx--;
        }
    }

}
