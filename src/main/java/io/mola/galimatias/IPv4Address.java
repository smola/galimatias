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

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class IPv4Address extends Host {

    private static final long serialVersionUID = 1L;

    private final int address;

    private IPv4Address(final int address) {
        this.address = address;
    }

    private IPv4Address(final byte[] addrBytes) {
        int addr = 0;
        addr  = addrBytes[3] & 0xFF;
        addr |= ((addrBytes[2] << 8) & 0xFF00);
        addr |= ((addrBytes[1] << 16) & 0xFF0000);
        addr |= ((addrBytes[0] << 24) & 0xFF000000);
        this.address = addr;
    }

    public static IPv4Address parseIPv4Address(final String input) throws GalimatiasParseException {
        return parseIPv4Address(input, new ErrorHandler() {});
    }

    static IPv4Address parseIPv4Address(final String input, final ErrorHandler handler) throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#concept-ipv4-parser
        if (input == null) {
            throw new NullPointerException("null input");
        }

        if (input.isEmpty()) {
            throw new GalimatiasParseException("empty input");
        }

        final String[] parts = input.split("\\.", -1);
        int partsLength = parts.length;
        if (partsLength > 0 && parts[partsLength - 1].isEmpty()) {
            if (partsLength == 1) {
                handler.error(new GalimatiasParseException("IPv4 address is empty"));
            } else {
                handler.error(new GalimatiasParseException("IPv4 address has trailing dot"));
                partsLength--;
            }
        }

        if (partsLength > 4) {
            throw new GalimatiasParseException("IPv4 with more than 4 parts");
        }

        int[] numbers = new int[partsLength];
        for (int i = 0; i < partsLength; i++) {
            if (parts[i].isEmpty()) {
                throw new GalimatiasParseException("IPv4 with empty part");
            }

            final int n = parsePart(parts[i], handler);
            numbers[i] = n;
            if (n > 255) {
                handler.error(new GalimatiasParseException("IPv4 with number greater than 255"));
                if (i < partsLength -1) {
                    throw new GalimatiasParseException("IPv4 non-last number greater than 255");
                }
            }

            if (i == partsLength - 1 && n >= Math.pow(256, 5 - partsLength)) {
                throw new GalimatiasParseException("IPv4 with too big value");
            }
        }

        long addr = numbers[partsLength-1];
        for (int i = 0; i < partsLength - 1; i++) {
            addr += numbers[i] * Math.pow(256, 3-i);
        }

        return new IPv4Address((int)addr);
    }

    private static int parsePart(String part, final ErrorHandler handler) throws GalimatiasParseException {
        // https://url.spec.whatwg.org/#ipv4-number-parser
        int r = 10;
        if (part.startsWith("0x") || part.startsWith("0X")) {
            handler.error(new GalimatiasParseException("IPv4 contains hexadecimal part"));
            r = 16;
            part = part.substring(2);
        } else if (part.startsWith("0")) {
            handler.error(new GalimatiasParseException("IPv4 contains octal part"));
            r = 8;
            part = part.substring(1);
        }

        if (part.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseUnsignedInt(part, r);
        } catch (NumberFormatException ex) {
            throw new GalimatiasParseException("invalid number format in IPv4 address");
        }
    }

    /**
     * Convert to @{java.net.InetAddress}.
     *
     * @return The IPv4 address as a @{java.net.InetAddress}.
     */
    public Inet4Address toInetAddress() throws UnknownHostException {
        return (Inet4Address) Inet4Address.getByAddress(getBytes());
    }

    /**
     * Convert from @{java.net.Inet4Address}.
     *
     * @param inet4Address The IPv4 address as a @{java.net.Inet4Address}.
     * @return The IPv4 address as a @{IPv4Address}.
     */
    public static IPv4Address fromInet4Adress(final Inet4Address inet4Address) {
        return new IPv4Address(inet4Address.getAddress());
    }

    private byte[] getBytes() {
        return new byte[] {
                (byte) (address >> 24 & 0x00FF),
                (byte) (address >> 16 & 0x00FF),
                (byte) (address >> 8 & 0x00FF),
                (byte) (address & 0x00FF)
        };
    }

    @Override
    public String toString() {
        byte[] bytes = getBytes();
        return String.format("%d.%d.%d.%d", bytes[0] & 0x00FF, bytes[1] & 0x00FF, bytes[2] & 0x00FF, bytes[3] & 0x00FF);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IPv4Address)) {
            return false;
        }
        return this.address == ((IPv4Address) obj).address;
    }

    @Override
    public int hashCode() {
        return address;
    }

}
