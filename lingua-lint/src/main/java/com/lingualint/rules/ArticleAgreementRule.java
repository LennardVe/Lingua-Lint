package com.lingualint.rules;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  Flags mismatches between the indefinite article ("a"/"an") and the
  word that follows it, e.g. "a apple" or "an dog".

  The correct article depends on sound, not spelling, so a small set
  of exceptions is hardcoded: a silent "h" makes a word start with a
  vowel sound ("an hour"), and a leading "u"/"eu" that sounds like
  "yu" makes a word start with a consonant sound ("a university").
  This is a heuristic, not a pronunciation dictionary, so uncommon
  words and acronyms ("an FBI agent") can still be misjudged, an
  acceptable trade-off for a dependency-free rule.
 */
public final class ArticleAgreementRule implements Rule {

    private static final Pattern ARTICLE_PATTERN = Pattern.compile(
            "\\b(a|an)\\s+([\\p{L}][\\p{L}'-]*)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> SILENT_H_WORDS = Set.of(
            "hour", "hours", "honest", "honestly", "honor", "honors", "honorable",
            "heir", "heirs"
    );
    private static final Set<String> CONSONANT_U_PREFIXES = Set.of(
            "uni", "use", "used", "useful", "user", "users", "usual", "usually",
            "utensil", "utility", "european", "eucalyptus", "one", "once"
    );

    @Override
    public String getId() {
        return "article-agreement";
    }

    @Override
    public String getDescription() {
        return "Flags \"a\"/\"an\" mismatches, e.g. \"a apple\" or \"an dog\".";
    }

    @Override
    public List<LintMatch> analyze(String text) {
        List<LintMatch> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        String[] lines = text.split("\n", -1);

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            Matcher matcher = ARTICLE_PATTERN.matcher(lines[lineIndex]);

            while (matcher.find()) {
                String article = matcher.group(1);
                String nextWord = matcher.group(2);
                String expected = expectedArticle(nextWord);

                if (expected.equalsIgnoreCase(article)) {
                    continue;
                }

                matches.add(LintMatch.of(
                        getId(),
                        "\"" + article + " " + nextWord + "\" — expected \""
                                + expected + " " + nextWord + "\".",
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

    private static String expectedArticle(String word) {
        String lower = word.toLowerCase();

        if (SILENT_H_WORDS.contains(lower)) {
            return "an";
        }
        if (startsWithConsonantU(lower)) {
            return "a";
        }

        char first = lower.charAt(0);
        boolean vowelSound = first == 'a' || first == 'e' || first == 'i'
                || first == 'o' || first == 'u';
        return vowelSound ? "an" : "a";
    }

    private static boolean startsWithConsonantU(String lower) {
        for (String prefix : CONSONANT_U_PREFIXES) {
            if (lower.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
