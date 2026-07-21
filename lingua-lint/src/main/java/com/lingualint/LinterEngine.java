package com.lingualint;

import com.lingualint.model.LintMatch;
import com.lingualint.rules.Rule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
  Runs a set of registered rules over a piece of text and merges the
  results into a single sorted LintResult.

  Usage:
    LinterEngine engine = new LinterEngine();
    engine.registerRule(new RepeatedWordRule());
    LintResult result = engine.lint(someText);
 */
public final class LinterEngine {

    // Line, then column, then rule id keeps output deterministic when
    // multiple rules match at the same spot.
    private static final Comparator<LintMatch> BY_POSITION =
            Comparator.comparingInt(LintMatch::line)
                    .thenComparingInt(LintMatch::columnStart)
                    .thenComparing(LintMatch::ruleId);

    private final List<Rule> rules = new ArrayList<>();

    public void registerRule(Rule rule) {
        Objects.requireNonNull(rule, "rule must not be null");
        rules.add(rule);
    }

    public List<Rule> getRules() {
        return List.copyOf(rules);
    }

    /**
      Runs every registered rule over the text and returns all matches,
      sorted by position. A rule that throws is skipped rather than
      failing the whole run, so one bad rule can't take down the rest.
     */
    public LintResult lint(String text) {
        String safeText = text == null ? "" : text;
        List<LintMatch> allMatches = new ArrayList<>();

        for (Rule rule : rules) {
            try {
                List<LintMatch> ruleMatches = rule.analyze(safeText);
                if (ruleMatches != null) {
                    allMatches.addAll(ruleMatches);
                }
            } catch (RuntimeException e) {
                System.err.println("[lingua-lint] Rule '" + rule.getId()
                        + "' threw an exception and was skipped: " + e);
            }
        }

        allMatches.sort(BY_POSITION);
        return new LintResult(allMatches);
    }
}
