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

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import org.junit.Test;

import java.net.*;

public class Benchmark extends AbstractBenchmark {

    private static String testURL = "https://www.google.es/search?q=complex+URL&oq=complex+URL&aqs=chrome..69i57j0l3.1875j0j4&sourceid=chrome&ie=UTF-8";

    private static final int COUNT = 5000;

    @Test
    public void benchParseURL() throws MalformedURLException{
        for (int i = 0; i < COUNT; i++) {
            new URLParser().parse(testURL);
        }
    }

    @Test
    public void benchParseURL_URI() throws URISyntaxException {
        for (int i = 0; i < COUNT; i++) {
            new URI(testURL);
        }
    }

    @Test
    public void benchParseURL_URL() throws MalformedURLException {
        for (int i = 0; i < COUNT; i++) {
            new java.net.URL(testURL);
        }
    }

}
