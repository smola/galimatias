#!/usr/bin/python
#
# Copyright (c) Santiago M. Mola <santi@mola.io>
# Released under the terms of the MIT License.
#
# generate_code_for_encodings_map.py
#
# Generates Java code for encodings map as defined
# in https://encoding.spec.whatwg.org/encodings.json
#
# More info at:
# https://encoding.spec.whatwg.org/#concept-encoding-get
#

import json

encodings = reduce(lambda a,b: a+b, map(lambda x: x['encodings'], json.load(open('encodings.json', 'r'))))

print '    // Code generated from https://encoding.spec.whatwg.org/encodings.json'
print '    // See scripts/generate_code_for_encodings_map.py'
print

for encoding in encodings:
    name = encoding['name'].upper().replace('-', '_')
    if name in ['REPLACEMENT', 'X_USER_DEFINED']:
        #XXX: Unsuppoorted
        continue
    print '    private static final Charset %s = Charset.forName("%s");' % (name, name)

print
print '    private static final Map<String,Charset> labelToEncodingMap = new HashMap<String,Charset>() {{'

for encoding in encodings:
    name = encoding['name'].upper().replace('-', '_')
    if name in ['REPLACEMENT', 'X_USER_DEFINED']:
       #XXX: Unsuppoorted
       continue
    print '        for (final String label : new String[]{ %s }) {' % ', '.join(map(lambda x: '"' + x + '"', encoding['labels']))
    print '            put(label, %s);' % name
    print '        }'

print '    }};'
