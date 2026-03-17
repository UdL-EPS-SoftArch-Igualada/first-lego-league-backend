package cat.udl.eps.softarch.fll.team;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import cat.udl.eps.softarch.fll.domain.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> User.create("admin", "admin@example.com", "securepass"));
	}

	@Nested
	class NullId {

		@Test
		void nullIdThrows() {
			assertThrows(DomainValidationException.class,
					() -> User.create(null, "admin@example.com", "securepass"));
		}

		@Test
		void blankIdThrows() {
			assertThrows(DomainValidationException.class,
					() -> User.create("  ", "admin@example.com", "securepass"));
		}
	}

	@Nested
	class InvalidEmail {

		@Test
		void nullEmailThrows() {
			assertThrows(DomainValidationException.class,
					() -> User.create("admin", null, "securepass"));
		}

		@Test
		void invalidEmailFormatThrows() {
			assertThrows(DomainValidationException.class,
					() -> User.create("admin", "not-an-email", "securepass"));
		}

		@Test
		void blankEmailThrows() {
			assertThrows(DomainValidationException.class,
					() -> User.create("admin", "", "securepass"));
		}
	}

	@Nested
	class EmptyPassword {

		@Test
		void nullPasswordThrows() {
			assertThrows(DomainValidationException.class,
					() -> User.create("admin", "admin@example.com", null));
		}

		@Test
		void blankPasswordThrows() {
			assertThrows(DomainValidationException.class,
					() -> User.create("admin", "admin@example.com", "  "));
		}
	}
}
