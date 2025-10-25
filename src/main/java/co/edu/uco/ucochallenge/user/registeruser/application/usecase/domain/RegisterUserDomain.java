package co.edu.uco.ucochallenge.user.registeruser.application.usecase.domain;

import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.exception.UcoChallengeException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;

public final class RegisterUserDomain {

	private final UUID idType;
	private final String idNumber;
	private final String firstName;
	private final String secondName;
	private final String firstSurname;
	private final String secondSurname;
	private final UUID homeCity;
	private final String email;
	private final String mobileNumber;


	private RegisterUserDomain(
			final UUID idType,
			final String idNumber,
			final String firstName,
			final String secondName,
			final String firstSurname,
			final String secondSurname,
			final UUID homeCity,
			final String email,
			final String mobileNumber) {

		this.idType = validarTipoIdentificacion(idType);
		this.idNumber = validarNumeroIdentificacion(idNumber);
		this.firstName = validarNombre(firstName, "primer nombre");
		this.secondName = validarNombre(secondName, "segundo nombre");
		this.firstSurname = validarNombre(firstSurname, "primer apellido");
		this.secondSurname = validarNombre(secondSurname, "segundo apellido");
		this.homeCity = validarCiudadResidencia(homeCity);
		this.email = validarCorreo(email);
		this.mobileNumber = validarNumeroCelular(mobileNumber);
	}


	private UUID validarTipoIdentificacion(final UUID idType) {
		if (idType == null || idType.equals(UUIDHelper.getDefault())) {
			throw UcoChallengeException.build("El tipo de identificación es obligatorio y debe ser válido.");
		}

		// Aquí asumimos que la existencia se valida desde el dominio:
		if (!tipoIdentificacionExiste(idType)) {
			throw UcoChallengeException.build("El tipo de identificación indicado no existe en el sistema.");
		}

		return idType;
	}

	private String validarNumeroIdentificacion(final String idNumber) {
		if (TextHelper.isEmpty(idNumber)) {
			throw UcoChallengeException.build("El número de identificación es obligatorio.");
		}
		if (!idNumber.matches("^[0-9]+$")) {
			throw UcoChallengeException.build("El número de identificación solo puede contener dígitos.");
		}
		if (idNumber.length() < 5 || idNumber.length() > 20) {
			throw UcoChallengeException.build("El número de identificación debe tener entre 5 y 20 dígitos.");
		}
		return idNumber.trim();
	}

	private String validarNombre(final String nombre, final String campo) {
		if (TextHelper.isEmpty(nombre)) {
			throw UcoChallengeException.build("El " + campo + " es obligatorio.");
		}
		if (!nombre.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
			throw UcoChallengeException.build("El " + campo + " solo puede contener letras y espacios.");
		}
		if (nombre.trim().length() < 2 || nombre.trim().length() > 40) {
			throw UcoChallengeException.build("El " + campo + " debe tener entre 2 y 40 caracteres.");
		}
		return nombre.trim();
	}

	private UUID validarCiudadResidencia(final UUID homeCity) {
		if (homeCity == null || homeCity.equals(UUIDHelper.getDefault())) {
			throw UcoChallengeException.build("La ciudad de residencia es obligatoria y debe ser válida.");
		}

		if (!ciudadExiste(homeCity)) {
			throw UcoChallengeException.build("La ciudad de residencia indicada no existe en el sistema.");
		}

		return homeCity;
	}

	private String validarCorreo(final String email) {
		if (TextHelper.isEmpty(email)) {
			throw UcoChallengeException.build("El correo electrónico es obligatorio.");
		}
		if (email.length() < 10 || email.length() > 100) {
			throw UcoChallengeException.build("El correo electrónico debe tener entre 10 y 100 caracteres.");
		}
		if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
			throw UcoChallengeException.build("El formato del correo electrónico no es válido.");
		}
		return email.trim().toLowerCase();
	}

	private String validarNumeroCelular(final String mobileNumber) {
		if (TextHelper.isEmpty(mobileNumber)) {
			throw UcoChallengeException.build("El número de teléfono móvil es obligatorio.");
		}
		if (!mobileNumber.matches("^[0-9]{10}$")) {
			throw UcoChallengeException.build("El número de teléfono móvil debe contener exactamente 10 dígitos.");
		}
		return mobileNumber.trim();
	}


	private boolean tipoIdentificacionExiste(final UUID idType) {
		// En una implementación real, aquí se consultaría el repositorio de tipos de identificación.
		// En el dominio puro, se asume que la existencia puede validarse a través de un servicio.
		// Aquí devolvemos true para fines demostrativos.
		return true;
	}

	private boolean ciudadExiste(final UUID homeCity) {
		// En una implementación real, aquí se consultaría el repositorio de ciudades.
		return true;
	}


	public static Builder construir() {
		return new Builder();
	}

	public static final class Builder {
		private UUID idType;
		private String idNumber;
		private String firstName;
		private String secondName;
		private String firstSurname;
		private String secondSurname;
		private UUID homeCity;
		private String email;
		private String mobileNumber;

		private Builder() {
		}

		public Builder conTipoIdentificacion(final UUID idType) {
			this.idType = idType;
			return this;
		}

		public Builder conNumeroIdentificacion(final String idNumber) {
			this.idNumber = idNumber;
			return this;
		}

		public Builder conPrimerNombre(final String firstName) {
			this.firstName = firstName;
			return this;
		}

		public Builder conSegundoNombre(final String secondName) {
			this.secondName = secondName;
			return this;
		}

		public Builder conPrimerApellido(final String firstSurname) {
			this.firstSurname = firstSurname;
			return this;
		}

		public Builder conSegundoApellido(final String secondSurname) {
			this.secondSurname = secondSurname;
			return this;
		}

		public Builder conCiudadResidencia(final UUID homeCity) {
			this.homeCity = homeCity;
			return this;
		}

		public Builder conCorreo(final String email) {
			this.email = email;
			return this;
		}

		public Builder conNumeroCelular(final String mobileNumber) {
			this.mobileNumber = mobileNumber;
			return this;
		}

		public RegisterUserDomain construir() {
			return new RegisterUserDomain(
					idType,
					idNumber,
					firstName,
					secondName,
					firstSurname,
					secondSurname,
					homeCity,
					email,
					mobileNumber
			);
		}
	}


	public UUID getIdType() {
		return idType;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getSecondName() {
		return secondName;
	}

	public String getFirstSurname() {
		return firstSurname;
	}

	public String getSecondSurname() {
		return secondSurname;
	}

	public UUID getHomeCity() {
		return homeCity;
	}

	public String getEmail() {
		return email;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}
}
