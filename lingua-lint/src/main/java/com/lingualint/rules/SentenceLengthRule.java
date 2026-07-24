package com.lingualint.rules;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Flags individual sentences longer than a configured word count, e.g.
  a 45-word sentence that's hard to follow in one breath.

  Sentences are detected the same way as in CapitalizationRule: split
  on ".", "!", or "?", one line at a time. This means a sentence that
  wraps across lines is treated as two sentences, an acceptable
  trade-off for a dependency-free, line-based rule.
 */
public final class SentenceLengthRule implements Rule {

    private static final int DEFAULT_WORD_THRESHOLD = 35;

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[^.!?]*[.!?]+|[^.!?]+$");
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}'-]+");

    private final int wordThreshold;

    public SentenceLengthRule() {
        this(DEFAULT_WORD_THRESHOLD);
    }

    public SentenceLengthRule(int wordThreshold) {
        this.wordThreshold = wordThreshold;
    }

    @Override
    public String getId() {
        return "sentence-length";
    }

    @Override
    public String getDescription() {
        return "Flags sentences longer than " + wordThreshold + " words.";
    }

    @Override
    public List<LintMatch> analyze(String text) {
        List<LintMatch> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        String[] lines = text.split("\n", -1);

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            Matcher matcher = SENTENCE_PATTERN.matcher(lines[lineIndex]);

            while (matcher.find()) {
                String sentence = matcher.group();
                if (sentence.isBlank()) {
                    continue;
                }

                int wordCount = countWords(sentence);
                if (wordCount <= wordThreshold) {
                    continue;
                }

                matches.add(LintMatch.of(
                        getId(),
                        "Sentence has " + wordCount + " words, over the " + wordThreshold
                                + "-word threshold. Consider splitting it up.",
                        lineIndex + 1,
                        matcher.start() + 1,
                        matcher.end(),
                        sentence.strip(),
                        Severity.INFO
                ));
            }
        }

        return matches;
    }

    private static int countWords(String sentence) {
        Matcher matcher = WORD_PATTERN.matcher(sentence);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
