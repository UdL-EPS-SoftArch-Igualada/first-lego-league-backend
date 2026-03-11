package cat.udl.eps.softarch.fll.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> Team.create("Robotics", "Lleida", 2020, "Challenge"));
	}

	@Test
	void invalidMinFoundationYearConstruction() {
		assertThrows(DomainValidationException.class, () -> Team.create("Robotics", "Barcelona", 0, "category"));
	}

	@Test
	void nullNameThrows() {
		DomainValidationException ex = assertThrows(DomainValidationException.class,
			() -> Team.create(null, "Lleida", 2020, "Challenge"));
		assertEquals("name must not be null", ex.getMessage());
	}

	@Test
	void nullCityThrows() {
		DomainValidationException ex = assertThrows(DomainValidationException.class,
			() -> Team.create("Robotics", null, 2000, "category"));
		assertEquals("city must not be null", ex.getMessage());
	}

	@Nested
	class EmptyName {

		@Test
		void blankNameThrows() {
			assertThrows(DomainValidationException.class,
				() -> Team.create("   ", "Lleida", 2020, "Challenge"));
		}

		@Test
		void emptyNameThrows() {
			assertThrows(DomainValidationException.class,
				() -> Team.create("", "Lleida", 2020, "Challenge"));
		}

		@Test
		void blankCityThrows() {
			assertThrows(DomainValidationException.class,
				() -> Team.create("Robotics", "  ", 2020, "Challenge"));
		}

		@Test
		void blankCategoryThrows() {
			assertThrows(DomainValidationException.class,
				() -> Team.create("Robotics", "Lleida", 2020, ""));
		}
	}

	@Nested
	class NullRequiredField {

		@Test
		void nullFoundationYearThrows() {
			assertThrows(DomainValidationException.class,
				() -> Team.create("Robotics", "Lleida", null, "Challenge"));
		}
	}
}
