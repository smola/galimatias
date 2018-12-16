package io.mola.galimatias;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static io.mola.galimatias.URLUtils.defaultEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class URLParserTest {
    @ParameterizedTest
    @MethodSource("io.mola.galimatias.URLTestData#cases")
    void testParseValid(final URLTestData data) throws GalimatiasParseException {
        assumeFalse(data.failure);
        final URL base = URL.parse(data.base);
        final URL url = URL.parse(base, data.input);
        assertURL(data, url);
    }

    void assertURL(final URLTestData data, final URL url) {
        assertEquals(data.scheme(), url.scheme());
        assertEquals(data.username, url.username());
        assertEquals(data.password, defaultEmpty(url.password())); //FIXME
        assertEquals(data.hostname, (url.host() == null)? "" : url.host().toString());
        assertEquals(data.port, url.port()
                .filter((p) -> !url.defaultPort().map(dp -> dp.equals(p)).orElse(true))
                .map(Object::toString).orElse(""));
        assertEquals(data.pathname, defaultEmpty(url.path())); //FIXME
        assertEquals(data.search.replaceFirst("^\\?", ""), defaultEmpty(url.query())); //FIXME
        assertEquals(data.hash.replaceFirst("^#", ""), defaultEmpty(url.fragment())); //FIXME
    }
}
