/**
 * Copyright (c) 2013-2014, 2018 Santiago M. Mola <santi@mola.io>
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class URLTest {

    @Test
    void testBuildHierarchical() throws GalimatiasParseException {
        final URL url = URL.buildHierarchical("http", "example.org");
        assertEquals("http", url.scheme());
        assertEquals(Optional.of("example.org"), url.host().map(Host::toHostString));
    }

    @Test
    void testEquals() throws GalimatiasParseException {
        final URL url = URL.buildHierarchical("http", "example.org");
        assertEquals(url, url);
    }

    @Test
    void testSchemeHierarchical() throws GalimatiasParseException {
        final URL url = URL.buildHierarchical("http", "example.org");
        assertTrue(url.isHierarchical());
        assertEquals("http", url.scheme());
        assertEquals("https", url.withScheme("https").scheme());
    }

    @Test
    void testSchemeOpaque() throws GalimatiasParseException {
        final URL url = URL.buildOpaque("about");
        assertFalse(url.isHierarchical());
        assertEquals("about", url.scheme());
        assertEquals("other", url.withScheme("other").scheme());

    }

    @Test
    void testPath() throws GalimatiasParseException {
        final URL url = URL.buildHierarchical("http", "example.org");
        assertEquals("/path", url.withPath("path").path());
        assertEquals("/path", url.withPath("/path").path());
        assertEquals("/path", url.withFragment("#fragment").withPath("/path").path());
    }

    @Test
    void testQuery() throws GalimatiasParseException {
        final URL url = URL.buildHierarchical("http", "example.org");
        assertEquals("?query", url.withQuery("query").query());
        assertEquals("?query", url.withQuery("?query").query());
        assertEquals("?query", url.withQuery("?query").withQuery("?query").query());
        assertEquals("?query", url.withQuery("?query").withQuery("query").query());
    }

    @Test
    void testFragment() throws GalimatiasParseException {
        final URL url = URL.buildHierarchical("http", "example.org");
        assertEquals("#fragment", url.withFragment("fragment").fragment());
        assertEquals("#fragment", url.withFragment("#fragment").fragment());
        assertEquals("#fragment", url.withFragment("#fragment").withFragment("#fragment").fragment());
        assertEquals("#fragment", url.withFragment("#fragment").withFragment("fragment").fragment());
        assertEquals("#fragment", url.withPath("/path").withFragment("#fragment").fragment());
        assertEquals("#fragment", url.withFragment("#fragment").withPath("/path").fragment());
    }
}
