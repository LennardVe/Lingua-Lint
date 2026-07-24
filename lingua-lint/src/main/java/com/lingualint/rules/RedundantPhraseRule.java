package com.lingualint.rules;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Flags redundant phrases (pleonasms) where one word already implies
  the other, e.g. "advance planning", "close proximity", "each and
  every".
 */
public final class RedundantPhraseRule implements Rule {

    // Longer phrases first so e.g. "each and every" matches whole
    // rather than leaving "each" and "and every" behind.
    private static final Pattern REDUNDANT_PATTERN = Pattern.compile(
            "\\b(each and every|unexpected surprise|absolutely essential|"
                    + "completely eliminate|advance planning|close proximity|"
                    + "end result|final outcome|future plans|past history|"
                    + "free gift|added bonus|brief summary|true facts|"
                    + "revert back|repeat again|combine together)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Phrase -> a shorter replacement, keyed in lowercase.
    private static final Map<String, String> REPLACEMENTS = Map.ofEntries(
            Map.entry("each and every", "each"),
            Map.entry("unexpected surprise", "surprise"),
            Map.entry("absolutely essential", "essential"),
            Map.entry("completely eliminate", "eliminate"),
            Map.entry("advance planning", "planning"),
            Map.entry("close proximity", "proximity"),
            Map.entry("end result", "result"),
            Map.entry("final outcome", "outcome"),
            Map.entry("future plans", "plans"),
            Map.entry("past history", "history"),
            Map.entry("free gift", "gift"),
            Map.entry("added bonus", "bonus"),
            Map.entry("brief summary", "summary"),
            Map.entry("true facts", "facts"),
            Map.entry("revert back", "revert"),
            Map.entry("repeat again", "repeat"),
            Map.entry("combine together", "combine")
    );

    @Override
    public String getId() {
        return "redundant-phrase";
    }

    @Override
    public String getDescription() {
        return "Flags redundant phrases such as \"close proximity\" or \"each and every\".";
    }

    @Override
    public List<LintMatch> analyze(String text) {
        List<LintMatch> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        String[] lines = text.split("\n", -1);

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            Matcher matcher = REDUNDANT_PATTERN.matcher(lines[lineIndex]);

            while (matcher.find()) {
                String phrase = matcher.group();
                String replacement = REPLACEMENTS.get(phrase.toLowerCase());

                matches.add(LintMatch.of(
                        getId(),
                        "Redundant phrase \"" + phrase + "\" — consider \"" + replacement + "\".",
                        lineIndex + 1,
                        matcher.start() + 1,
                        matcher.end(),
                        phrase,
                        Severity.INFO
                ));
            }
        }

        return matches;
    }
}
