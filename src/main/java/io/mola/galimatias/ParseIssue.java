package io.mola.galimatias;

/**
* @author ablick
*/
public enum ParseIssue {
    UNSPECIFIED,
    MISSING_SCHEME,
    INVALID_PERCENT_ENCODING,
    BACKSLASH_AS_DELIMITER,
    ILLEGAL_WHITESPACE,
    ILLEGAL_CHARACTER,
    INVALID_HOST;
}
