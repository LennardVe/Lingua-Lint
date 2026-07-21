package com.lingualint;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.Collections;
import java.util.List;

/**
  Result of a lint run: an ordered, read-only list of matches plus
  counts per severity.
 */
public final class LintResult {

    private final List<LintMatch> matches;

    LintResult(List<LintMatch> matches) {
        this.matches = Collections.unmodifiableList(matches);
    }

    // All matches, sorted by line then column.
    public List<LintMatch> getMatches() {
        return matches;
    }

    public boolean isClean() {
        return matches.isEmpty();
    }

    public long errorCount() {
        return countBySeverity(Severity.ERROR);
    }

    public long warningCount() {
        return countBySeverity(Severity.WARNING);
    }

    public long infoCount() {
        return countBySeverity(Severity.INFO);
    }

    private long countBySeverity(Severity severity) {
        return matches.stream().filter(m -> m.severity() == severity).count();
    }
}
