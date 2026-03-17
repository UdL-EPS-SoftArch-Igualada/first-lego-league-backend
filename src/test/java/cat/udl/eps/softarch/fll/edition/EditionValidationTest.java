package cat.udl.eps.softarch.fll.edition;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import cat.udl.eps.softarch.fll.domain.Edition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EditionValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> Edition.create(2024, "Lleida Arena", "FLL Season"));
	}

	@Nested
	class NullRequiredField {

		@Test
		void nullYearThrows() {
			assertThrows(DomainValidationException.class,
					() -> Edition.create(null, "Lleida Arena", "FLL Season"));
		}
	}

	@Nested
	class EmptyName {

		@Test
		void blankVenueNameThrows() {
			assertThrows(DomainValidationException.class,
					() -> Edition.create(2024, "  ", "FLL Season"));
		}

		@Test
		void nullVenueNameThrows() {
			assertThrows(DomainValidationException.class,
					() -> Edition.create(2024, null, "FLL Season"));
		}

		@Test
		void blankDescriptionThrows() {
			assertThrows(DomainValidationException.class,
					() -> Edition.create(2024, "Lleida Arena", ""));
		}
	}
}
