package cat.udl.eps.softarch.fll.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainValidationTest {

	@Nested
	class RequireNonBlank {

		@Test
		void acceptsNonBlank() {
			assertDoesNotThrow(() -> DomainValidation.requireNonBlank("hello", "field"));
		}

		@Test
		void rejectsNull() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireNonBlank(null, "field"));
			assertEquals("field must not be null", ex.getMessage());
		}

		@Test
		void rejectsBlank() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireNonBlank("   ", "field"));
			assertEquals("field must not be blank", ex.getMessage());
		}

		@Test
		void rejectsEmpty() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireNonBlank("", "field"));
			assertEquals("field must not be blank", ex.getMessage());
		}
	}

	@Nested
	class RequireValidEmail {

		@Test
		void acceptsValidEmail() {
			assertDoesNotThrow(() -> DomainValidation.requireValidEmail("user@example.com", "email"));
		}

		@Test
		void rejectsNull() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireValidEmail(null, "email"));
		}

		@Test
		void rejectsInvalidFormat() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireValidEmail("not-an-email", "email"));
		}

		@Test
		void rejectsMissingDomain() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireValidEmail("user@", "email"));
		}

		@Test
		void rejectsMissingAt() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireValidEmail("userexample.com", "email"));
		}

		@Test
		void acceptsLocalPartWithSingleDot() {
			assertDoesNotThrow(() -> DomainValidation.requireValidEmail("first.last@example.com", "email"));
		}
	}

	@Nested
	class RequireNonNegative {

		@Test
		void acceptsZero() {
			assertDoesNotThrow(() -> DomainValidation.requireNonNegative(0, "score"));
		}

		@Test
		void acceptsPositive() {
			assertDoesNotThrow(() -> DomainValidation.requireNonNegative(100, "score"));
		}

		@Test
		void rejectsNull() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireNonNegative(null, "score"));
		}

		@Test
		void rejectsNegative() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireNonNegative(-1, "score"));
			assertEquals("score must not be negative", ex.getMessage());
		}
	}

	@Nested
	class RequireNonNull {

		@Test
		void acceptsNonNull() {
			assertDoesNotThrow(() -> DomainValidation.requireNonNull("abc", "id"));
		}

		@Test
		void rejectsNull() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireNonNull(null, "id"));
			assertEquals("id must not be null", ex.getMessage());
		}
	}

	@Nested
	class RequireMin {

		@Test
		void acceptsValueAtMinimum() {
			assertDoesNotThrow(() -> DomainValidation.requireMin(5, 5, "age"));
		}

		@Test
		void acceptsValueAboveMinimum() {
			assertDoesNotThrow(() -> DomainValidation.requireMin(10, 5, "age"));
		}

		@Test
		void rejectsNull() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireMin(null, 5, "age"));
		}

		@Test
		void rejectsBelowMinimum() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireMin(4, 5, "age"));
			assertEquals("age must not be less than 5", ex.getMessage());
		}
	}

	@Nested
	class RequirePast {

		@Test
		void acceptsDateInThePast() {
			LocalDate yesterday = LocalDate.now().minusDays(1);
			assertDoesNotThrow(() -> DomainValidation.requirePast(yesterday, "birthDate"));
		}

		@Test
		void rejectsNull() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requirePast(null, "birthDate"));
		}

		@Test
		void rejectsToday() {
			LocalDate today = LocalDate.now();
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requirePast(today, "birthDate"));
			assertEquals("birthDate must be in the past", ex.getMessage());
		}

		@Test
		void rejectsFutureDate() {
			LocalDate tomorrow = LocalDate.now().plusDays(1);
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requirePast(tomorrow, "birthDate"));
		}
	}

	@Nested
	class RequireLengthBetween {

		@Test
		void acceptsLengthAtMinimum() {
			assertDoesNotThrow(() -> DomainValidation.requireLengthBetween("ab", 2, 5, "code"));
		}

		@Test
		void acceptsLengthAtMaximum() {
			assertDoesNotThrow(() -> DomainValidation.requireLengthBetween("abcde", 2, 5, "code"));
		}

		@Test
		void acceptsLengthWithinBounds() {
			assertDoesNotThrow(() -> DomainValidation.requireLengthBetween("abc", 2, 5, "code"));
		}

		@Test
		void rejectsNull() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireLengthBetween(null, 2, 5, "code"));
		}

		@Test
		void rejectsBelowMinimum() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireLengthBetween("a", 2, 5, "code"));
			assertEquals("code length must not be less than 2", ex.getMessage());
		}

		@Test
		void rejectsAboveMaximum() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireLengthBetween("abcdef", 2, 5, "code"));
			assertEquals("code length must not be more than 5", ex.getMessage());
		}
	}

	@Nested
	class RequireMaxLength {

		@Test
		void acceptsLengthAtMaximum() {
			assertDoesNotThrow(() -> DomainValidation.requireMaxLength("abcde", 5, "name"));
		}

		@Test
		void acceptsLengthBelowMaximum() {
			assertDoesNotThrow(() -> DomainValidation.requireMaxLength("abc", 5, "name"));
		}

		@Test
		void acceptsEmptyString() {
			assertDoesNotThrow(() -> DomainValidation.requireMaxLength("", 5, "name"));
		}

		@Test
		void rejectsNull() {
			assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireMaxLength(null, 5, "name"));
		}

		@Test
		void rejectsAboveMaximum() {
			DomainValidationException ex = assertThrows(DomainValidationException.class,
				() -> DomainValidation.requireMaxLength("abcdef", 5, "name"));
			assertEquals("name length must not be more than 5", ex.getMessage());
		}
	}
}
