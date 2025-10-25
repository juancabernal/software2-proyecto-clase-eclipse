package co.edu.uco.ucochallenge.crosscuting.exception;

/**
 * Defines the logical layer where an exception originates. The order reflects the
 * propagation direction from the innermost layer (DOMAIN) to the outermost one
 * (GENERAL).
 */
public enum ExceptionLayer {

    GENERAL,
    USECASE,
    CONTROLLER,
    DTO,
    APPLICATION,
    RULE,
    EMAIL,
    ENTITY,
    DOMAIN
}
