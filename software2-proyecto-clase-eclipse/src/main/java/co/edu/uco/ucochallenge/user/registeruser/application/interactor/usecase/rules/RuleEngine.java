package co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules;


import co.edu.uco.ucochallenge.application.interactor.usecase.rules.Rule;
import org.springframework.stereotype.Service;

@Service
public class RuleEngine {
    private final RuleRegistry ruleRegistry;

    public RuleEngine(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    public boolean applyRule(String ruleName, RegisterUserContext context) {
        Rule rule = ruleRegistry.getRule(ruleName);
        if (rule == null) {
            throw new IllegalArgumentException("Unknown rule: " + ruleName);
        }
        return rule.evaluate(context);
    }
}
