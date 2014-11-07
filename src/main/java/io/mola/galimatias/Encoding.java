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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implements some static util methods from the
 * <a href="https://encoding.spec.whatwg.org/">WHATWG Encoding Standard</a>.
 */
final class Encoding {

    /**
     * The <a href="https://encoding.spec.whatwg.org/#concept-encoding-get">get an encoding</a>
     * method. This is similar to {@link java.nio.charset.Charset#forName(String)} but imposes
     * additional restrictions to guarantee compatibility with legacy content.
     *
     * @param label
     * @return
     */
    public static Charset getEncoding(final String label) {
        if (label == null) {
            throw new NullPointerException("label");
        }
        final String lowerLabel = label.trim().toLowerCase(Locale.ENGLISH);
        return labelToEncodingMap.get(lowerLabel);
    }

    // Code generated from https://encoding.spec.whatwg.org/encodings.json
    // See scripts/generate_code_for_encodings_map.py

    private static final Charset UTF_8 = Charset.forName("UTF_8");
    private static final Charset IBM866 = Charset.forName("IBM866");
    private static final Charset ISO_8859_2 = Charset.forName("ISO_8859_2");
    private static final Charset ISO_8859_3 = Charset.forName("ISO_8859_3");
    private static final Charset ISO_8859_4 = Charset.forName("ISO_8859_4");
    private static final Charset ISO_8859_5 = Charset.forName("ISO_8859_5");
    private static final Charset ISO_8859_6 = Charset.forName("ISO_8859_6");
    private static final Charset ISO_8859_7 = Charset.forName("ISO_8859_7");
    private static final Charset ISO_8859_8 = Charset.forName("ISO_8859_8");
    private static final Charset ISO_8859_8_I = Charset.forName("ISO_8859_8_I");
    private static final Charset ISO_8859_10 = Charset.forName("ISO_8859_10");
    private static final Charset ISO_8859_13 = Charset.forName("ISO_8859_13");
    private static final Charset ISO_8859_14 = Charset.forName("ISO_8859_14");
    private static final Charset ISO_8859_15 = Charset.forName("ISO_8859_15");
    private static final Charset ISO_8859_16 = Charset.forName("ISO_8859_16");
    private static final Charset KOI8_R = Charset.forName("KOI8_R");
    private static final Charset KOI8_U = Charset.forName("KOI8_U");
    private static final Charset MACINTOSH = Charset.forName("MACINTOSH");
    private static final Charset WINDOWS_874 = Charset.forName("WINDOWS_874");
    private static final Charset WINDOWS_1250 = Charset.forName("WINDOWS_1250");
    private static final Charset WINDOWS_1251 = Charset.forName("WINDOWS_1251");
    private static final Charset WINDOWS_1252 = Charset.forName("WINDOWS_1252");
    private static final Charset WINDOWS_1253 = Charset.forName("WINDOWS_1253");
    private static final Charset WINDOWS_1254 = Charset.forName("WINDOWS_1254");
    private static final Charset WINDOWS_1255 = Charset.forName("WINDOWS_1255");
    private static final Charset WINDOWS_1256 = Charset.forName("WINDOWS_1256");
    private static final Charset WINDOWS_1257 = Charset.forName("WINDOWS_1257");
    private static final Charset WINDOWS_1258 = Charset.forName("WINDOWS_1258");
    private static final Charset X_MAC_CYRILLIC = Charset.forName("X_MAC_CYRILLIC");
    private static final Charset GB18030 = Charset.forName("GB18030");
    private static final Charset BIG5 = Charset.forName("BIG5");
    private static final Charset EUC_JP = Charset.forName("EUC_JP");
    private static final Charset ISO_2022_JP = Charset.forName("ISO_2022_JP");
    private static final Charset SHIFT_JIS = Charset.forName("SHIFT_JIS");
    private static final Charset EUC_KR = Charset.forName("EUC_KR");
    private static final Charset UTF_16BE = Charset.forName("UTF_16BE");
    private static final Charset UTF_16LE = Charset.forName("UTF_16LE");

    private static final Map<String,Charset> labelToEncodingMap = new HashMap<String,Charset>() {{
        for (final String label : new String[]{ "unicode-1-1-utf-8", "utf-8", "utf8" }) {
            put(label, UTF_8);
        }
        for (final String label : new String[]{ "866", "cp866", "csibm866", "ibm866" }) {
            put(label, IBM866);
        }
        for (final String label : new String[]{ "csisolatin2", "iso-8859-2", "iso-ir-101", "iso8859-2", "iso88592", "iso_8859-2", "iso_8859-2:1987", "l2", "latin2" }) {
            put(label, ISO_8859_2);
        }
        for (final String label : new String[]{ "csisolatin3", "iso-8859-3", "iso-ir-109", "iso8859-3", "iso88593", "iso_8859-3", "iso_8859-3:1988", "l3", "latin3" }) {
            put(label, ISO_8859_3);
        }
        for (final String label : new String[]{ "csisolatin4", "iso-8859-4", "iso-ir-110", "iso8859-4", "iso88594", "iso_8859-4", "iso_8859-4:1988", "l4", "latin4" }) {
            put(label, ISO_8859_4);
        }
        for (final String label : new String[]{ "csisolatincyrillic", "cyrillic", "iso-8859-5", "iso-ir-144", "iso8859-5", "iso88595", "iso_8859-5", "iso_8859-5:1988" }) {
            put(label, ISO_8859_5);
        }
        for (final String label : new String[]{ "arabic", "asmo-708", "csiso88596e", "csiso88596i", "csisolatinarabic", "ecma-114", "iso-8859-6", "iso-8859-6-e", "iso-8859-6-i", "iso-ir-127", "iso8859-6", "iso88596", "iso_8859-6", "iso_8859-6:1987" }) {
            put(label, ISO_8859_6);
        }
        for (final String label : new String[]{ "csisolatingreek", "ecma-118", "elot_928", "greek", "greek8", "iso-8859-7", "iso-ir-126", "iso8859-7", "iso88597", "iso_8859-7", "iso_8859-7:1987", "sun_eu_greek" }) {
            put(label, ISO_8859_7);
        }
        for (final String label : new String[]{ "csiso88598e", "csisolatinhebrew", "hebrew", "iso-8859-8", "iso-8859-8-e", "iso-ir-138", "iso8859-8", "iso88598", "iso_8859-8", "iso_8859-8:1988", "visual" }) {
            put(label, ISO_8859_8);
        }
        for (final String label : new String[]{ "csiso88598i", "iso-8859-8-i", "logical" }) {
            put(label, ISO_8859_8_I);
        }
        for (final String label : new String[]{ "csisolatin6", "iso-8859-10", "iso-ir-157", "iso8859-10", "iso885910", "l6", "latin6" }) {
            put(label, ISO_8859_10);
        }
        for (final String label : new String[]{ "iso-8859-13", "iso8859-13", "iso885913" }) {
            put(label, ISO_8859_13);
        }
        for (final String label : new String[]{ "iso-8859-14", "iso8859-14", "iso885914" }) {
            put(label, ISO_8859_14);
        }
        for (final String label : new String[]{ "csisolatin9", "iso-8859-15", "iso8859-15", "iso885915", "iso_8859-15", "l9" }) {
            put(label, ISO_8859_15);
        }
        for (final String label : new String[]{ "iso-8859-16" }) {
            put(label, ISO_8859_16);
        }
        for (final String label : new String[]{ "cskoi8r", "koi", "koi8", "koi8-r", "koi8_r" }) {
            put(label, KOI8_R);
        }
        for (final String label : new String[]{ "koi8-u" }) {
            put(label, KOI8_U);
        }
        for (final String label : new String[]{ "csmacintosh", "mac", "macintosh", "x-mac-roman" }) {
            put(label, MACINTOSH);
        }
        for (final String label : new String[]{ "dos-874", "iso-8859-11", "iso8859-11", "iso885911", "tis-620", "windows-874" }) {
            put(label, WINDOWS_874);
        }
        for (final String label : new String[]{ "cp1250", "windows-1250", "x-cp1250" }) {
            put(label, WINDOWS_1250);
        }
        for (final String label : new String[]{ "cp1251", "windows-1251", "x-cp1251" }) {
            put(label, WINDOWS_1251);
        }
        for (final String label : new String[]{ "ansi_x3.4-1968", "ascii", "cp1252", "cp819", "csisolatin1", "ibm819", "iso-8859-1", "iso-ir-100", "iso8859-1", "iso88591", "iso_8859-1", "iso_8859-1:1987", "l1", "latin1", "us-ascii", "windows-1252", "x-cp1252" }) {
            put(label, WINDOWS_1252);
        }
        for (final String label : new String[]{ "cp1253", "windows-1253", "x-cp1253" }) {
            put(label, WINDOWS_1253);
        }
        for (final String label : new String[]{ "cp1254", "csisolatin5", "iso-8859-9", "iso-ir-148", "iso8859-9", "iso88599", "iso_8859-9", "iso_8859-9:1989", "l5", "latin5", "windows-1254", "x-cp1254" }) {
            put(label, WINDOWS_1254);
        }
        for (final String label : new String[]{ "cp1255", "windows-1255", "x-cp1255" }) {
            put(label, WINDOWS_1255);
        }
        for (final String label : new String[]{ "cp1256", "windows-1256", "x-cp1256" }) {
            put(label, WINDOWS_1256);
        }
        for (final String label : new String[]{ "cp1257", "windows-1257", "x-cp1257" }) {
            put(label, WINDOWS_1257);
        }
        for (final String label : new String[]{ "cp1258", "windows-1258", "x-cp1258" }) {
            put(label, WINDOWS_1258);
        }
        for (final String label : new String[]{ "x-mac-cyrillic", "x-mac-ukrainian" }) {
            put(label, X_MAC_CYRILLIC);
        }
        for (final String label : new String[]{ "chinese", "csgb2312", "csiso58gb231280", "gb18030", "gb2312", "gb_2312", "gb_2312-80", "gbk", "iso-ir-58", "x-gbk" }) {
            put(label, GB18030);
        }
        for (final String label : new String[]{ "big5", "big5-hkscs", "cn-big5", "csbig5", "x-x-big5" }) {
            put(label, BIG5);
        }
        for (final String label : new String[]{ "cseucpkdfmtjapanese", "euc-jp", "x-euc-jp" }) {
            put(label, EUC_JP);
        }
        for (final String label : new String[]{ "csiso2022jp", "iso-2022-jp" }) {
            put(label, ISO_2022_JP);
        }
        for (final String label : new String[]{ "csshiftjis", "ms_kanji", "shift-jis", "shift_jis", "sjis", "windows-31j", "x-sjis" }) {
            put(label, SHIFT_JIS);
        }
        for (final String label : new String[]{ "cseuckr", "csksc56011987", "euc-kr", "iso-ir-149", "korean", "ks_c_5601-1987", "ks_c_5601-1989", "ksc5601", "ksc_5601", "windows-949" }) {
            put(label, EUC_KR);
        }
        for (final String label : new String[]{ "utf-16be" }) {
            put(label, UTF_16BE);
        }
        for (final String label : new String[]{ "utf-16", "utf-16le" }) {
            put(label, UTF_16LE);
        }
    }};

}
