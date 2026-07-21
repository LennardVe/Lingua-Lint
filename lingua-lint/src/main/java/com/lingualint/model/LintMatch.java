package com.lingualint.model;

import java.util.Objects;

/**
  A single finding produced by a rule: where it happened and what it means.

  @param ruleId      id of the rule that produced this match
  @param message     human-readable explanation, ideally with a suggestion
  @param line        1-based line number
  @param columnStart 1-based, inclusive
  @param columnEnd   1-based, inclusive
  @param matchedText the exact substring that triggered the match
  @param severity    how serious this finding is
 */
public record LintMatch(
        String ruleId,
        String message,
        int line,
        int columnStart,
        int columnEnd,
        String matchedText,
        Severity severity
) {

    public LintMatch {
        Objects.requireNonNull(ruleId, "ruleId must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(matchedText, "matchedText must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        if (line < 1) {
            throw new IllegalArgumentException("line must be >= 1, was " + line);
        }
        if (columnStart < 1 || columnEnd < columnStart) {
            throw new IllegalArgumentException(
                    "invalid column range [" + columnStart + ", " + columnEnd + "]");
        }
    }

    public static LintMatch of(String ruleId, String message, int line,
                                int columnStart, int columnEnd,
                                String matchedText, Severity severity) {
        return new LintMatch(ruleId, message, line, columnStart, columnEnd, matchedText, severity);
    }
}
