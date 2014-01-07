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

package io.mola.galimatias.cli;

import io.mola.galimatias.ErrorHandler;
import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import io.mola.galimatias.URLParsingSettings;

/**
 * A command line interface to Galimatias URL parser.
 */
public class CLI {

    private static void printError(GalimatiasParseException error) {
        System.out.println("\t\tError: " + error.getMessage());
        if (error.getPosition() != -1) {
            System.out.println("\t\tPosition: " + error.getPosition());
        }
    }

    private static void printResult(URL url) {
        System.out.println("\tResult:");
        System.out.println("\t\tURL: " + url.toString());
        System.out.println("\t\tURL type: " + ((url.isHierarchical())? "hierarchical" : "opaque"));
        System.out.println("\t\tScheme: " + url.scheme());
        if (url.schemeData() != null) {
            System.out.println("\t\tScheme data: " + url.schemeData());
        }
        if (url.username() != null) {
            System.out.println("\t\tUsername: " + url.username());
        }
        if (url.password() != null) {
            System.out.println("\t\tPassword: " + url.password());
        }
        if (url.host() != null) {
            System.out.println("\t\tHost: " + url.host());
        }
        if (url.port() != -1) {
            System.out.println("\t\tPort: " + url.port());
        }
        if (url.path() != null) {
            System.out.println("\t\tPath: " + url.path());
        }
        if (url.query() != null) {
            System.out.println("\t\tQuery: " + url.query());
        }
        if (url.fragment() != null) {
            System.out.println("\t\tFragment: " + url.fragment());
        }
    }

    private static class PrintErrorHandler implements ErrorHandler {

        @Override
        public void error(GalimatiasParseException error) throws GalimatiasParseException {
            System.out.println("\tRecoverable error found;");
            System.out.println("\t\tError: " + error.getMessage());
            if (error.getPosition() != -1) {
                System.out.println("\t\tPosition: " + error.getPosition());
            }
        }

        @Override
        public void fatalError(GalimatiasParseException error) {

        }
    }

    private static ErrorHandler errorHandler = new PrintErrorHandler();

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Need a URL as input");
            System.exit(1);
        }

        final String input = args[0];

        System.out.println("Analyzing URL: " + input);

        URLParsingSettings settings = URLParsingSettings.create().withErrorHandler(errorHandler);
        URL url;
        String whatwgUrlSerialized = "";
        String rfc3986UrlSerialized = "";

        boolean parseErrors;

        try {
            System.out.println("Parsing with WHATWG rules...");
            settings = settings.withStandard(URLParsingSettings.Standard.WHATWG);
            url = URL.parse(settings, input);
            whatwgUrlSerialized = url.toString();
            printResult(url);
        } catch (GalimatiasParseException ex) {
            System.out.println("Parsing with WHATWG rules resulted in fatal error");
            printError(ex);
        }

        try {
            System.out.println("Parsing with RFC 3986 rules...");
            settings = settings.withStandard(URLParsingSettings.Standard.RFC_3986);
            url = URL.parse(settings, input);
            rfc3986UrlSerialized = url.toString();
            if (whatwgUrlSerialized.equals(url.toString())) {
                System.out.println("\tResult identical to WHATWG rules");
            } else {
                printResult(url);
            }
        } catch (GalimatiasParseException ex) {
            System.out.println("Parsing with RFC 3986 rules resulted in fatal error");
            printError(ex);
        }

        try {
            System.out.println("Parsing with RFC 2396 rules...");
            settings = settings.withStandard(URLParsingSettings.Standard.RFC_2396);
            url = URL.parse(settings, input);
            if (rfc3986UrlSerialized.equals(url.toString())) {
                System.out.println("\tResult identical to RFC 3986 rules");
            } else {
                printResult(url);
            }
        } catch (GalimatiasParseException ex) {
            System.out.println("Parsing with RFC 2396 rules resulted in fatal error");
            printError(ex);
        }

    }

}
