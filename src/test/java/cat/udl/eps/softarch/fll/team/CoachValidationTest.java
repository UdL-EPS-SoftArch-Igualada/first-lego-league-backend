package cat.udl.eps.softarch.fll.team;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cat.udl.eps.softarch.fll.domain.Coach;
import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CoachValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> Coach.create("John Doe", "john@example.com"));
	}

	@Nested
	class EmptyName {

		@Test
		void nullNameThrows() {
			assertThrows(DomainValidationException.class,
					() -> Coach.create(null, "john@example.com"));
		}

		@Test
		void blankNameThrows() {
			assertThrows(DomainValidationException.class,
					() -> Coach.create("  ", "john@example.com"));
		}
	}

	@Nested
	class InvalidEmail {

		@Test
		void nullEmailThrows() {
			assertThrows(DomainValidationException.class,
					() -> Coach.create("John Doe", null));
		}

		@Test
		void blankEmailThrows() {
			assertThrows(DomainValidationException.class,
					() -> Coach.create("John Doe", ""));
		}

		@Test
		void invalidEmailFormatThrows() {
			assertThrows(DomainValidationException.class,
					() -> Coach.create("John Doe", "not-an-email"));
		}

		@Test
		void emailWithoutDomainThrows() {
			assertThrows(DomainValidationException.class,
					() -> Coach.create("John Doe", "john@"));
		}
	}
}
