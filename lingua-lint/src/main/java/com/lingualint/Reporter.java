package com.lingualint;

import com.lingualint.model.LintMatch;
import com.lingualint.model.Severity;

import java.util.List;

/**
  Renders a LintResult either as a colored text report or as JSON.
  Shared by the one-shot CLI and the interactive shell so both produce
  identical output for the same result.
 */
public final class Reporter {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String GRAY = "\u001B[90m";

    public static void printText(String source, String text, LintResult result) {
        String[] lines = text.split("\n", -1);

        System.out.println(BOLD + "lingua-lint report" + RESET + " - " + source);
        System.out.println(GRAY + "-".repeat(60) + RESET);

        if (result.isClean()) {
            System.out.println("No issues found.");
            return;
        }

        for (LintMatch match : result.getMatches()) {
            String lineText = match.line() - 1 < lines.length ? lines[match.line() - 1] : "";
            String color = colorFor(match.severity());

            System.out.printf("%s%d:%d%s  %s[%s]%s %s%n",
                    CYAN, match.line(), match.columnStart(), RESET,
                    color, match.severity(), RESET,
                    match.message());

            System.out.println("    " + lineText);
            System.out.println("    " + " ".repeat(Math.max(0, match.columnStart() - 1))
                    + color + "^".repeat(Math.max(1, match.columnEnd() - match.columnStart() + 1)) + RESET
                    + "  (" + GRAY + match.ruleId() + RESET + ")");
            System.out.println();
        }

        System.out.println(GRAY + "-".repeat(60) + RESET);
        System.out.printf("Summary: %d error(s), %d warning(s), %d info%n",
                result.errorCount(), result.warningCount(), result.infoCount());
    }

    // Minimal hand-rolled JSON output, no dependencies.
    public static void printJson(String source, LintResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"source\": \"").append(escapeJson(source)).append("\",\n");
        json.append("  \"errorCount\": ").append(result.errorCount()).append(",\n");
        json.append("  \"warningCount\": ").append(result.warningCount()).append(",\n");
        json.append("  \"infoCount\": ").append(result.infoCount()).append(",\n");
        json.append("  \"matches\": [\n");

        List<LintMatch> matches = result.getMatches();
        for (int i = 0; i < matches.size(); i++) {
            LintMatch match = matches.get(i);
            json.append("    {\n");
            json.append("      \"ruleId\": \"").append(escapeJson(match.ruleId())).append("\",\n");
            json.append("      \"severity\": \"").append(match.severity()).append("\",\n");
            json.append("      \"line\": ").append(match.line()).append(",\n");
            json.append("      \"columnStart\": ").append(match.columnStart()).append(",\n");
            json.append("      \"columnEnd\": ").append(match.columnEnd()).append(",\n");
            json.append("      \"matchedText\": \"").append(escapeJson(match.matchedText())).append("\",\n");
            json.append("      \"message\": \"").append(escapeJson(match.message())).append("\"\n");
            json.append("    }").append(i < matches.size() - 1 ? "," : "").append("\n");
        }

        json.append("  ]\n");
        json.append("}");

        System.out.println(json);
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String colorFor(Severity severity) {
        return switch (severity) {
            case ERROR -> RED;
            case WARNING -> YELLOW;
            case INFO -> CYAN;
        };
    }

    private Reporter() {
    }
}
