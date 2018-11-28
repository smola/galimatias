/**
 * Copyright (c) 2018 Santiago M. Mola <santi@mola.io>
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class URLTestData {

    public final String input;
    public final String base;
    public final String href;
    public final String origin;
    public final String protocol;
    public final String username;
    public final String password;
    public final String host;
    public final String hostname;
    public final String port;
    public final String pathname;
    public final String search;
    public final String hash;
    public final boolean failure;

    @JsonCreator
    public URLTestData(
            @JsonProperty("input") final String input,
            @JsonProperty("base") final String base,
            @JsonProperty("href") final String href,
            @JsonProperty("origin") final String origin,
            @JsonProperty("protocol") final String protocol,
            @JsonProperty("username") final String username,
            @JsonProperty("password") final String password,
            @JsonProperty("host") final String host,
            @JsonProperty("hostname") final String hostname,
            @JsonProperty("port") final String port,
            @JsonProperty("pathname") final String pathname,
            @JsonProperty("search") final String search,
            @JsonProperty("searchParams") final String searchParams,
            @JsonProperty("hash") final String hash,
            @JsonProperty("failure") final boolean failure
    ) {
        this.input = input.replaceAll("\\t", "\t");
        this.base = base;
        this.href = href;
        this.origin = origin;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.hostname = hostname;
        this.port = port;
        this.pathname = pathname;
        this.search = search;
        this.hash = hash;
        this.failure = failure;
    }

    public String scheme() {
        if (protocol.isEmpty()) {
            return protocol;
        }
        return protocol.substring(0, protocol.length() - 1);
    }

    @Override
    public String toString() {
        return base + " " + input;
    }

    public static Stream<URLTestData> cases() throws IOException {
        try (final InputStream in = URLTestData.class.getResourceAsStream("/data/urltestdata.json")) {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(in);
            return StreamSupport
                    .stream(root.spliterator(), false)
                    .filter((n) -> !n.isTextual())
                    .map((n) -> {
                        try {
                            return mapper.treeToValue(n, URLTestData.class);
                        } catch (JsonProcessingException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
    }
}
