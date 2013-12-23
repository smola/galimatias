/*
 * Copyright (c) 2013 Santiago M. Mola <santi@mola.io>
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a
 *   copy of this software and associated documentation files (the "Software"),
 *   to deal in the Software without restriction, including without limitation
 *   the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *   and/or sell copies of the Software, and to permit persons to whom the
 *   Software is furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *   OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *   DEALINGS IN THE SOFTWARE.
 */

package io.mola.galimatias;

import java.util.Arrays;

public class IPv6Address extends Host {

    private final short[] pieces;

    IPv6Address(short[] pieces) {
        this.pieces = Arrays.copyOf(pieces, pieces.length);
    }

    @Override
    public String toString() {
        // IPv6 serialization as specified in the WHATWG URL standard.
        // http://url.spec.whatwg.org/#host-serializing

        // Step 1
        final StringBuilder output = new StringBuilder(40);

        // Step 2: Let compress pointer be a pointer to the first 16-bit piece in
        //         the first longest sequences of address's 16-bit pieces that are 0.
        int compressPointer = -1;
        int maxConsecutiveZeroes = 0;
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i] != 0) {
                continue;
            }
            int consecutiveZeroes = 0;
            for (int j = i; j < pieces.length; j++) {
                if (pieces[j] == 0) {
                    consecutiveZeroes++;
                } else {
                    break;
                }
            }
            if (consecutiveZeroes > maxConsecutiveZeroes) {
                compressPointer = i;
                maxConsecutiveZeroes = consecutiveZeroes;
            }
        }

        // Step 3: If there is no sequence of address's 16-bit pieces that are 0 longer than one,
        //         set compress pointer to null.
        //
        // NOTE: Here null is -1, and it was already initialized.

        // Step 4: For each piece in address's pieces, run these substeps:
        for (int i = 0; i < pieces.length; i++) {

            // Step 4.1: If compress pointer points to piece, append "::" to output if piece is address's
            //           first piece and append ":" otherwise, and then run these substeps again with all
            //           subsequent pieces in address's pieces that are 0 skipped or go the next step in the
            //           overall set of steps if that leaves no pieces.
            if (compressPointer == i) {
                if (i == 0) {
                    output.append("::");
                } else {
                    output.append(':');
                }
                while (i < pieces.length && pieces[i] == 0) {
                    i++;
                }
            }

            if (i >= pieces.length) {
                break;
            }

            // Step 4.2: Append piece, represented as the shortest possible lowercase hexadecimal number, to output.
            output.append(Integer.toHexString(pieces[i] & 0xFFFF));

            // Step 4.3: If piece is not address's last piece, append ":" to output.
            if (i < pieces.length - 1) {
                output.append(':');
            }
        }

        return output.toString();
    }


}
