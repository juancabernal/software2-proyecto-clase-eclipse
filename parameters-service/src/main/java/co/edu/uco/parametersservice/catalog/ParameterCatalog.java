package co.edu.uco.parametersservice.catalog;

import java.util.HashMap;
import java.util.Map;

public class ParameterCatalog {

	private static Map<String, Parameter> parameters = new HashMap<>(
	);
	
	static {
		parameters.put("FechaDefectoMaxima", new Parameter("FechaDefectoMaxima","31/12/2500"));
		parameters.put("correoAdministrador", new Parameter("correoAdministrador","admin@uco.edu.co"));
		parameters.put("numeroMaximoReintentosEnvioCorrec", new Parameter("numeroMaximoReintentosEnvioCorrec","8"));
	}
	
	public static Parameter getParameterValue(String key) {
		return parameters.get(key);
	}
	
	public static void synchronizeParameterValue(Parameter parameter) {
		parameters.put(parameter.getKey(),parameter);
	}
	
	public static Parameter removeParameter(String key) {
		return parameters.remove(key);
	}
	
	public static Map<String, Parameter> getAllParameters() {
		return parameters;
	}
	
}
