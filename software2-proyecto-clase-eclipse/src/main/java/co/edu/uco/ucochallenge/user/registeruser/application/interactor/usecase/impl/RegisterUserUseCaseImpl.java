package co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.impl;

import org.springframework.stereotype.Service;
import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.secondary.ports.repository.UserRepository;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.RegisterUserUseCase;
import co.edu.uco.ucochallenge.user.registeruser.application.usecase.domain.RegisterUserDomain;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.RegisterUserContext;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.RegisterUserRuleNames;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.RuleEngine;

@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

	private final UserRepository repository;
	private final RuleEngine ruleEngine;

	public RegisterUserUseCaseImpl(UserRepository repository, RuleEngine ruleEngine) {
		this.repository = repository;
		this.ruleEngine = ruleEngine;
	}

	@Override
	public Void execute(final RegisterUserDomain domain) {

		RegisterUserContext context = new RegisterUserContext(domain);

		applyFormatRules(context);
		applyUniquenessRules(context);

		// DataMapper/MapStruct could be used here
		UserEntity userEntity = null; // Mapping from Domain to Entity is needed

		repository.save(userEntity); // Solo por el momento porque hay que convertir de domain a entity
		return Void.returnVoid();
	}

	private void applyFormatRules(RegisterUserContext context) {
		ruleEngine.applyRule(RegisterUserRuleNames.ID_NUMBER_FORMAT, context);
		ruleEngine.applyRule(RegisterUserRuleNames.EMAIL_FORMAT, context);
		ruleEngine.applyRule(RegisterUserRuleNames.MOBILE_NUMBER_FORMAT, context);
	}

	private void applyUniquenessRules(RegisterUserContext context) {
		ruleEngine.applyRule(RegisterUserRuleNames.UNIQUE_ID_TYPE_NUMBER, context);
		ruleEngine.applyRule(RegisterUserRuleNames.UNIQUE_EMAIL, context);
		ruleEngine.applyRule(RegisterUserRuleNames.UNIQUE_MOBILE_NUMBER, context);
	}
}
