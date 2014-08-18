package io.mola.galimatias;

/**
 * Represents a parsing error.
 *
 * <strong>
 *     This API is considered experimental and will change in
 *     coming versions.
 * </strong>
 */
public enum ParseIssue {
    UNSPECIFIED,
    MISSING_SCHEME,
    INVALID_PERCENT_ENCODING,
    BACKSLASH_AS_DELIMITER,
    ILLEGAL_WHITESPACE,
    ILLEGAL_CHARACTER,
    INVALID_HOST
}
