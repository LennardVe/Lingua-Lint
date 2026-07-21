package com.lingualint;

import com.lingualint.rules.CapitalizationRule;
import com.lingualint.rules.FillerWordRule;
import com.lingualint.rules.PassiveVoiceRule;
import com.lingualint.rules.ReadabilityRule;
import com.lingualint.rules.RepeatedWordRule;
import com.lingualint.rules.Rule;

import java.util.List;
import java.util.Set;

/**
  Central list of built-in rules, shared by every entry point (one-shot
  CLI, interactive shell, tests, ...). Add a new rule here to make it
  available everywhere at once.
 */
public final class RuleRegistry {

    public static List<Rule> allRules() {
        return List.of(
                new RepeatedWordRule(),
                new PassiveVoiceRule(),
                new CapitalizationRule(),
                new FillerWordRule(),
                new ReadabilityRule()
        );
    }

    // Builds an engine with every built-in rule except the given ids.
    public static LinterEngine buildEngine(Set<String> ignoredRuleIds) {
        LinterEngine engine = new LinterEngine();
        for (Rule rule : allRules()) {
            if (!ignoredRuleIds.contains(rule.getId())) {
                engine.registerRule(rule);
            }
        }
        return engine;
    }

    private RuleRegistry() {
    }
}
