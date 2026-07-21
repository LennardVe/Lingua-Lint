package com.lingualint.rules;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Flags basic passive voice: a form of "to be" followed by a word ending
  in "-ed", e.g. "was completed", "is managed", "were rejected".

  This is a heuristic, not a grammatical parse. It misses irregular past
  participles ("was written", "was given") and will occasionally flag
  adjectives that happen to end in "-ed" ("was annoyed"). Both are
  reasonable trade-offs for a dependency-free regex rule, a stronger
  version could add an irregular-verb list or a POS tagger later.
 */
public final class PassiveVoiceRule implements Rule {

    private static final Pattern PASSIVE_PATTERN = Pattern.compile(
            "\\b(is|are|was|were|be|been|being)\\b(\\s+)(?:([\\p{L}]+ly)\\s+)?([\\p{L}]+ed)\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getId() {
        return "passive-voice";
    }

    @Override
    public String getDescription() {
        return "Flags basic passive voice constructions, e.g. \"was completed\".";
    }

    @Override
    public List<LintMatch> analyze(String text) {
        List<LintMatch> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        String[] lines = text.split("\n", -1);

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            Matcher matcher = PASSIVE_PATTERN.matcher(lines[lineIndex]);

            while (matcher.find()) {
                String participle = matcher.group(4);

                matches.add(LintMatch.of(
                        getId(),
                        "Possible passive voice: \"" + matcher.group() + "\". "
                                + "Consider rewriting in active voice (who performed \""
                                + participle + "\"?).",
                        lineIndex + 1,
                        matcher.start() + 1,
                        matcher.end(),
                        matcher.group(),
                        Severity.INFO
                ));
            }
        }

        return matches;
    }
}
