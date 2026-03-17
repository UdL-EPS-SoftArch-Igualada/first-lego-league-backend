package cat.udl.eps.softarch.fll.volunteer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import cat.udl.eps.softarch.fll.domain.volunteer.Floater;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FloaterValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(
				() -> Floater.create("Alice", "alice@example.com", "123456789", "STU001"));
	}

	@Nested
	class EmptyName {

		@Test
		void nullNameThrows() {
			assertThrows(DomainValidationException.class,
					() -> Floater.create(null, "alice@example.com", "123456789", "STU001"));
		}

		@Test
		void blankNameThrows() {
			assertThrows(DomainValidationException.class,
					() -> Floater.create("", "alice@example.com", "123456789", "STU001"));
		}
	}

	@Nested
	class InvalidEmail {

		@Test
		void invalidEmailThrows() {
			assertThrows(DomainValidationException.class,
					() -> Floater.create("Alice", "not-valid", "123456789", "STU001"));
		}
	}

	@Nested
	class NullRequiredField {

		@Test
		void nullStudentCodeThrows() {
			assertThrows(DomainValidationException.class,
					() -> Floater.create("Alice", "alice@example.com", "123456789", null));
		}

		@Test
		void blankStudentCodeThrows() {
			assertThrows(DomainValidationException.class,
					() -> Floater.create("Alice", "alice@example.com", "123456789", "  "));
		}
	}
}
