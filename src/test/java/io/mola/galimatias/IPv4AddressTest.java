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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class IPv4AddressTest {

    private static Logger log = LoggerFactory.getLogger(IPv4AddressTest.class);

    private static final String[] TEST_ADDRESSES = new String[] {
        "0.0.0.0",
        "255.255.255.255",
        "127.0.0.1"
    };

    @Test
    public void parseIPv4Address() throws GalimatiasParseException {
        for (final String testAddress : TEST_ADDRESSES) {
            log.debug("TESTING: {}", testAddress);
            assertThat(IPv4Address.parseIPv4Address(testAddress).toString()).isEqualTo(testAddress);
        }
    }

    @Test
    public void equals() throws GalimatiasParseException {
        final IPv4Address ip = IPv4Address.parseIPv4Address("127.0.0.1");
        assertThat(ip).isEqualTo(ip);
        assertThat(ip).isEqualTo(IPv4Address.parseIPv4Address("127.0.0.1"));
        assertThat(ip).isNotEqualTo(IPv4Address.parseIPv4Address("127.0.0.2"));
        assertThat(ip).isNotEqualTo("foo");
        assertThat(ip).isNotEqualTo(null);
        assertThat(ip.toHumanString()).isEqualTo(ip.toString());
    }

    @Test(expected = NullPointerException.class)
    public void parseNullAddress() throws GalimatiasParseException {
        IPv4Address.parseIPv4Address(null);
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseEmptyAddress() throws GalimatiasParseException {
        IPv4Address.parseIPv4Address("");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseIllegalCharacter() throws GalimatiasParseException {
        IPv4Address.parseIPv4Address("1.1.x.1");
    }


    @Test(expected = GalimatiasParseException.class)
    public void parseTooLongAddress() throws GalimatiasParseException {
        IPv4Address.parseIPv4Address("1.1.1.1.2");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseAddressWithFinalDot() throws GalimatiasParseException {
        IPv4Address.parseIPv4Address("1.1.1.1.");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseWithLeadingZero1() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("192.168.1.1.05");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseWithLeadingZero2() throws GalimatiasParseException {
        IPv6Address.parseIPv6Address("192.168.1.1.00");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseTooShortAddress() throws GalimatiasParseException {
        IPv4Address.parseIPv4Address("1.1.1");
    }

    @Test(expected = GalimatiasParseException.class)
    public void parseHighValueIPv4Mapped() throws GalimatiasParseException {
        IPv4Address.parseIPv4Address("192.168.1.256");
    }

    @Test
    public void toFromInetAddress() throws UnknownHostException, GalimatiasParseException {
        for (final String testAddress : TEST_ADDRESSES) {
            log.debug("TESTING: {}", testAddress);
            final InetAddress target = InetAddress.getByName(testAddress);
            final IPv4Address address = IPv4Address.parseIPv4Address(testAddress);
            assertThat(address.toInetAddress()).isEqualTo(target);
            assertThat(address.toInetAddress().getHostAddress()).isEqualToIgnoringCase(testAddress);
            assertThat(IPv4Address.fromInet4Adress((Inet4Address)target)).isEqualTo(address);
        }
    }

}
