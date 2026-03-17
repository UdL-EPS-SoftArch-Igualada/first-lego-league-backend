package cat.udl.eps.softarch.fll.match;

import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Team;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MatchResultValidationTest {

	private final Match validMatch = new Match();
	private final Team validTeam = Team.create("ValidTeam", "Barcelona", 2000, "category");

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> MatchResult.create(100, validMatch, validTeam));
	}

	@Test
	void zeroScoreIsValid() {
		assertDoesNotThrow(() -> MatchResult.create(0, validMatch, validTeam));
	}

	@Nested
	class NegativeScore {

		@Test
		void negativeScoreThrows() {
			assertThrows(DomainValidationException.class,
				() -> MatchResult.create(-1, validMatch, validTeam));
		}

		@Test
		void nullScoreThrows() {
			assertThrows(DomainValidationException.class,
				() -> MatchResult.create(null, validMatch, validTeam));
		}
	}

	@Nested
	class NullRequiredReference {

		@Test
		void nullMatchThrows() {
			assertThrows(DomainValidationException.class,
				() -> MatchResult.create(50, null, validTeam));
		}

		@Test
		void nullTeamThrows() {
			assertThrows(DomainValidationException.class,
				() -> MatchResult.create(50, validMatch, null));
		}
	}
}
