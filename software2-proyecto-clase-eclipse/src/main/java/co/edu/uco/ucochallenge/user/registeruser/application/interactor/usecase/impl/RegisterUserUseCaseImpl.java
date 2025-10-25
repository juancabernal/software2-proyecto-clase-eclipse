package co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.impl;

import org.springframework.stereotype.Service;
import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.secondary.ports.repository.UserRepository;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.RegisterUserUseCase;
import co.edu.uco.ucochallenge.user.registeruser.application.usecase.domain.RegisterUserDomain;


@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {
	
	private UserRepository repository;
	
	
	
	public RegisterUserUseCaseImpl(UserRepository repository) {
		this.repository = repository;
	}



	@Override
	public Void execute(final RegisterUserDomain domain) {
		
		//DataMapper/MapStruct could be used here
		UserEntity userEntity = null; // Mapping from Domain to Entity is needed
		
		repository.save(userEntity); //Solo por el momento porque hay que convertir de domain a entity
		return Void.returnVoid();
	}

}
