package com.lingualint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
  CLI entry point.

  One-shot mode (for scripts and CI):
    java com.lingualint.Main <file> [--ignore-rule=id[,id...]] [--format=text|json]

  Interactive mode (for linting several files in one session):
    java com.lingualint.Main
    (starts a shell; type 'help' once inside for the list of commands)
 */
public final class Main {

    public static void main(String[] args) {
        CliOptions options = CliOptions.parse(args);

        if (options.filePath() == null) {
            new Repl(options.ignoredRuleIds(), options.format()).run();
            return;
        }

        String text;
        try {
            text = Files.readString(options.filePath());
        } catch (IOException e) {
            System.err.println("Could not read file '" + options.filePath() + "': " + e.getMessage());
            System.exit(1);
            return;
        }

        LintResult result = RuleRegistry.buildEngine(options.ignoredRuleIds()).lint(text);

        if (options.format() == OutputFormat.JSON) {
            Reporter.printJson(options.filePath().toString(), result);
        } else {
            Reporter.printText(options.filePath().toString(), text, result);
        }

        if (result.errorCount() > 0) {
            System.exit(1);
        }
    }

    private Main() {
    }

    // Parsed command-line arguments.
    private record CliOptions(Path filePath, Set<String> ignoredRuleIds, OutputFormat format) {

        static CliOptions parse(String[] args) {
            Path filePath = null;
            Set<String> ignoredRuleIds = new HashSet<>();
            OutputFormat format = OutputFormat.TEXT;

            for (String arg : args) {
                if (arg.startsWith("--ignore-rule=")) {
                    String value = arg.substring("--ignore-rule=".length());
                    ignoredRuleIds.addAll(Arrays.asList(value.split(",")));
                } else if (arg.startsWith("--format=")) {
                    String value = arg.substring("--format=".length());
                    format = "json".equalsIgnoreCase(value) ? OutputFormat.JSON : OutputFormat.TEXT;
                } else if (!arg.startsWith("--")) {
                    filePath = Path.of(arg);
                } else {
                    System.err.println("Unknown flag: " + arg);
                }
            }

            return new CliOptions(filePath, ignoredRuleIds, format);
        }
    }
}
