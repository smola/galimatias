galimatias
==========

[![Build Status](https://travis-ci.org/smola/galimatias?branch=master)](https://travis-ci.org/smola/galimatias)
[![Coverage Status](https://coveralls.io/repos/smola/galimatias/badge.png?branch=master)](https://coveralls.io/r/smola/galimatias?branch=master)

galimatias is a URL parsing and normalization library written in Java.

Design goals:

- Parse URLs as browsers do, with optional normalization to RFC 3986 or RFC 2396.
- Stay as close as possible to WHATWG's [URL Standard](http://url.spec.whatwg.org/). In fact, the parser is largely based on it.
- Convenient fluent API with immutable URL objects.
- Interoperable with java.net.URL and java.net.URI.
- Zero dependencies.

But, why?
---------

galimatias started out of frustration with java.net.URL and java.net.URI. Both of them are good for basic use cases, but severely broken for others:

- **[java.net.URL.equals() is broken.](http://stackoverflow.com/a/3771123/205607)**

- **java.net.URI can pase only RFC 2396 URI syntax.** `java.net.URI` will only parse a URI if it's strictly compliant with RFC 2396. Most URLs found in the wild do not comply with any syntax standard, and RFC 2396 is outdated anyway.

- **java.net.URI is not protocol-aware.** `http://example.com`, `http://example.com/` and `http://example.com:80` are different entities.

- **Manipulation is a pain.** I haven't seen any URL manipulation code using `java.net.URL` or `java.net.URI` that is simple and concise.

- **Not IDN ready.** Java has IDN support with `java.net.IDN`, but this does not apply to `java.net.URL` or `java.net.URI`. `URI.create("http://ß.com").toASCIIString()` will return `http://%C3%9F.com`, but the correct result would be `http://xn--pda.com/`.


Setup
-----

TODO

Getting started
---------------

TODO

Documentation
-------------

TODO

Contribute
----------

Did you find a bug? Report it on GitHub.

Did you write a patch? Send a pull request.

Something else? Email me at santi@mola.io.

License
-------

Copyright (c) 2013 Santiago M. Mola <santi@mola.io>

galimatias is released under the terms of the MIT License.
