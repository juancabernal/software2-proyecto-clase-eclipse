package co.edu.uco.ucochallenge.user.registeruser.application.usecase.rules;

import co.edu.uco.ucochallenge.user.registeruser.application.usecase.rules.context.ContextRegisterUser;
import org.springframework.stereotype.Service;

@Service
public class RuleEngine {

    private final RuleRegistry ruleRegistry;

    public RuleEngine(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    public boolean applyRule(String ruleName, ContextRegisterUser context) {
        Rule rule = ruleRegistry.getRule(ruleName);
        if (rule == null) {
            throw new IllegalArgumentException("Unknown rule: " + ruleName);
        }
        return rule.evaluate(context);
    }
}