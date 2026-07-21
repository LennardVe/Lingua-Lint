package com.lingualint;

import com.lingualint.rules.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
  Interactive shell: keeps the JVM running so a user can lint several
  files in a row, without re-launching the CLI
  and re-registering rules each time.

  Commands:
    lint <path>       lint a file
    text <text...>    lint the given text directly
    rules             list available rules and whether they're active
    ignore <id>       stop running a rule
    unignore <id>     re-enable a previously ignored rule
    format text|json  switch the report format
    help              show this list again
    exit / quit       leave the shell
 */
public final class Repl {

    private static final String PROMPT = "lingua-lint> ";

    private final Set<String> ignoredRuleIds;
    private OutputFormat format;

    public Repl() {
        this(new HashSet<>(), OutputFormat.TEXT);
    }

    public Repl(Set<String> initialIgnoredRuleIds, OutputFormat initialFormat) {
        this.ignoredRuleIds = new HashSet<>(initialIgnoredRuleIds);
        this.format = initialFormat;
    }

    public void run() {
        System.out.println("lingua-lint interactive shell. Type 'help' for commands, 'exit' to quit.");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(PROMPT);
            if (!scanner.hasNextLine()) {
                System.out.println();
                return;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String rest = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case "lint" -> lintFile(rest);
                case "text" -> lintText(rest);
                case "rules" -> listRules();
                case "ignore" -> ignoreRule(rest);
                case "unignore" -> unignoreRule(rest);
                case "format" -> setFormat(rest);
                case "help" -> printHelp();
                case "exit", "quit" -> {
                    return;
                }
                default -> System.out.println("Unknown command: '" + command + "'. Type 'help' for a list of commands.");
            }
        }
    }

    private void lintFile(String pathArg) {
        if (pathArg.isBlank()) {
            System.out.println("Usage: lint <path>");
            return;
        }

        Path path = Path.of(pathArg.trim());
        String text;
        try {
            text = Files.readString(path);
        } catch (IOException e) {
            System.out.println("Could not read file '" + path + "': " + e.getMessage());
            return;
        }

        report(path.toString(), text);
    }

    private void lintText(String text) {
        if (text.isBlank()) {
            System.out.println("Usage: text <some text to lint>");
            return;
        }
        report("<inline text>", text);
    }

    private void report(String source, String text) {
        LintResult result = RuleRegistry.buildEngine(ignoredRuleIds).lint(text);
        if (format == OutputFormat.JSON) {
            Reporter.printJson(source, result);
        } else {
            Reporter.printText(source, text, result);
        }
    }

    private void listRules() {
        for (Rule rule : RuleRegistry.allRules()) {
            String status = ignoredRuleIds.contains(rule.getId()) ? "ignored" : "active";
            System.out.printf("%-16s [%s] %s%n", rule.getId(), status, rule.getDescription());
        }
    }

    private void ignoreRule(String ruleId) {
        if (!ruleExists(ruleId)) {
            return;
        }
        ignoredRuleIds.add(ruleId);
        System.out.println("Ignoring rule: " + ruleId);
    }

    private void unignoreRule(String ruleId) {
        if (!ruleExists(ruleId)) {
            return;
        }
        ignoredRuleIds.remove(ruleId);
        System.out.println("Re-enabled rule: " + ruleId);
    }

    private boolean ruleExists(String ruleId) {
        boolean exists = RuleRegistry.allRules().stream().anyMatch(r -> r.getId().equals(ruleId));
        if (!exists) {
            System.out.println("Unknown rule id: '" + ruleId + "'. Type 'rules' to see available rules.");
        }
        return exists;
    }

    private void setFormat(String value) {
        if ("json".equalsIgnoreCase(value)) {
            format = OutputFormat.JSON;
        } else if ("text".equalsIgnoreCase(value)) {
            format = OutputFormat.TEXT;
        } else {
            System.out.println("Usage: format text|json");
            return;
        }
        System.out.println("Output format set to " + format);
    }

    private void printHelp() {
        System.out.println("""
                lint <path>       lint a file
                text <text...>    lint the given text directly
                rules             list available rules and whether they're active
                ignore <id>       stop running a rule
                unignore <id>     re-enable a previously ignored rule
                format text|json  switch the report format
                help              show this list again
                exit / quit       leave the shell""");
    }
}
