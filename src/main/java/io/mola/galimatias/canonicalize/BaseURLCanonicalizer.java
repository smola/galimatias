package io.mola.galimatias.canonicalize;

import static io.mola.galimatias.URLUtils.UTF_8;
import static io.mola.galimatias.URLUtils.isASCIIHexDigit;
import static io.mola.galimatias.URLUtils.percentEncode;

abstract class BaseURLCanonicalizer implements URLCanonicalizer {

  protected static String canonicalize(String input, CharacterPredicate unencodedPredicate) {
    StringBuilder result = new StringBuilder();
    final int length = input.length();
    for (int offset = 0; offset < length; ) {
        final int c = input.codePointAt(offset);

        if ((c == '%' && input.length() > offset + 2 &&
                isASCIIHexDigit(input.charAt(offset + 1)) && isASCIIHexDigit(input.charAt(offset + 2))) ||
            unencodedPredicate.test(c)) {
            result.append((char) c);
        } else {
            final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
            for (final byte b : bytes) {
                percentEncode(b, result);
            }
        }

        offset += Character.charCount(c);
    }
    return result.toString();
  }

}
