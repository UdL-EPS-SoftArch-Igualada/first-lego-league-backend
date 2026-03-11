package cat.udl.eps.softarch.fll.domain;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class DomainValidation {

	private static final Pattern EMAIL_PATTERN =
		Pattern.compile("^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,}$");

	private DomainValidation() {
	}

	public static void requireNonBlank(String value, String fieldName) {
		if (value == null) {
			throw new DomainValidationException(fieldName + " must not be null");
		}
		if (value.isBlank()) {
			throw new DomainValidationException(fieldName + " must not be blank");
		}
	}

	public static void requireValidEmail(String email, String fieldName) {
		requireNonBlank(email, fieldName);
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new DomainValidationException(fieldName + " must be a valid email address");
		}
	}

	public static void requireNonNegative(Integer value, String fieldName) {
		if (value == null) {
			throw new DomainValidationException(fieldName + " must not be null");
		}
		if (value < 0) {
			throw new DomainValidationException(fieldName + " must not be negative");
		}
	}

	public static void requireMin(Integer value, Integer minValue, String fieldName) {
		if (value == null) {
			throw new DomainValidationException(fieldName + " must not be null");
		}
		if (value < minValue) {
			throw new DomainValidationException(fieldName + " must not be less than " + minValue);
		}
	}

	public static void requirePast(LocalDate value, String fieldName) {
		if (value == null) {
			throw new DomainValidationException(fieldName + " must not be null");
		}
		if (!value.isBefore(LocalDate.now())) {
			throw new DomainValidationException(fieldName + " must be in the past");
		}
	}

	public static void requireLengthBetween(String value, Integer minLength, Integer maxLength, String fieldName) {
		if (value == null) {
			throw new DomainValidationException(fieldName + " must not be null");
		}
		if (value.length() < minLength) {
			throw new DomainValidationException(fieldName + " length must not be less than " + minLength);
		}
		if (value.length() > maxLength) {
			throw new DomainValidationException(fieldName + " length must not be more than " + maxLength);
		}
	}

	public static void requireMaxLength(String value, Integer maxLength, String fieldName) {
		if (value == null) {
			throw new DomainValidationException(fieldName + " must not be null");
		}
		if (value.length() > maxLength) {
			throw new DomainValidationException(fieldName + " length must not be more than " + maxLength);
		}
	}

	public static void requireNonNull(Object value, String fieldName) {
		if (value == null) {
			throw new DomainValidationException(fieldName + " must not be null");
		}
	}
}
