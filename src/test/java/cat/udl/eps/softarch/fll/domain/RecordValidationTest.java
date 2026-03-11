package cat.udl.eps.softarch.fll.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RecordValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> Record.create("My Record"));
	}

	@Nested
	class EmptyName {

		@Test
		void nullNameThrows() {
			assertThrows(DomainValidationException.class, () -> Record.create(null));
		}

		@Test
		void blankNameThrows() {
			assertThrows(DomainValidationException.class, () -> Record.create("  "));
		}

		@Test
		void emptyNameThrows() {
			assertThrows(DomainValidationException.class, () -> Record.create(""));
		}
	}
}
