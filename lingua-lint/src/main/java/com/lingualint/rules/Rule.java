package com.lingualint.rules;

import com.lingualint.model.LintMatch;

import java.util.List;

/**
  Extension point for lint rules. To add a new rule:

    1. Implement this interface.
    2. Give it a unique, stable id and a short description.
    3. Implement analyze() to scan the text and return matches.
    4. Register an instance with LinterEngine.registerRule().

  Rules should be stateless (or thread-safe), independent of each other,
  and should never throw on malformed input return an empty list instead.
 */
public interface Rule {

    // Short, unique, machine-friendly id, e.g. "repeated-word".
    String getId();

    // One-line description of what this rule checks for.
    String getDescription();

    /**
      Analyzes the given text and returns everything this rule finds.
      Never returns null; returns an empty list when there's nothing to report.
     */
    List<LintMatch> analyze(String text);
}
