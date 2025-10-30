package co.edu.uco.ucochallenge.application;

public final class Void extends Response<java.lang.Void> {

        protected Void() {
                super(false, null);
        }
	
	public static Void returnVoid() {
		return new Void();
	}


}
