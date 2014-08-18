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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads URL tests in the W3C format.
 *
 * See https://raw.github.com/w3c/web-platform-tests/master/url/urltestdata.txt
 */
public class TestURLLoader {

    private static Map<Character, Character> tokenMap = new HashMap<Character, Character>() {{
        put('\\', '\\');
        put('n', '\n');
        put('r', '\r');
        put('s', ' ');
        put('t', '\t');
        put('f', '\f');
    }};

    private static String normalize(final String input) {
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
                    throw new RuntimeException("Token error");
                }
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

    public static List<TestURL> loadTestURLs(final String resource) {
        try {
            final List<TestURL> results = new ArrayList<TestURL>();
            final BufferedReader br;
            br = new BufferedReader(new InputStreamReader(TestURLLoader.class.getResourceAsStream(resource)));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                String[] fields = line.split(" ");

                TestURL testURL = new TestURL();
                results.add(testURL);
                testURL.rawURL = normalize(fields[0]);

                if (fields.length > 1 && !fields[1].isEmpty()) {
                    testURL.rawBaseURL = normalize(fields[1]);
                    try {
                        testURL.parsedBaseURL = URL.parse(testURL.rawBaseURL);
                    } catch (GalimatiasParseException ex) {
                        throw new RuntimeException("Exception while parsing test line: " + line, ex);
                    }
                } else {
                    //FIXME: This implies there are no tests with null base
                    testURL.rawBaseURL = results.get(results.size() - 2).rawBaseURL;
                    testURL.parsedBaseURL = results.get(results.size() - 2).parsedBaseURL;
                }

                if (fields.length < 2) {
                    continue;
                }

                String scheme = "";
                String schemeData = "";
                String username = "";
                String password = null;
                Host host = null;
                int port = -1;
                String path = null;
                String query = null;
                String fragment = null;
                boolean isHierarchical = false;

                for (int i = 2; i < fields.length; i++) {
                    final String field = normalize(fields[i]);
                    if (field.length() < 2) {
                        throw new RuntimeException("Malformed test: " + line);
                    }
                    final String value = field.substring(2);
                    if (field.startsWith("s:")) {
                        scheme = value;
                        isHierarchical = URLUtils.isRelativeScheme(scheme);
                    } else if (field.startsWith("u:")) {
                        username = value;
                    } else if (field.startsWith("pass:")) {
                        password = field.substring(5);
                    } else if (field.startsWith("h:")) {
                        try {
                            host = Host.parseHost(value);
                        } catch (GalimatiasParseException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else if  (field.startsWith("port:")) {
                        port = Integer.parseInt(field.substring(5));
                    } else if (field.startsWith("p:")) {
                        path = value;
                    } else if (field.startsWith("q:")) {
                        query = value;
                        //FIXME: Workaround for bad test data
                        //TODO: https://github.com/w3c/web-platform-tests/issues/499
                        if (query.startsWith("?")) {
                            query = query.substring(1);
                        }
                    } else if (field.startsWith("f:")) {
                        fragment = value;
                        //FIXME: Workaround for bad test data
                        if (fragment.startsWith("#")) {
                            fragment = fragment.substring(1);
                        }
                    }
                }

                if (!isHierarchical) {
                    schemeData = path;
                    path = null;
                } else {
                    final String defaultPortString = URLUtils.getDefaultPortForScheme(scheme);
                    if (defaultPortString != null && port == Integer.parseInt(defaultPortString)) {
                        port = -1;
                    }
                }

                testURL.parsedURL = new URL(scheme, schemeData, username, password,
                        host, port, path, query, fragment, isHierarchical);

            }
            return results;
        } catch (IOException ex) {
            throw  new RuntimeException(ex);
        }
    }

}
