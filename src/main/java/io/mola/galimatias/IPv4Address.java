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

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class IPv4Address extends Host {

    private final int address;

    private IPv4Address(final byte[] addr) {
        int address = 0;
        address  = addr[3] & 0xFF;
        address |= ((addr[2] << 8) & 0xFF00);
        address |= ((addr[1] << 16) & 0xFF0000);
        address |= ((addr[0] << 24) & 0xFF000000);
        this.address = address;
    }

    public static IPv4Address parseIPv4Address(final String input) throws GalimatiasParseException{
        if (input == null) {
            throw new NullPointerException("null input");
        }
        if (input.isEmpty()) {
            throw new GalimatiasParseException("empty input");
        }
        byte[] addr = new byte[4];
        int dotsSeen = 0;
        int addrIdx = 0;
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            int value = 0;
            while (URLUtils.isASCIIDigit(c)) {
                value = value * 10 + (c - 0x30);
                i++;
                c = (i >= input.length())? 0x00 : input.charAt(i);
            }
            if (value > 255) {
                throw new GalimatiasParseException("Malformed IPv4 address, bad value: " + value);
            }
            if (dotsSeen < 3 && c != '.') {
                throw new GalimatiasParseException("Malformed IPv4 address", i);
            }
            if (dotsSeen == 3 && i < input.length()) {
                throw new GalimatiasParseException("IPv4 address is too long", i);
            }
            addr[addrIdx] = (byte) value;
            addrIdx++;
            dotsSeen++;
            i++;
        }
        if (dotsSeen != 4) {
            throw new GalimatiasParseException("Malformed IPv4 address");
        }
        return new IPv4Address(addr);
    }

    public Inet4Address toInetAddress() throws UnknownHostException {
        return (Inet4Address) Inet4Address.getByAddress(getBytes());
    }

    private byte[] getBytes() {
        return new byte[] {
                (byte) (address & 0xFF),
                (byte) (address >> 8 & 0xFF),
                (byte) (address >> 16 & 0xFF),
                (byte) (address >> 24 & 0xFF)
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
