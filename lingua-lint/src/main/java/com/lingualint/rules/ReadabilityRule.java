package com.lingualint.rules;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Estimates the Flesch-Kincaid grade level of the whole document and
  reports it as a single match, rather than flagging individual spans.

  Grade level above the configured threshold is reported as a WARNING
 (the text may be harder to read than intended); at or below it, an
  INFO match still reports the score for visibility.

  Syllable counting is a heuristic (vowel-group counting with common
  English adjustments), not a dictionary lookup, so the score is an
  approximation, not an exact match to tools that use a syllable dictionary.
 */
public final class ReadabilityRule implements Rule {

    private static final double DEFAULT_GRADE_THRESHOLD = 12.0;

    private static final Pattern SENTENCE_SPLIT = Pattern.compile("[.!?]+");
    private static final Pattern WORD_SPLIT = Pattern.compile("[\\p{L}'-]+");
    private static final Pattern VOWEL_GROUP = Pattern.compile("[aeiouy]+");

    private final double gradeThreshold;

    public ReadabilityRule() {
        this(DEFAULT_GRADE_THRESHOLD);
    }

    public ReadabilityRule(double gradeThreshold) {
        this.gradeThreshold = gradeThreshold;
    }

    @Override
    public String getId() {
        return "readability";
    }

    @Override
    public String getDescription() {
        return "Reports the estimated Flesch-Kincaid grade level of the document.";
    }

    @Override
    public List<LintMatch> analyze(String text) {
        List<LintMatch> matches = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return matches;
        }

        List<String> words = extractWords(text);
        if (words.isEmpty()) {
            return matches;
        }

        int sentenceCount = Math.max(1, countSentences(text));
        int wordCount = words.size();
        int syllableCount = words.stream().mapToInt(ReadabilityRule::countSyllables).sum();

        double wordsPerSentence = (double) wordCount / sentenceCount;
        double syllablesPerWord = (double) syllableCount / wordCount;
        double gradeLevel = 0.39 * wordsPerSentence + 11.8 * syllablesPerWord - 15.59;

        Severity severity = gradeLevel > gradeThreshold ? Severity.WARNING : Severity.INFO;
        String rounded = String.format("%.1f", gradeLevel);

        matches.add(LintMatch.of(
                getId(),
                "Estimated Flesch-Kincaid grade level: " + rounded
                        + " (" + wordCount + " words, " + sentenceCount + " sentences).",
                1, 1, 1,
                rounded,
                severity
        ));

        return matches;
    }

    private static int countSentences(String text) {
        String[] parts = SENTENCE_SPLIT.split(text);
        int count = 0;
        for (String part : parts) {
            if (!part.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private static List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();
        Matcher matcher = WORD_SPLIT.matcher(text);
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }

    /**
      Estimates syllables in a word by counting vowel groups, with a
      couple of common English corrections.
     */
    private static int countSyllables(String word) {
        String lower = word.toLowerCase();
        Matcher matcher = VOWEL_GROUP.matcher(lower);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        if (lower.endsWith("e") && !lower.endsWith("le") && count > 1) {
            count--;
        }

        return Math.max(1, count);
    }
}
