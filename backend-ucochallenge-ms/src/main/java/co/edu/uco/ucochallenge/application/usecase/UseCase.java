package co.edu.uco.ucochallenge.application.usecase;

public interface UseCase<D, R> {
	R execute(D domain);

}
 	