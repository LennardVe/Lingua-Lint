# lingua-lint

A modular, rule-based text linter / grammar-checking engine in pure Java (17+), with zero runtime dependencies.

## Project layout

```
lingua-lint/
├── build.gradle
└── src/main/java/com/lingualint/
    ├── Main.java              # CLI entry point (one-shot mode or launches the shell)
    ├── Repl.java              # Interactive shell for linting multiple files in one session
    ├── RuleRegistry.java      # Central list of built-in rules
    ├── Reporter.java          # Renders a LintResult as text or JSON
    ├── OutputFormat.java      # TEXT, JSON
    ├── LinterEngine.java      # Orchestrates rule execution
    ├── LintResult.java        # Immutable result of a lint run
    ├── model/
    │   ├── Severity.java      # INFO, WARNING, ERROR
    │   └── LintMatch.java     # A single finding
    └── rules/
        ├── Rule.java                  # Extension point for all rules
        ├── RepeatedWordRule.java      # "the the" style duplicate word detection
        ├── PassiveVoiceRule.java      # "was completed" style passive voice detection
        ├── CapitalizationRule.java    # sentences should start with an uppercase letter
        ├── FillerWordRule.java        # "basically", "literally", "in order to", ...
        └── ReadabilityRule.java       # Flesch-Kincaid grade level for the whole document
```

## Running it

One-shot mode, for scripts and CI:

```bash
./gradlew run --args="path/to/file.txt"
./gradlew run --args="path/to/file.txt --ignore-rule=passive-voice --format=json"
```

Interactive mode, for linting several files in one session:

```bash
./gradlew run
```

```
lingua-lint interactive shell. Type 'help' for commands, 'exit' to quit.
lingua-lint> lint draft.txt
...
lingua-lint> ignore filler-word
Ignoring rule: filler-word
lingua-lint> lint draft2.txt
...
lingua-lint> exit
```

Or, compiled by hand:

```bash
javac -d out $(find src -name "*.java")
java -cp out com.lingualint.Main path/to/file.txt   # one-shot
java -cp out com.lingualint.Main                    # interactive shell
```

### Shell commands

```
lint <path>       lint a file
text <text...>    lint the given text directly
rules             list available rules and whether they're active
ignore <id>       stop running a rule
unignore <id>     re-enable a previously ignored rule
format text|json  switch the report format
help              show this list again
exit / quit       leave the shell
```

## Adding a new rule

1. Create a class in `com.lingualint.rules` implementing `Rule`:

```java
public final class MyRule implements Rule {
    @Override
    public String getId() { return "my-rule"; }

    @Override
    public String getDescription() { return "Explains what this checks for."; }

    @Override
    public List<LintMatch> analyze(String text) {
        // scan `text`, return LintMatch.of(...) for each finding
    }
}
```

2. Add it to `RuleRegistry.allRules()` so both the one-shot CLI and the
   interactive shell pick it up.

The engine, sorting, `--ignore-rule` filtering, and both output formats
all work automatically for any rule that honors the `Rule` contract.

## Design notes

- **Dependency-free**: only the JDK standard library is used (`java.util.regex`, records, etc.).
- **Fault isolation**: if one rule throws, `LinterEngine` logs it and continues with the rest rather than failing the whole run.
- **Deterministic output**: matches are always sorted by line, then column, then rule id.
- Rule heuristics (`PassiveVoiceRule`, `CapitalizationRule`, `ReadabilityRule`'s syllable
  counting) are intentionally simple approximations, not full NLP. They're good
  starting points to swap out for smarter logic later without changing the `Rule` contract.
