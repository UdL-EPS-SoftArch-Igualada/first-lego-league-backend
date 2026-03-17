package cat.udl.eps.softarch.fll.team;

import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.domain.TeamMember;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamMemberValidationTest {

	private final Team validTeam = Team.create("TestTeam", "Barcelona", 2000, "category");

	@Test
	void validConstruction() {
		LocalDate birthDate = LocalDate.ofYearDay(0, 1);
		assertDoesNotThrow(() -> TeamMember.create("Alice", "Player", birthDate, validTeam));
	}

	@Test
	void futureDateThrows() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		assertThrows(DomainValidationException.class,
			() -> TeamMember.create("Alice", "Player", tomorrow, validTeam));
	}

	@Nested
	class EmptyName {

		@Test
		void nullNameThrows() {
			LocalDate birthDate = LocalDate.ofYearDay(0, 1);
			assertThrows(DomainValidationException.class,
				() -> TeamMember.create(null, "Player", birthDate, validTeam));
		}

		@Test
		void blankNameThrows() {
			LocalDate birthDate = LocalDate.ofYearDay(0, 1);
			assertThrows(DomainValidationException.class,
				() -> TeamMember.create("  ", "Player", birthDate, validTeam));
		}

		@Test
		void blankRoleThrows() {
			LocalDate birthDate = LocalDate.ofYearDay(1, 1);
			assertThrows(DomainValidationException.class,
				() -> TeamMember.create("Alice", "", birthDate, validTeam));
		}
	}

	@Nested
	class NullRequiredReference {

		@Test
		void nullTeamThrows() {
			LocalDate birthDate = LocalDate.ofYearDay(0, 1);
			assertThrows(DomainValidationException.class,
				() -> TeamMember.create("Alice", "Player", birthDate, null));
		}
	}
}
