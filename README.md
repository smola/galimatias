galimatias
==========

[![Build Status](https://travis-ci.org/smola/galimatias.png?branch=master)](https://travis-ci.org/smola/galimatias)
[![Coverage Status](https://coveralls.io/repos/smola/galimatias/badge.png?branch=master)](https://coveralls.io/r/smola/galimatias?branch=master)

galimatias is a URL parsing and normalization library written in Java.

Design goals:

- Parse URLs as browsers do, with optional normalization to RFC 3986 or RFC 2396.
- Stay as close as possible to WHATWG's [URL Standard](http://url.spec.whatwg.org/). In fact, the parser is largely based on it.
- Convenient fluent API with immutable URL objects.
- Interoperable with java.net.URL and java.net.URI.
- Zero dependencies.
- **I have not decided on the scope of galimatias with respect to non-HTTP(S) URIs. [I would like to read about your use cases here](https://github.com/smola/galimatias/issues/8).**

But, why?
---------

galimatias started out of frustration with java.net.URL and java.net.URI. Both of them are good for basic use cases, but severely broken for others:

- **[java.net.URL.equals() is broken.](http://stackoverflow.com/a/3771123/205607)**

- **java.net.URI can pase only RFC 2396 URI syntax.** `java.net.URI` will only parse a URI if it's strictly compliant with RFC 2396. Most URLs found in the wild do not comply with any syntax standard, and RFC 2396 is outdated anyway.

- **java.net.URI is not protocol-aware.** `http://example.com`, `http://example.com/` and `http://example.com:80` are different entities.

- **Manipulation is a pain.** I haven't seen any URL manipulation code using `java.net.URL` or `java.net.URI` that is simple and concise.

- **Not IDN ready.** Java has IDN support with `java.net.IDN`, but this does not apply to `java.net.URL` or `java.net.URI`.

Setup
-----

### Using Maven

TODO

### Standalone

TODO

Getting started
---------------

### Parse a URL

```java
// Parse
String urlString = //...
URL url;
try {
  url = URL.parse(urlString);
} catch (GalimatiasParseException ex) {
  // Do something with non-recoverable parsing error
}
```

### Convert to java.net.URL

```java
URL url = //...
java.net.URL javaURL;
try {
  javaURL = url.toJavaURL();
} catch (MalformedURLException ex) {
  // This can happen if scheme is not http, https, ftp, file or jar.
}
```

### Convert to java.net.URI

```java
URL url = //...
java.net.URI javaURI;
try {
  javaURI = url.toJavaURI();
} catch (URISyntaxException ex) {
  // This will happen if the URL contains unsafe characters (e.g. {}).
}
```

You can also parse a URL and convert it to java.net.URI without
any exception by forcing RFC 2396 compliance.

```java
String urlString = //...
URLParsingSettings settings = URLParsingSettings.create()
  .withStandard(URLParsingSettings.Standard.RFC_2396);
URL url = URL.parse(settings, urlString);
```

### Parse a URL with strict error handling

You can use a strict error handler that will throw an exception
on any invalid URL, even if it's a recovarable error.

```java
URLParsingSettings settings = URLParsingSettings.create()
  .withErrorHandler(StrictErrorHandler.getInstance());
URL url = URL.parse(settings, urlString);
```

Documentation
-------------

Check our [API docs](http://mola.io/galimatias/site/0.0.1-SNAPSHOT/apidocs/index.html).

Contribute
----------

Did you find a bug? [Report it on GitHub](https://github.com/smola/galimatias/issues).

Did you write a patch? Send a pull request.

Something else? Email me at santi@mola.io.

License
-------

Copyright (c) 2013 Santiago M. Mola <santi@mola.io>

galimatias is released under the terms of the MIT License.
