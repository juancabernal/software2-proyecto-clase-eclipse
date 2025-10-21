package co.edu.uco.ucochallenge.application.usecase.domain;

import java.util.UUID;

import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.CityEntity;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.IdTypeEntity;

public class RegisterUserDomain {
	
	private UUID idType;
	private String idNumber;
	private String firstName;
	private String secondName;
	private String firstSurname;
	private String secondSurname;
	private UUID homeCity;
	private String email;
	private String mobileNumber;

}
