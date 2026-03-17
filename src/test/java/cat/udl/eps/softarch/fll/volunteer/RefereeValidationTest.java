package cat.udl.eps.softarch.fll.volunteer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import cat.udl.eps.softarch.fll.domain.volunteer.Referee;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RefereeValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> Referee.create("Jane Doe", "jane@example.com", "123456789"));
	}

	@Nested
	class EmptyName {

		@Test
		void nullNameThrows() {
			assertThrows(DomainValidationException.class,
					() -> Referee.create(null, "jane@example.com", "123456789"));
		}

		@Test
		void blankNameThrows() {
			assertThrows(DomainValidationException.class,
					() -> Referee.create("  ", "jane@example.com", "123456789"));
		}
	}

	@Nested
	class InvalidEmail {

		@Test
		void invalidEmailThrows() {
			assertThrows(DomainValidationException.class,
					() -> Referee.create("Jane Doe", "bad-email", "123456789"));
		}

		@Test
		void nullEmailThrows() {
			assertThrows(DomainValidationException.class,
					() -> Referee.create("Jane Doe", null, "123456789"));
		}
	}

	@Nested
	class NullRequiredField {

		@Test
		void nullPhoneThrows() {
			assertThrows(DomainValidationException.class,
					() -> Referee.create("Jane Doe", "jane@example.com", null));
		}
	}
}
