package com.lingualint.rules;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Flags filler words and phrases that usually add length without adding
  meaning, e.g. "basically", "literally", "in order to".
 */
public final class FillerWordRule implements Rule {

    // Longer phrases first so "in order to" matches as a whole rather
    // than leaving "order" and "to" behind.
    private static final Pattern FILLER_PATTERN = Pattern.compile(
            "\\b(in order to|basically|literally|actually|essentially|just|really|very|quite)\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getId() {
        return "filler-word";
    }

    @Override
    public String getDescription() {
        return "Flags filler words such as \"basically\", \"literally\", or \"in order to\".";
    }

    @Override
    public List<LintMatch> analyze(String text) {
        List<LintMatch> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        String[] lines = text.split("\n", -1);

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            Matcher matcher = FILLER_PATTERN.matcher(lines[lineIndex]);

            while (matcher.find()) {
                String filler = matcher.group();
                String suggestion = filler.equalsIgnoreCase("in order to")
                        ? "consider just \"to\""
                        : "consider removing it";

                matches.add(LintMatch.of(
                        getId(),
                        "Filler word \"" + filler + "\" — " + suggestion + ".",
                        lineIndex + 1,
                        matcher.start() + 1,
                        matcher.end(),
                        filler,
                        Severity.INFO
                ));
            }
        }

        return matches;
    }
}
