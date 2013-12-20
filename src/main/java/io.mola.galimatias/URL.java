package io.mola.galimatias;

/**
 * A parsed URL. Immutable.
 */
public class URL {

    final static String HTTP = "http";
    final static String HTTPS = "https";
    final static String DATA = "data";

    private final static int DEFAULT_HTTP_PORT = 80;
    private final static int DEFAULT_HTTPS_PORT = 443;

    private String scheme;
    private String user;
    private String password;
    private String host;
    private Integer port;
    private String path;
    private String queryString;
    private String fragment;
    private String content; // For data: URLs

    private boolean isAbsolute;
    private String fullURL;

    URL(final String scheme, final String user, final String password, final String host, final Integer port, final String path, final String queryString, final String fragment) {
        this.isAbsolute = true;
        this.scheme = scheme;
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
        this.queryString = queryString;
        this.fragment = fragment;
    }

    public String scheme() {
        return scheme;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public String host() {
        return host;
    }

    public Integer port() {
        return port;
    }

    public String path() {
        return path;
    }

    public String queryString() {
        return queryString;
    }

    public String fragment() {
        return fragment;
    }

    public String content() {
        return content;
    }

    @Override
    public String toString() {
        if (fullURL == null) {
            final StringBuilder sb = new StringBuilder();

            if (DATA.equals(schema)) {
                sb.append(DATA);
                sb.append(':');
                sb.append(content);
                fullURL = sb.toString();
                return fullURL;
            }

            // Absolute URL
            if (host != null) {
                if (schema != null) {
                    sb.append(schema);
                } else {
                    // Protocol-relative URL
                    sb.append('/');
                }
                sb.append("://");
                if (user != null && password != null) {
                    sb.append(user).append(':').append(password).append("@");
                }
                sb.append(host);
                if (port != null) {
                    sb.append(':').append(port);
                }
            }

            sb.append(path)
            ;
            if (queryString != null) {
                sb.append('?').append(queryString);
            }

            if (fragment != null) {
                sb.append('#').append(fragment);
            }

            fullURL = sb.toString();
        }

        return fullURL;
    }

}
