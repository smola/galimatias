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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.stream.Stream;

class IPv4AddressTest {

    private static final String[][] TEST_ADDRESSES = new String[][] {
        new String[] { "0.0.0.0" },
        new String[] { "255.255.255.255" },
        new String[] { "127.0.0.1" },
        //TODO: new String[] { "192.0x00A80001", "192.168.0.1" },
        //TODO: new String[] { "1.1.1.1.", "1.1.1.1" },
        //TODO: new String[] { "1.1.1", "1.1.0.1" },
        //TODO: new String[] { "4294967295", "255.255.255.255" }
    };

    private static Stream<Arguments> testAddresses() {
        return Arrays.stream(TEST_ADDRESSES)
                .map((arr) -> (arr.length == 2)? arr : new String[]{arr[0], arr[0]})
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("testAddresses")
    void parseIPv4Address(final String origin, final String target) throws GalimatiasParseException {
        assertEquals(target, IPv4Address.parseIPv4Address(origin).toString());
    }

    @Test
    void equals() throws GalimatiasParseException {
        final IPv4Address ip = IPv4Address.parseIPv4Address("127.0.0.1");
        assertNotNull(ip);
        assertEquals(ip, ip);
        assertEquals(ip, IPv4Address.parseIPv4Address("127.0.0.1"));
        assertNotEquals(ip, IPv4Address.parseIPv4Address("127.0.0.2"));
        assertNotEquals(ip,"foo");
        assertEquals(ip.toHumanString(), ip.toString());
    }

    @Test
    void parseNullAddress() {
        assertThrows(NullPointerException.class, () -> IPv4Address.parseIPv4Address(null));
    }

    @Test
    void parseEmptyAddress() {
        assertThrows(GalimatiasParseException.class, () -> IPv4Address.parseIPv4Address(""));
    }

    @Test
    void parseIllegalCharacter() throws GalimatiasParseException {
        assertThrows(GalimatiasParseException.class, () -> IPv4Address.parseIPv4Address("1.1.x.1"));
    }


    @Test
    void parseTooLongAddress() {
        assertThrows(GalimatiasParseException.class, () -> IPv4Address.parseIPv4Address("1.1.1.1.2"));
    }

    @Test
    void parseAddressWithFinalDot() {
        assertThrows(GalimatiasParseException.class, () -> IPv4Address.parseIPv4Address("1.1.1.1."));
    }

    @Test
    void parseWithLeadingZero1() {
        assertThrows(GalimatiasParseException.class, () -> IPv4Address.parseIPv4Address("192.168.1.1.05"));
        assertThrows(GalimatiasParseException.class, () -> IPv4Address.parseIPv4Address("192.168.1.1.00"));
    }

    @Test
    void parseHighValueIPv4Mapped() {
        assertThrows(GalimatiasParseException.class, () -> IPv4Address.parseIPv4Address("192.168.1.256"));
    }

    @ParameterizedTest
    @MethodSource("testAddresses")
    void toFromInetAddress(final String origin, final String target) throws UnknownHostException, GalimatiasParseException {
        // Java standard library does not support trailing dots in IPv4
        if (origin.endsWith(".")) {
            return;
        }

        final InetAddress stdAddress = InetAddress.getByName(origin);
        final IPv4Address address = IPv4Address.parseIPv4Address(origin);
        assertEquals(stdAddress, address.toInetAddress());
        assertEquals(target, address.toInetAddress().getHostAddress());
        assertEquals(address, IPv4Address.fromInet4Adress((Inet4Address)stdAddress));
    }

}
