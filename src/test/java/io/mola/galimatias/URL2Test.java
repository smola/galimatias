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

import junit.framework.TestCase;

/**
 * URL tests imported from Android's libcore.java.net.URLTest.
 */
@SuppressWarnings("static-method")
public final class URL2Test extends TestCase {

    /* TODO
    public void testFileEqualsWithEmptyHost() throws Exception {
        assertEquals(new URL("file", "", -1, "/a/"), new URL("file:/a/"));
    }

    public void testHttpEqualsWithEmptyHost() throws Exception {
        assertEquals(new URL("http", "", 80, "/a/"), new URL("http:/a/"));
        assertFalse(new URL("http", "", 80, "/a/").equals(new URL("http://host/a/")));
    }

    public void testFileEquals() throws Exception {
        assertEquals(new URL("file", null, -1, "/a"), new URL("file", null, -1, "/a"));
        assertFalse(new URL("file", null, -1, "/a").equals(new URL("file", null, -1, "/A")));
    }

    public void testJarEquals() throws Exception {
        assertEquals(new URL("jar", null, -1, "/a!b"), new URL("jar", null, -1, "/a!b"));
        assertFalse(new URL("jar", null, -1, "/a!b").equals(new URL("jar", null, -1, "/a!B")));
        assertFalse(new URL("jar", null, -1, "/a!b").equals(new URL("jar", null, -1, "/A!b")));
    }

    public void testUrlSerialization() throws Exception {
        String s = "aced00057372000c6a6176612e6e65742e55524c962537361afce472030006490004706f72744c0"
                + "009617574686f726974797400124c6a6176612f6c616e672f537472696e673b4c000466696c65710"
                + "07e00014c0004686f737471007e00014c000870726f746f636f6c71007e00014c000372656671007"
                + "e00017870ffffffff74000e757365723a7061737340686f73747400102f706174682f66696c653f7"
                + "175657279740004686f7374740004687474707400046861736878";
        URL url = new URL("http://user:pass@host/path/file?query#hash");
        new SerializationTester<URL>(url, s).test();
    }

    */

    /**
     * The serialized form of a URL includes its hash code. But the hash code
     * is not documented. Check that we don't return a deserialized hash code
     * from a deserialized value.
     */
    /*TODO
    public void testUrlSerializationWithHashCode() throws Exception {
        String s = "aced00057372000c6a6176612e6e65742e55524c962537361afce47203000749000868617368436"
                + "f6465490004706f72744c0009617574686f726974797400124c6a6176612f6c616e672f537472696"
                + "e673b4c000466696c6571007e00014c0004686f737471007e00014c000870726f746f636f6c71007"
                + "e00014c000372656671007e00017870cdf0efacffffffff74000e757365723a7061737340686f737"
                + "47400102f706174682f66696c653f7175657279740004686f7374740004687474707400046861736"
                + "878";
        final URL url = new URL("http://user:pass@host/path/file?query#hash");
        new SerializationTester<URL>(url, s) {
            @Override protected void verify(URL deserialized) {
                assertEquals(url.hashCode(), deserialized.hashCode());
            }
        }.test();
    }

    public void testOnlySupportedProtocols() {
        try {
            new URL("abcd://host");
            fail();
        } catch (MalformedURLException expected) {
        }
    }

    */

    public void testOmittedHost() throws Exception {
        URL url = URL.parse("http:///path");
        assertEquals("path", url.host().toString()); // Android will parse this as ""
        assertEquals("/", url.file()); // Android will parse this as "/path"
        assertEquals("/", url.path());
    }

    public void testNoHost() throws Exception {
        URL url = URL.parse("http:/path");
        assertEquals("http", url.scheme());
        assertEquals("path", url.authority()); // Android will be null
        assertEquals("", url.userInfo()); // Android will be null
        assertEquals("path", url.host().toString()); // Android will be null
        assertEquals(80, url.port());
        assertEquals(80, url.defaultPort());
        assertEquals("/", url.file()); // Android will be "/path"
        assertEquals("/", url.path()); // Android will be "/path"
        assertEquals(null, url.query());
        assertEquals(null, url.fragment());
    }

    public void testNoPath() throws Exception {
        URL url = URL.parse("http://host");
        assertEquals("host", url.host().toString());
        assertEquals("/", url.file()); // Android will be ""
        assertEquals("/", url.path()); // Android will be ""
    }

    public void testEmptyHostAndNoPath() throws Exception {
        try {
            // This works on Android
            URL.parse("http://");
            fail();
        } catch (GalimatiasParseException expected) {}
    }

    public void testNoHostAndNoPath() throws Exception {
        try {
            // This works on Android
            URL.parse("http:");
            fail();
        } catch (GalimatiasParseException expected) { }
    }

    public void testAtSignInUserInfo() throws Exception {
        // This will fail on Android
        URL url = URL.parse("http://user@userhost.com:password@host");
        assertEquals("host", url.host().toString());
        assertEquals("user%40userhost.com:password", url.userInfo());

        URLParsingSettings settings = URLParsingSettings.create()
                .withErrorHandler(StrictErrorHandler.getInstance());
        try {
            URL.parse(settings, "http://user@userhost.com:password@host");
            fail();
        } catch (GalimatiasParseException expected) { }
    }

    public void testUserNoPassword() throws Exception {
        URL url = URL.parse("http://user@host");
        assertEquals("user@host", url.authority());
        assertEquals("user", url.userInfo());
        assertEquals("host", url.host().toString());
    }

    public void testUserNoPasswordExplicitPort() throws Exception {
        URL url = URL.parse("http://user@host:8080");
        assertEquals("user@host:8080", url.authority());
        assertEquals("user", url.userInfo());
        assertEquals("host", url.host().toString());
        assertEquals(8080, url.port());
    }

    public void testUserPasswordHostPort() throws Exception {
        URL url = URL.parse("http://user:password@host:8080");
        assertEquals("user:password@host:8080", url.authority());
        assertEquals("user:password", url.userInfo());
        assertEquals("host", url.host().toString());
        assertEquals(8080, url.port());
    }

    public void testUserPasswordEmptyHostPort() throws Exception {
        try {
            URL.parse("http://user:password@:8080");
        } catch (GalimatiasParseException expected) {}
    }

    public void testUserPasswordEmptyHostEmptyPort() throws Exception {
        try {
            // This works on Android
            URL.parse("http://user:password@");
            fail();
        } catch (GalimatiasParseException expected) {}
    }

    public void testPathOnly() throws Exception {
        URL url = URL.parse("http://host/path");
        assertEquals("/path", url.file());
        assertEquals("/path", url.path());
    }

    public void testQueryOnly() throws Exception {
        URL url = URL.parse("http://host?query");
        assertEquals("/?query", url.file());
        assertEquals("/", url.path());
        assertEquals("query", url.query());
    }

    public void testFragmentOnly() throws Exception {
        URL url = URL.parse("http://host#fragment");
        assertEquals("/", url.file());
        assertEquals("/", url.path());
        assertEquals("fragment", url.fragment());
    }

    public void testAtSignInPath() throws Exception {
        URL url = URL.parse("http://host/file@foo");
        assertEquals("/file@foo", url.file());
        assertEquals("/file@foo", url.path());
        assertEquals("", url.userInfo()); // Null on Android
    }

    public void testColonInPath() throws Exception {
        URL url = URL.parse("http://host/file:colon");
        assertEquals("/file:colon", url.file());
        assertEquals("/file:colon", url.path());
    }

    public void testSlashInQuery() throws Exception {
        URL url = URL.parse("http://host/file?query/path");
        assertEquals("/file?query/path", url.file());
        assertEquals("/file", url.path());
        assertEquals("query/path", url.query());
    }

    public void testQuestionMarkInQuery() throws Exception {
        URL url = URL.parse("http://host/file?query?another");
        assertEquals("/file?query?another", url.file());
        assertEquals("/file", url.path());
        assertEquals("query?another", url.query());
    }

    public void testAtSignInQuery() throws Exception {
        URL url = URL.parse("http://host/file?query@at");
        assertEquals("/file?query@at", url.file());
        assertEquals("/file", url.path());
        assertEquals("query@at", url.query());
    }

    public void testColonInQuery() throws Exception {
        URL url = URL.parse("http://host/file?query:colon");
        assertEquals("/file?query:colon", url.file());
        assertEquals("/file", url.path());
        assertEquals("query:colon", url.query());
    }

    public void testQuestionMarkInFragment() throws Exception {
        URL url = URL.parse("http://host/file#fragment?query");
        assertEquals("/file", url.file());
        assertEquals("/file", url.path());
        assertEquals(null, url.query());
        assertEquals("fragment?query", url.fragment());
    }

    public void testColonInFragment() throws Exception {
        URL url = URL.parse("http://host/file#fragment:80");
        assertEquals("/file", url.file());
        assertEquals("/file", url.path());
        assertEquals(80, url.port());
        assertEquals("fragment:80", url.fragment());
    }

    public void testSlashInFragment() throws Exception {
        URL url = URL.parse("http://host/file#fragment/path");
        assertEquals("/file", url.file());
        assertEquals("/file", url.path());
        assertEquals("fragment/path", url.fragment());
    }

    /* TODO
    public void testSlashInFragmentCombiningConstructor() throws Exception {
        URL url = new URL("http", "host", "/file#fragment/path");
        assertEquals("/file", url.getFile());
        assertEquals("/file", url.getPath());
        assertEquals("fragment/path", url.getRef());
    }
    */

    public void testHashInFragment() throws Exception {
        URL url = URL.parse("http://host/file#fragment#another");
        assertEquals("/file", url.file());
        assertEquals("/file", url.path());
        assertEquals("fragment#another", url.fragment());
    }

    public void testEmptyPort() throws Exception {
        URL url = URL.parse("http://host:/");
        assertEquals(80, url.port());
    }

    public void testNonNumericPort() throws Exception {
        try {
            URL.parse("http://host:x/");
            fail();
        } catch (GalimatiasParseException expected) {
        }
    }

    public void testNegativePort() throws Exception {
        try {
            URL.parse("http://host:-2/");
            fail();
        } catch (GalimatiasParseException expected) {
        }
    }

    public void testNegativePortEqualsPlaceholder() throws Exception {
        try {
            URL.parse("http://host:-1/");
            fail(); // RI fails this
        } catch (GalimatiasParseException expected) {
        }
    }

    public void testRelativePathOnQuery() throws Exception {
        URL base = URL.parse("http://host/file?query/x");
        URL url = URL.parse(base, "another");
        assertEquals("http://host/another", url.toString());
        assertEquals("/another", url.file());
        assertEquals("/another", url.path());
        assertEquals(null, url.query());
        assertEquals(null, url.fragment());
    }

    public void testRelativeFragmentOnQuery() throws Exception {
        URL base = URL.parse("http://host/file?query/x#fragment");
        URL url = URL.parse(base, "#another");
        assertEquals("http://host/file?query/x#another", url.toString());
        assertEquals("/file?query/x", url.file());
        assertEquals("/file", url.path());
        assertEquals("query/x", url.query());
        assertEquals("another", url.fragment());
    }

    public void testPathContainsRelativeParts() throws Exception {
        URL url = URL.parse("http://host/a/b/../c");
        assertEquals("http://host/a/c", url.toString()); // RI doesn't canonicalize
    }

    public void testRelativePathAndFragment() throws Exception {
        URL base = URL.parse("http://host/file");
        assertEquals("http://host/another#fragment", URL.parse(base, "another#fragment").toString());
    }

    public void testRelativeParentDirectory() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        assertEquals("http://host/a/d", URL.parse(base, "../d").toString());
    }

    public void testRelativeChildDirectory() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        assertEquals("http://host/a/b/d/e", URL.parse(base, "d/e").toString());
    }

    public void testRelativeRootDirectory() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        assertEquals("http://host/d", URL.parse(base, "/d").toString());
    }

    public void testRelativeFullUrl() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        assertEquals("http://host2/d/e", URL.parse(base, "http://host2/d/e").toString());
        assertEquals("https://host2/d/e", URL.parse(base, "https://host2/d/e").toString());
    }

    public void testRelativeDifferentScheme() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        assertEquals("https://host2/d/e", URL.parse(base, "https://host2/d/e").toString());
    }

    public void testRelativeDifferentAuthority() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        assertEquals("http://another/d/e", URL.parse(base, "//another/d/e").toString());
    }

    public void testRelativeWithScheme() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        assertEquals("http://host/a/b/c", URL.parse(base, "http:").toString());
        assertEquals("http://host/", URL.parse(base, "http:/").toString());
    }

    public void testRelativeFragmentOnly() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        assertEquals("http://host/a/b/c#", URL.parse(base, "#").toString());
    }

    public void testMalformedUrlsRefusedByFirefoxAndChrome() throws Exception {
        URL base = URL.parse("http://host/a/b/c");
        // All these are Ok in android, not in galimatias
        try {
            URL.parse(base, "http://"); // fails on RI and galimatias; path retained on Android
            fail();
        } catch (GalimatiasParseException expected) {

        }
        try {
            URL.parse(base, "//");
            fail();
        } catch (GalimatiasParseException expected) {

        }
        try {
            URL.parse(base, "https:");
            fail();
        } catch (GalimatiasParseException expected) {

        }
        try {
            URL.parse("https:/");
            fail();
        } catch (GalimatiasParseException expected) {

        }
        try {
            URL.parse("https:/");
            fail();
        } catch (GalimatiasParseException expected) {

        }
        try {
            URL.parse("https://");
            fail();
        } catch (GalimatiasParseException expected) {

        }
    }

    public void testRfc1808NormalExamples() throws Exception {
        URL base = URL.parse("http://a/b/c/d;p?q");
        assertEquals("https://h/", URL.parse(base, "https:h").toString()); // Android will parse "https:h"
        assertEquals("http://a/b/c/g", URL.parse(base, "g").toString());
        assertEquals("http://a/b/c/g", URL.parse(base, "./g").toString());
        assertEquals("http://a/b/c/g/", URL.parse(base, "g/").toString());
        assertEquals("http://a/g", URL.parse(base, "/g").toString());
        assertEquals("http://g/", URL.parse(base, "//g").toString()); // Android will parse "http://g"
        assertEquals("http://a/b/c/d;p?y", URL.parse(base, "?y").toString()); // RI fails; file lost
        assertEquals("http://a/b/c/g?y", URL.parse(base, "g?y").toString());
        assertEquals("http://a/b/c/d;p?q#s", URL.parse(base, "#s").toString());
        assertEquals("http://a/b/c/g#s", URL.parse(base, "g#s").toString());
        assertEquals("http://a/b/c/g?y#s", URL.parse(base, "g?y#s").toString());
        assertEquals("http://a/b/c/;x", URL.parse(base, ";x").toString());
        assertEquals("http://a/b/c/g;x", URL.parse(base, "g;x").toString());
        assertEquals("http://a/b/c/g;x?y#s", URL.parse(base, "g;x?y#s").toString());
        assertEquals("http://a/b/c/d;p?q", URL.parse(base, "").toString());
        assertEquals("http://a/b/c/", URL.parse(base, ".").toString());
        assertEquals("http://a/b/c/", URL.parse(base, "./").toString());
        assertEquals("http://a/b/", URL.parse(base, "..").toString());
        assertEquals("http://a/b/", URL.parse(base, "../").toString());
        assertEquals("http://a/b/g", URL.parse(base, "../g").toString());
        assertEquals("http://a/", URL.parse(base, "../..").toString());
        assertEquals("http://a/", URL.parse(base, "../../").toString());
        assertEquals("http://a/g", URL.parse(base, "../../g").toString());
    }

    public void testRfc1808AbnormalExampleTooManyDotDotSequences() throws Exception {
        URL base = URL.parse("http://a/b/c/d;p?q");
        assertEquals("http://a/g", URL.parse(base, "../../../g").toString()); // RI doesn't normalize
        assertEquals("http://a/g", URL.parse(base, "../../../../g").toString());
    }

    public void testRfc1808AbnormalExampleRemoveDotSegments() throws Exception {
        URL base = URL.parse("http://a/b/c/d;p?q");
        assertEquals("http://a/g", URL.parse(base, "/./g").toString()); // RI doesn't normalize
        assertEquals("http://a/g", URL.parse(base, "/../g").toString()); // RI doesn't normalize
        assertEquals("http://a/b/c/g.", URL.parse(base, "g.").toString());
        assertEquals("http://a/b/c/.g", URL.parse(base, ".g").toString());
        assertEquals("http://a/b/c/g..", URL.parse(base, "g..").toString());
        assertEquals("http://a/b/c/..g", URL.parse(base, "..g").toString());
    }

    public void testRfc1808AbnormalExampleNonsensicalDots() throws Exception {
        URL base = URL.parse("http://a/b/c/d;p?q");
        assertEquals("http://a/b/g", URL.parse(base, "./../g").toString());
        assertEquals("http://a/b/c/g/", URL.parse(base, "./g/.").toString());
        assertEquals("http://a/b/c/g/h", URL.parse(base, "g/./h").toString());
        assertEquals("http://a/b/c/h", URL.parse(base, "g/../h").toString());
        assertEquals("http://a/b/c/g;x=1/y", URL.parse(base, "g;x=1/./y").toString());
        assertEquals("http://a/b/c/y", URL.parse(base, "g;x=1/../y").toString());
    }

    public void testRfc1808AbnormalExampleRelativeScheme() throws Exception {
        URL base = URL.parse("http://a/b/c/d;p?q");
        // this result is permitted; strict parsers prefer "http:g"
        assertEquals("http://a/b/c/g", URL.parse(base, "http:g").toString());
    }

    public void testRfc1808AbnormalExampleQueryOrFragmentDots() throws Exception {
        URL base = URL.parse("http://a/b/c/d;p?q");
        assertEquals("http://a/b/c/g?y/./x", URL.parse(base, "g?y/./x").toString());
        assertEquals("http://a/b/c/g?y/../x", URL.parse(base, "g?y/../x").toString());
        assertEquals("http://a/b/c/g#s/./x", URL.parse(base, "g#s/./x").toString());
        assertEquals("http://a/b/c/g#s/../x", URL.parse(base, "g#s/../x").toString());
    }

    public void testSquareBracketsInUserInfo() throws Exception {
        URL url = URL.parse("http://user:[::1]@host");
        assertEquals("user:[::1]", url.userInfo());
        assertEquals("host", url.host().toString());
    }

    /*TODO
    public void testComposeUrl() throws Exception {
        URL url = new URL("http", "host", "a");
        assertEquals("http", url.getProtocol());
        assertEquals("host", url.getAuthority());
        assertEquals("host", url.getHost());
        assertEquals("/a", url.getFile()); // RI fails; doesn't insert '/' separator
        assertEquals("http://host/a", url.toString()); // fails on RI
    }

    public void testComposeUrlWithNullHost() throws Exception {
        URL url = new URL("http", null, "a");
        assertEquals("http", url.getProtocol());
        assertEquals(null, url.getAuthority());
        assertEquals(null, url.getHost());
        assertEquals("a", url.getFile());
        assertEquals("http:a", url.toString()); // fails on RI
    }

    */

    public void testFileUrlExtraLeadingSlashes() throws Exception {
        URL url = URL.parse("file:////foo");
        assertEquals(null, url.authority()); // RI and galimatias return null, Android returns ""
        assertEquals("//foo", url.path());
        assertEquals("file:////foo", url.toString());
    }

    public void testFileUrlWithAuthority() throws Exception {
        URL url = URL.parse("file://x/foo");
        assertEquals("x", url.authority());
        assertEquals("/foo", url.path());
        assertEquals("file://x/foo", url.toString());
    }

    /**
     * The RI is not self-consistent on missing authorities, returning either
     * null or the empty string depending on the number of slashes in the path.
     * We always treat '//' as the beginning of an authority.
     */
    public void testEmptyAuthority() throws Exception {
        URL url = URL.parse("http:///foo");
        assertEquals("foo", url.authority()); // Android will be ""
        assertEquals("/", url.path()); // Android will be "/foo"
        assertEquals("http://foo/", url.toString()); // RI drops '//', android will be "http:///foo"
    }

    public void testHttpUrlExtraLeadingSlashes() throws Exception {
        URL url = URL.parse("http:////foo");
        assertEquals("foo", url.authority()); // RI returns null, Android "//"
        assertEquals("/", url.path()); // Android returns "//foo"
        assertEquals("http://foo/", url.toString()); // Android returns "http:////foo"
    }

    public void testFileUrlRelativePath() throws Exception {
        URL base = URL.parse("file:a/b/c");
        assertEquals("file:///a/b/d", URL.parse(base, "d").toString()); // This is parsed to "file:a/b/d" on Android
    }

    public void testFileUrlDottedPath() throws Exception {
        URL url = URL.parse("file:../a/b");
        assertEquals("/a/b", url.path());  // Android will be "../a/b"
        assertEquals("file:///a/b", url.toString()); // Android will be "file:../a/b"
    }

    public void testParsingDotAsHostname() throws Exception {
        try {
            // This is valid on Android
            URL url = URL.parse("http://./");
            fail();
        } catch (GalimatiasParseException expected) { }
    }

    public void testSquareBracketsWithIPv4() throws Exception {
        try {
            URL.parse("http://[192.168.0.1]/");
            fail();
        } catch (GalimatiasParseException expected) {
        }
        /* TODO
        URL url = new URL("http", "[192.168.0.1]", "/");
        assertEquals("[192.168.0.1]", url.getHost());
        */
    }

    public void testSquareBracketsWithHostname() throws Exception {
        try {
            URL.parse("http://[www.android.com]/");
            fail();
        } catch (GalimatiasParseException expected) {
        }
        /*TODO
        URL url = new URL("http", "[www.android.com]", "/");
        assertEquals("[www.android.com]", url.getHost());
        */
    }

    public void testIPv6WithoutSquareBrackets() throws Exception {
        try {
            URL.parse("http://fe80::1234/");
            fail();
        } catch (GalimatiasParseException expected) {
        }
        /*TODO
        URL url = new URL("http", "fe80::1234", "/");
        assertEquals("[fe80::1234]", url.getHost());
        */
    }

    public void testIpv6WithSquareBrackets() throws Exception {
        URL url = URL.parse("http://[::1]:2/");
        assertEquals("::1", url.host().toString()); // returns [::1] on Android
        assertEquals(2, url.port());
    }

    public void testEqualityWithNoPath() throws Exception {
        //DIFF: Android returns true
        assertTrue(URL.parse("http://android.com").equals(URL.parse("http://android.com/")));
    }

    /*TODO
    public void testUrlDoesNotEncodeParts() throws Exception {
        URL url = new URL("http", "host", 80, "/doc|search?q=green robots#over 6\"");
        assertEquals("http", url.getProtocol());
        assertEquals("host:80", url.getAuthority());
        assertEquals("/doc|search", url.getPath());
        assertEquals("q=green robots", url.getQuery());
        assertEquals("over 6\"", url.getRef());
        assertEquals("http://host:80/doc|search?q=green robots#over 6\"", url.toString());
    }
    */

    public void testSchemeCaseIsCanonicalized() throws Exception {
        URL url = URL.parse("HTTP://host/path");
        assertEquals("http", url.scheme());
    }

    public void testEmptyAuthorityWithPath() throws Exception {
        URL url = URL.parse("http:///path");
        assertEquals("path", url.authority()); // Android will be ""
        assertEquals("/", url.path()); // Android will be "/path"
    }

    public void testEmptyAuthorityWithQuery() throws Exception {
        try {
            // This works on Android
            URL.parse("http://?query");
            fail();
        } catch (GalimatiasParseException expected) {}
    }

    public void testEmptyAuthorityWithFragment() throws Exception {
        try {
            // This works on Android
            URL.parse("http://#fragment");
            fail();
        } catch (GalimatiasParseException expected) {}
    }

    /*TODO
    public void testCombiningConstructorsMakeRelativePathsAbsolute() throws Exception {
        assertEquals("/relative", new URL("http", "host", "relative").getPath());
        assertEquals("/relative", new URL("http", "host", -1, "relative").getPath());
        assertEquals("/relative", new URL("http", "host", -1, "relative", null).getPath());
    }

    public void testCombiningConstructorsDoNotMakeEmptyPathsAbsolute() throws Exception {
        assertEquals("", new URL("http", "host", "").getPath());
        assertEquals("", new URL("http", "host", -1, "").getPath());
        assertEquals("", new URL("http", "host", -1, "", null).getPath());
    }
    */

    public void testPartContainsSpace() throws Exception {
        try {
            URL.parse("ht tp://host/");
            fail();
        } catch (GalimatiasParseException expected) {
        }
        assertEquals("user%20name", URL.parse("http://user name@host/").username());
        try {
            URL.parse("http://ho st/");
            fail();
        } catch (GalimatiasParseException expected) {
        }
        try {
            URL.parse("http://host:80 80/");
            fail();
        } catch (GalimatiasParseException expected) {
        }
        assertEquals("/fi%20le", URL.parse("http://host/fi le").file());
        assertEquals("que%20ry", URL.parse("http://host/file?que ry").query());
        //TODO: The following seems ok according to WHATWG URL (and matches Android too)
        //      but it does not seem compliant with RFC 3986? Gecko and WebKit have
        //      different behaviours about this.
        assertEquals("re f", URL.parse("http://host/file?query#re f").fragment());
    }

    // http://code.google.com/p/android/issues/detail?id=37577
    public void testUnderscore() throws Exception {
        URL url = URL.parse("http://a_b.c.d.net/");
        assertEquals("a_b.c.d.net", url.authority());
        // The RFC's don't permit underscores in hostnames, but URL accepts them (unlike URI).
        assertEquals("a_b.c.d.net", url.host().toString());
    }

    /*TODO
    // http://b/7369778
    public void testToURILeniantThrowsURISyntaxExceptionWithPartialTrailingEscape()
            throws Exception {
        // make sure if there a partial trailing escape that we don't throw the wrong exception
        URL[] badUrls = new URL[] {
            new URL("http://example.com/?foo=%%bar"),
            new URL("http://example.com/?foo=%%bar%"),
            new URL("http://example.com/?foo=%%bar%2"),
            new URL("http://example.com/?foo=%%bar%%"),
            new URL("http://example.com/?foo=%%bar%%%"),
            new URL("http://example.com/?foo=%%bar%%%%"),
        };
        for (URL badUrl : badUrls) {
            try {
                badUrl.toURILenient();
                fail();
            } catch (URISyntaxException expected) {
            }
        }

        // make sure we properly handle an normal escape at the end of a string
        String[] goodUrls = new String[] {
            "http://example.com/?foo=bar",
            "http://example.com/?foo=bar%20",
        };
        for (String goodUrl : goodUrls) {
            assertEquals(new URI(goodUrl), new URL(goodUrl).toURILenient());
        }
    }
    */

    // Adding a new test? Consider adding an equivalent test to URITest.java
}
