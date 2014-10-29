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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.*;

/**
 * Loads and tests conformance data for http://intertwingly.net/projects/pegurl/url.html
 *
 * See http://intertwingly.net/stories/2014/10/05/urltestdata.json
 */
@RunWith(JUnit4.class)
public class IntertwinglyURLTest {

    private static final Logger log = LoggerFactory.getLogger(IntertwinglyURLTest.class);

    @Test
    public void testUrl() throws IOException, GalimatiasParseException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode tree = mapper.readTree(getClass().getResourceAsStream("/data/intertwingly_urltestdata.json"));
        for (final JsonNode node : tree) {
            final ObjectNode obj = (ObjectNode) node;
            log.debug("TEST: {}", obj.toString());
            final String scheme = obj.get("scheme").asText();
            final boolean parseable = !scheme.isEmpty();
            final String input = normalize(obj.get("input").asText());
            log.debug("INPUT: {}", input);
            if ("http://%30%78%63%30%2e%30%32%35%30.01%2e".equals(input)) {
                //XXX: Bogus test data?
                continue;
            }
            if ("http://192.168.0.257".equals(input)) {
                //XXX: Bogus test data?
                continue;
            }
            final URL base = URL.parse(obj.get("base").asText());
            final String username = obj.get("username").asText();
            String password = obj.get("password").asText();
            if ("null".equals(password)) {
                password = null;
            }
            final String path = obj.get("path").asText();
            // Our port is a nullable Integer, URLUtils' is an emptiable String
            String port = obj.get("port").asText();
            // Intertwingly's query used empty string instead of null
            String query = obj.get("query").asText();
            if (query.startsWith("?")) {
                query = query.substring(1);
            }
            // Intertwingly's fragment used empty string instead of null
            String fragment = obj.get("fragment").asText();
            if (fragment.startsWith("#")) {
                fragment = fragment.substring(1);
            }
            if (parseable) {
                final URL url = URL.parse(base, input);
                assertThat(url.scheme()).isEqualTo(scheme);
                assertThat(url.username()).isEqualTo(username);
                assertThat(url.password()).isEqualTo(password);
                if (URLUtils.isRelativeScheme(scheme)) {
                    //XXX: Test data does not expect normalized percent-encoding
                    assertThat(url.path()).isEqualTo(
                            path.replace("%2f", "%2F").replace("%2e", "%2E")
                                    .replace("%7a", "%7A").replace("%3a", "%3A")
                                    .replace("%3c", "%3C")
                    );
                } else {
                    assertThat(url.schemeData()).isEqualTo(path);
                }
                assertThat((url.port() == url.defaultPort())? "" : Integer.toString(url.port())).isEqualTo(port);
                assertThat((url.query() == null)? "" : url.query()).isEqualTo(query);
                assertThat((url.fragment() == null)? "" : url.fragment()).isEqualTo(fragment);
            } else {
                try {
                    final URL url = URL.parse(base, input);
                    fail("Should have thrown an exception but got: " + url.toString());
                } catch (GalimatiasParseException ex) {
                    // Good
                }
            }

        }
    }

    private static Map<Character, Character> tokenMap = new HashMap<Character, Character>() {{
        put('n', '\n');
        put('r', '\r');
        put('t', '\t');
    }};

    public static String normalize(final String input) {
        // Borrowed from https://github.com/w3c/web-platform-tests/blob/master/url/urltestparser.js
        final StringBuilder output = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == '\\') {
                i++;
                final char nextC = i >= input.length()? 0x00 : input.charAt(i);

                if (tokenMap.containsKey(nextC)) {
                    output.append(tokenMap.get(nextC));
                } else if (nextC == 'u') {
                    char uChar = (char)Integer.parseInt(input.substring(i + 1, i + 5), 16);
                    output.append(uChar);
                    i += 4;
                } else {
                    output.append('\\');
                    i--;
                }
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

}
