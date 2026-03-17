package cat.udl.eps.softarch.fll.competition;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CompetitionTableValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> CompetitionTable.create("TABLE-1"));
	}

	@Nested
	class NullId {

		@Test
		void nullIdThrows() {
			assertThrows(DomainValidationException.class,
					() -> CompetitionTable.create(null));
		}

		@Test
		void blankIdThrows() {
			assertThrows(DomainValidationException.class,
					() -> CompetitionTable.create("  "));
		}

		@Test
		void emptyIdThrows() {
			assertThrows(DomainValidationException.class,
					() -> CompetitionTable.create(""));
		}
	}
}
