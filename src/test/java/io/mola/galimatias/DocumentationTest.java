/*
 * Copyright (c) 2014 Santiago M. Mola <santi@mola.io>
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

import io.mola.galimatias.canonicalize.DecodeUnreservedCanonicalizer;
import io.mola.galimatias.canonicalize.URLCanonicalizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.fest.assertions.Assertions.*;

/**
 * Example snippets for documentation.
 */
@RunWith(JUnit4.class)
public class DocumentationTest {

    @Test
    public void parseUrl() {
        // START SNIPPET: basic-example
        String urlString = "http://galimatias.mola.io/my/path.html?q=search#section";
        URL url = null;
        try {
            url = URL.parse(urlString);
        } catch (GalimatiasParseException ex) {
            // Do something with a non-recoverable parsing error
        }
        System.out.println(String.format(
                "SCHEME: %s | HOST: %s | PORT: %d | PATH: %s | QUERY: %s | FRAGMENT: %s",
                url.scheme(), url.host(), url.port(), url.path(), url.query(), url.fragment()
        ));
        // OUTPUT: SCHEME: http | HOST: galimatias.mola.io | PORT: 80 | PATH: /my/path.html | QUERY: q=search | FRAGMENT: section
        // END SNIPPET: basic-example
        assertThat(String.format(
                "SCHEME: %s | HOST: %s | PORT: %d | PATH: %s | QUERY: %s | FRAGMENT: %s",
                url.scheme(), url.host(), url.port(), url.path(), url.query(), url.fragment()
        )).isEqualTo("SCHEME: %s | HOST: %s | PORT: %d | PATH: %s | QUERY: %s | FRAGMENT: %s");
    }

    @Test
    public void getting_started_parsing_url() {
        // START SNIPPET: parsing-url-1
        // Let's get a funky URL
        String urlString = " http:/日本.jp:80//.././[ FÜNKY ] ";

        // Parse it
        URL url = null;
        try {
            URL.parse(urlString);
        } catch (GalimatiasParseException ex) {
            // Do something if there is an unrecoverable error
        }

        System.out.println(url);
        // OUTPUT: http://xn--wgv71a.jp/[%20F%C3%9CNKY%20]
        // END SNIPPET: parsing-url-1
        assertThat(url.toString()).isEqualTo("http://xn--wgv71a.jp/[%20F%C3%9CNKY%20]");
    }

    @Test
    public void blog_post_1() throws GalimatiasParseException {
        // START SNIPPET: blog_posted_teaser_1
        // Let's get a funky URL
        String urlString = " http:/日本.jp:80//.././[ FÜNKY ] ";

        // Parse it
        URL url = URL.parse(urlString);

        System.out.println(url);
        // OUTPUT: http://xn--wgv71a.jp/[%20F%C3%9CNKY%20]

        System.out.println(url.toHumanString());
        // OUTPUT: http://日本.jp/[ FÜNKY ]


        // URLs can be modified with a fluent API
        URL modifiedURL = url.withQuery(" let's do some query about 日本 ").withFragment(" and a fragment");

        System.out.println(modifiedURL);
        // OUTPUT: http://xn--wgv71a.jp/[%20F%C3%9CNKY%20]?let's%20do%20some%20query%20about%20%E6%97%A5%E6%9C%AC#and a fragment

        System.out.println(modifiedURL.toHumanString());
        // OUTPUT: http://日本.jp/[ FÜNKY ]?let's do some query about 日本#and a fragment


        // And there are convenient canonicalizers to get URLs to a standard form

        String differentUrlString = "http:/日本.jp/[20%46%c3%9c%4e%4B%59%20]";
        URL differentURL = URL.parse(differentUrlString);
        System.out.println(differentURL);
        // OUTPUT: http://xn--wgv71a.jp/[%20%46%C3%9C%4E%4B%59%20]

        URLCanonicalizer canonicalizer = new DecodeUnreservedCanonicalizer();
        URL canonicalizedURL = canonicalizer.canonicalize(differentURL);
        System.out.println(canonicalizedURL.toString());
        // OUTPUT: http://xn--wgv71a.jp/[%20%46%C3%9C%4E%4B%59%20]
        System.out.println(canonicalizedURL.toHumanString());
        // OUTPUT: http://日本.jp/[ FÜNKY ]
        // END SNIPPET: blog_posted_teaser_1
    }
}