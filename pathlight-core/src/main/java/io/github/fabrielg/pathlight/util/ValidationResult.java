package io.github.fabrielg.pathlight.util;

/**
 * Represents the result of a validation check.
 * Contains whether the check passed and an optional error message.
 */
public class ValidationResult {

	private final boolean valid;
	private final String errorMessage;

	public ValidationResult(boolean valid, String errorMessage) {
		this.valid = valid;
		this.errorMessage = errorMessage;
	}

	public static ValidationResult ok() {
		return new ValidationResult(true, null);
	}

	public static ValidationResult error(String message) {
		return new ValidationResult(false, message);
	}

	public boolean isValid() { return valid; }
	public String getErrorMessage() { return errorMessage; }

	@Override
	public String toString() {
		return valid ? "OK" : "ERROR: " + errorMessage;
	}

}
