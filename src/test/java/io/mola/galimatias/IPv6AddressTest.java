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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class IPv6AddressTest {

    private static Logger log = LoggerFactory.getLogger(IPv6AddressTest.class);

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

    @Test
    public void parseIPv6Address() throws GalimatiasParseException {
        for (final String[] testAddress : TEST_ADDRESSES) {
            final String origin = testAddress[0];
            log.debug("TESTING: {}", origin);
            final String target = (testAddress.length > 1)? testAddress[1] : testAddress[0];
            final IPv6Address address = IPv6Address.parseIPv6Address(origin);
            assertThat(address.toString()).isEqualTo(target);
        }
    }

    @Test
    public void equals() throws GalimatiasParseException {
        for (final String[] testAddress : TEST_ADDRESSES) {
            final IPv6Address original = IPv6Address.parseIPv6Address(testAddress[0]);
            final IPv6Address result = IPv6Address.parseIPv6Address(testAddress[testAddress.length > 1? 1 : 0]);
            assertThat(original).isEqualTo(original);
            assertThat(original).isEqualTo(result);
            assertThat(original.hashCode()).isEqualTo(result.hashCode());
            assertThat(original).isNotEqualTo(null);
            assertThat(original).isNotEqualTo("foo");
            assertThat(original.toHumanString()).isEqualTo(original.toString());
        }
        assertThat(IPv6Address.parseIPv6Address(TEST_ADDRESSES[0][0]))
                .isNotEqualTo(IPv6Address.parseIPv6Address(TEST_ADDRESSES[1][0]));

    }

    @Test(expected = GalimatiasParseException.class)
    public void parseInvalidPrefix1() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address(":1");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseInvalidPrefix2() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address(":");
    }

    @Test(expected = NullPointerException.class)
    public void parseNullAddress() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address(null);
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseEmptyAddress() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseIllegalCharacter() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("1::x:1");
    }


    @Test(expected = GalimatiasParseException.class)
    public void parseTooLongAddress() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("0:0:0:0:0:0:0:1:2");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseAddressWithFinalColon() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("0:0:0:0:0:0:0:1:");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseTooLongIPv4MappedAddress() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("0:0:0:0:0:0:0:192.168.1.1");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseTooShortAddress() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("0:0:0:0:0:1");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseDoubleCompressedAddress() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("1::2::3");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseTooLongIPv4Mapped() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("::192.168.1.1.5");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseIPv4MappedWithLeadingZero1() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("::192.168.1.1.05");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseIPv4MappedWithLeadingZero2() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("::192.168.1.1.00");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseMalformedIPv4Mapped() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("::192.168.1a.1");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseMalformedIPv4Mapped2() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("::192.168.a1.1");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseMalformedIPv4Mapped3() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("::.192.168.1.1");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseHighValueIPv4Mapped() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("::192.168.1.256");
    }

    @Test
    public void toFromInetAddress() throws UnknownHostException, GalimatiasParseException {
        for (final String[] testAddress : TEST_ADDRESSES) {
            final String origin = testAddress[0];
            log.debug("TESTING: {}", origin);
            final InetAddress target = InetAddress.getByName(origin);
            final IPv6Address address = IPv6Address.parseIPv6Address(origin);
            assertThat(address.toInetAddress()).isEqualTo(target);

            //FIXME: We currently do not support getting Inet4Address here
            //       (such as an IPv6-mapped IPv4 address).
            if (target instanceof Inet6Address) {
                assertThat(IPv6Address.fromInet6Address((Inet6Address) target)).isEqualTo(address);
            }
        }
    }

}
