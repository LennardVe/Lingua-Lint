package com.lingualint.rules;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Flags sentences that don't start with an uppercase letter.

  A sentence start is detected as the beginning of a line, or the first
  letter after a ".", "!", or "?" followed by whitespace. This is a
  simple split, not a full sentence boundary parser, so it can misfire
  on abbreviations like "e.g." or "Mr." an acceptable trade-off for
  a dependency-free rule.
 */
public final class CapitalizationRule implements Rule {

    // Matches the first letter of a sentence either at the start of the
    // line, or right after sentence-ending punctuation and whitespace.
    private static final Pattern SENTENCE_START = Pattern.compile(
            "(?:^|[.!?]\\s+)([\\p{L}])"
    );

    @Override
    public String getId() {
        return "capitalization";
    }

    @Override
    public String getDescription() {
        return "Flags sentences that don't start with an uppercase letter.";
    }

    @Override
    public List<LintMatch> analyze(String text) {
        List<LintMatch> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        String[] lines = text.split("\n", -1);

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            Matcher matcher = SENTENCE_START.matcher(line);

            while (matcher.find()) {
                String letter = matcher.group(1);
                if (Character.isUpperCase(letter.charAt(0)) || !Character.isLetter(letter.charAt(0))) {
                    continue;
                }

                int column = matcher.start(1) + 1;

                matches.add(LintMatch.of(
                        getId(),
                        "Sentence should start with an uppercase letter.",
                        lineIndex + 1,
                        column,
                        column,
                        letter,
                        Severity.WARNING
                ));
            }
        }

        return matches;
    }
}
