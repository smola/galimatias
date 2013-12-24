galimatias
==========

galimatias is a URL parsing and normalizing library written in Java.

Design goals:

- Parse URLs as browsers do.
- Stay as close as possible to WHATWG's [URL Standard](http://url.spec.whatwg.org/). In fact, the aprser is largely based on it.
- Convenient fluent API with immutable URL objects.
- Convert to and from java.net.URL and java.net.URI without any error.
- Zero dependencies.

Non-goals:

- Be nit-picky about standards if that conflicts with the first goal.
- Provide networking capabilities.

But, why?
---------

galimatias started out of frustration with java.net.URL and java.net.URI. Both of them are good for basic use cases, but severely broken for others:

- **[java.net.URL.equals() is broken.](http://stackoverflow.com/a/3771123/205607)**

- **java.net.URI follows strict parsing rules.** URLs containing special characters (e.g. `^`) will throw an exception if passed to `URI.create`.

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
