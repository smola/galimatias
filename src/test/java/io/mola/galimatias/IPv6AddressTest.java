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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IPv6AddressTest {

    private static final String[][] TEST_ADDRESSES = new String[][] {
            new String[] { "fedc:ba98:7654:3210:fedc:ba98:7654:3210" },
            new String[] { "FEDC:BA98:7654:3210:FEDC:BA98:7654:3211", "fedc:ba98:7654:3210:fedc:ba98:7654:3211" },
            new String[] { "2001:0db8:85a3:0000:0000:8a2e:0370:7334", "2001:db8:85a3::8a2e:370:7334" },
            new String[] { "2001:db8:85a3::8a2e:370:7334", "2001:db8:85a3::8a2e:370:7334" },
            new String[] { "0:0:0:0:0:0:0:1", "::1" },
            new String[] { "0:0:0:0:0:0:0:0", "::" },
            new String[] { "::1" },
            new String[] { "::" },
            new String[] { "::ffff:192.0.2.128", "::ffff:c000:280" }, //XXX: "::ffff:192.0.2.128" },
            new String[] { "::192.0.2.128", "::c000:280" } //XXX: Are we serializing IPv4-mapped addresses? "::192.0.2.128" }
    };

    private static Stream<Arguments> testAddresses() {
        return Arrays.stream(TEST_ADDRESSES)
                .map((arr) -> (arr.length == 2)? arr : new String[]{arr[0], arr[0]})
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("testAddresses")
    void parseIPv6Address(final String origin, final String target) throws GalimatiasParseException {
            final IPv6Address address = IPv6Address.parseIPv6Address(origin);
            assertNotNull(address);
            assertEquals(target, address.toString());
    }

    @ParameterizedTest
    @MethodSource("testAddresses")
    void equals(final String origin, final String result) throws GalimatiasParseException {
        final IPv6Address originAddr = IPv6Address.parseIPv6Address(origin);
        assertNotNull(originAddr);
        final IPv6Address resultAddr = IPv6Address.parseIPv6Address(result);
        assertNotNull(originAddr);
        assertAll("address",
                () -> assertEquals(resultAddr, originAddr),
                () -> assertEquals(originAddr, originAddr),
                () -> assertEquals(originAddr.hashCode(), resultAddr.hashCode()),
                () -> assertNotEquals(originAddr, null),
                () -> assertNotEquals(originAddr, "foo"),
                () -> assertEquals(originAddr.toHumanString(), originAddr.toString()));
    }

    @Test
    void notEquals() throws GalimatiasParseException {
        assertNotEquals(
                IPv6Address.parseIPv6Address(TEST_ADDRESSES[1][0]),
                IPv6Address.parseIPv6Address(TEST_ADDRESSES[0][0]));
    }

    @Test
    void parseInvalidPrefix() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address(":1"));
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address(":"));
    }

    @Test
    void parseNullAddress() {
        assertThrows(NullPointerException.class, () -> IPv6Address.parseIPv6Address(null));
    }

    @Test
    void parseEmptyAddress() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address(""));
    }

    @Test
    void parseIllegalCharacter() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("1::x:1"));
    }


    @Test
    void parseTooLongAddress() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("0:0:0:0:0:0:0:1:2"));
    }

    @Test
    void parseAddressWithFinalColon() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("0:0:0:0:0:0:0:1:"));
    }

    @Test
    void parseTooLongIPv4MappedAddress() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("0:0:0:0:0:0:0:192.168.1.1"));
    }

    @Test
    void parseTooShortAddress() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("0:0:0:0:0:1"));
    }

    @Test
    void parseDoubleCompressedAddress() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("1::2::3"));
    }

    @Test
    void parseTooLongIPv4Mapped() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("::192.168.1.1.5"));
    }

    @Test
    void parseIPv4MappedWithLeadingZero() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("::192.168.1.1.05"));
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("::192.168.1.1.00"));
    }

    @Test
    void parseMalformedIPv4Mapped() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("::192.168.1a.1"));
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("::192.168.a1.1"));
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("::.192.168.1.1"));
    }

    @Test
    void parseHighValueIPv4Mapped() {
        assertThrows(GalimatiasParseException.class, () -> IPv6Address.parseIPv6Address("::192.168.1.256"));
    }

    @ParameterizedTest
    @MethodSource("testAddresses")
    void toFromInetAddress(final String origin, final String discard) throws UnknownHostException, GalimatiasParseException {
            final InetAddress target = InetAddress.getByName(origin);
            final IPv6Address address = IPv6Address.parseIPv6Address(origin);
            assertEquals(target, address.toInetAddress());

            //FIXME: We currently do not support getting Inet4Address here
            //       (such as an IPv6-mapped IPv4 address).
            if (target instanceof Inet6Address) {
                assertEquals(address, IPv6Address.fromInet6Address((Inet6Address) target));
            }
    }

}
