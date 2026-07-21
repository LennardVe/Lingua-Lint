package com.lingualint.rules;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Flags immediately repeated words, e.g. "the the" or "is is", ignoring case.
  A common typo from editing or copy-pasting.
 */
public final class RepeatedWordRule implements Rule {

    private static final Pattern REPEATED_WORD_PATTERN = Pattern.compile(
            "\\b([\\p{L}\\p{N}'-]+)\\b(\\s+)(\\1)\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getId() {
        return "repeated-word";
    }

    @Override
    public String getDescription() {
        return "Flags immediately repeated words, e.g. \"the the\".";
    }

    @Override
    public List<LintMatch> analyze(String text) {
        List<LintMatch> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        // Process line by line so matches carry real line/column positions.
        String[] lines = text.split("\n", -1);

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            Matcher matcher = REPEATED_WORD_PATTERN.matcher(lines[lineIndex]);

            while (matcher.find()) {
                matches.add(LintMatch.of(
                        getId(),
                        "Repeated word \"" + matcher.group(1) + "\" — did you mean to type it once?",
                        lineIndex + 1,
                        matcher.start() + 1,
                        matcher.end(),
                        matcher.group(),
                        Severity.WARNING
                ));
            }
        }

        return matches;
    }
}
