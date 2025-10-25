package co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.impl;

import co.edu.uco.ucochallenge.application.interactor.usecase.rules.Rule;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.RegisterUserContext;
import co.edu.uco.ucochallenge.crosscuting.exception.ExceptionLayer;
import co.edu.uco.ucochallenge.crosscuting.exception.UcoChallengeException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageKey;
import co.edu.uco.ucochallenge.secondary.ports.repository.UserRepository;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.RegisterUserRuleNames;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class UniqueMobileNumber implements Rule<RegisterUserContext> {

	private final UserRepository userRepository;

	public UniqueMobileNumber(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public String getName() {
		return RegisterUserRuleNames.UNIQUE_MOBILE_NUMBER;
	}

	@Override
	public boolean evaluate(RegisterUserContext context) {
		String mobileNumber = TextHelper.getDefaultWithTrim(context.getMobileNumber());

		if (TextHelper.isEmpty(mobileNumber)) {
			return true;
		}

		try {
			if (userRepository.existsByMobileNumber(mobileNumber)) {
				throw UcoChallengeException.createUserException(ExceptionLayer.RULE,
						MessageKey.RegisterUser.RULE_MOBILE_DUPLICATED_OWNER);
			}
		} catch (DataAccessException exception) {
			throw UcoChallengeException.createTechnicalException(ExceptionLayer.RULE,
					MessageKey.GENERAL_TECHNICAL_ERROR, exception);
		}

		return true;
	}
}
