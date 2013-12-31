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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static io.mola.galimatias.URLUtils.isASCIIDigit;

public class IPv6Address extends Host {

    private final short[] pieces;

    IPv6Address(short[] pieces) {
        this.pieces = Arrays.copyOf(pieces, pieces.length);
    }

    public static IPv6Address parseIPv6Address(final String ipString) throws GalimatiasParseException {
        // See also Mozilla's IPv6 parser:
        //  http://bonsai.mozilla.org/cvsblame.cgi?file=/mozilla/nsprpub/pr/src/misc/prnetdb.c&rev=3.54&mark=1561#1561

        if (ipString == null) {
            throw new NullPointerException("Argument is null");
        }
        if (ipString.isEmpty()) {
            throw new GalimatiasParseException("empty string");
        }

        final short[] address = new short[8];

        int piecePointer = 0;
        Integer compressPointer = null;
        int idx = 0;
        final char[] input = ipString.toCharArray();
        boolean isEOF = idx >= input.length;
        char c = (isEOF)? 0x00 : input[idx];

        if (c == ':') {
            if (idx + 1 >= input.length || input[idx+1] != ':') {
                throw new GalimatiasParseException("IPv6 address starting with ':' is not followed by a second ':'.");
            }
            idx += 2;
            piecePointer = 1;
            compressPointer = piecePointer;
        }

        boolean jumpToIpV4 = false;

        while (!isEOF) { // MAIN

            isEOF = idx >= input.length;
            c = (isEOF)? 0x00 : input[idx];

            if (piecePointer == 8) {
                throw new GalimatiasParseException("Address too long");
            }
            if (c == ':') {
                if (compressPointer != null) {
                    throw new GalimatiasParseException("Zero-compression can be used only once.");
                }
                idx++;
                isEOF = idx >= input.length;
                c = (isEOF)? 0x00 : input[idx];
                piecePointer++;
                compressPointer = piecePointer;
                continue;
            }

            int value = 0;
            int length = 0;

            while (length < 4 && URLUtils.isASCIIHexDigit(c)) {
                value =  value * 0x10 + Integer.parseInt("" + c, 16);
                idx++;
                isEOF = idx >= input.length;
                c = (isEOF)? 0x00 : input[idx];
                length++;
            }

            if (c == '.') {
                if (length == 0) {
                    throw new GalimatiasParseException("':' cannot be followed by '.'");
                }
                idx -= length;
                isEOF = idx >= input.length;
                c = (isEOF)? 0x00 : input[idx];
                jumpToIpV4 = true;
                break;
            } else if (c == ':') {
                idx++;
                isEOF = idx >= input.length;
                if (isEOF) {
                    throw new GalimatiasParseException("Cannot end with ':'");
                }
            } else if (!isEOF) {
                throw new GalimatiasParseException("Illegal character");
            }

            address[piecePointer] = (short)value;
            piecePointer++;

        } // end while MAIN

        boolean jumpToFinale = false;

        // Step 7
        if (!jumpToIpV4 && isEOF) {
            jumpToFinale = true;
        }

        if (!jumpToFinale) {
            // Step 8 IPv4
            if (piecePointer > 6) {
                throw new GalimatiasParseException("Not enough room for a IPv4-mapped address");
            }
        }

        // Step 9
        int dotsSeen = 0;

        if (!jumpToFinale) {
            // Step 10: IPv4-mapped address.
            while (!isEOF) {
                // Step 10.1
                int value = 0;

                // Step 10.2
                if (!isASCIIDigit(c)) {
                    throw new GalimatiasParseException("Non-digit character in IPv4-mapped address");
                }

                // Step 10.3
                while (isASCIIDigit(c)) {
                    value = value * 10 + (c - 0x30);
                    idx++;
                    isEOF = idx >= input.length;
                    c = (isEOF)? 0x00 : input[idx];
                }

                // Step 10.4
                if (value > 255) {
                    throw new GalimatiasParseException("Invalid value for IPv4-mapped address");
                }

                // Step 10.5
                if (dotsSeen < 3 && c != '.') {
                    throw new GalimatiasParseException("Illegal character in IPv4-mapped address");
                }

                // Step 10.6
                address[piecePointer] = (short) ((address[piecePointer] << 8) + value);

                // Step 10.7
                if (dotsSeen == 1 || dotsSeen == 3) { //FIXME: This was 0 and 2 in the spec?
                    piecePointer++;
                }

                // Step 10.8
                idx++;
                isEOF = idx >= input.length;
                c = (isEOF)? 0x00 : input[idx];

                // Step 10.9
                if (dotsSeen == 3 && !isEOF) {
                    throw new GalimatiasParseException("Too long IPv4-mapped address");
                }

                // Step 10.10
                dotsSeen++;
            }
        }

        // Step 11 Finale
        if (compressPointer != null) {
            // Step 11.1
            int swaps = piecePointer - compressPointer;
            // Step 11.2
            piecePointer = 7;
            // Step 11.3
            while (swaps != 0) {  //FIXME: Spec states that piecePointer should be checked for
                //       != 0, but it is always non-zero if 'swaps' is non-zero.
                short swappedPiece = address[piecePointer];
                address[piecePointer] = address[compressPointer + swaps - 1];
                address[compressPointer + swaps - 1] = swappedPiece;
                piecePointer--;
                swaps--;
            }
        }
        // Step 12
        else if (piecePointer != 8) {
            throw new GalimatiasParseException("Address too short");
        }

        return new IPv6Address(address);
    }

    /**
     * Convert to @{java.net.InetAddress}.
     *
     * @return The IPv6 address as a @{java.net.InetAddress}.
     */
    public InetAddress toInetAddress() {
        final byte[] bytes = new byte[16];
        for (int i = 0; i < pieces.length; i++) {
            bytes[i*2] = (byte)((pieces[i] >> 8) & 0xFF);
            bytes[i*2+1] = (byte)(pieces[i] & 0xFF);
        }

        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            // Can't happen if we pass the right amount of bytes
            throw new RuntimeException("BUG", e);
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPv6Address that = (IPv6Address) o;

        return Arrays.equals(pieces, that.pieces);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pieces);
    }
}
