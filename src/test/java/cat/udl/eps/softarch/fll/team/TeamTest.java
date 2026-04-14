package cat.udl.eps.softarch.fll.team;

import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.domain.TeamMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamTest {
	private Team team;

	@BeforeEach
	void setUp() {
		team = Team.create("UdL Eagles", "Igualada", 2000, "Senior");
	}

	@Test
	@DisplayName("Validate max 10 team members")
	void testMemberLimit() {
		for (int i = 0; i < 10; i++) {
			TeamMember.create("Member " + i, "role" + i, LocalDate.of(2000, 1, 1), team);
		}
		LocalDate birthDate = LocalDate.of(2000, 1, 1);
		assertThrows(IllegalStateException.class, () -> TeamMember.create("ExtraMember", "role" + 10, birthDate, team));
	}

	@Test
	@DisplayName("Set inscription date on prePersist when null")
	void testPrePersistDate() {
		LocalDate before = LocalDate.now();
		team.setInscriptionDate(null);
		team.prePersist();
		LocalDate after = LocalDate.now();

		assertNotNull(team.getInscriptionDate());
		assertFalse(team.getInscriptionDate().isBefore(before));
		assertFalse(team.getInscriptionDate().isAfter(after));
	}

	@Test
	@DisplayName("Keep existing inscription date on prePersist")
	void testPrePersistWithExistingDate() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		team.setInscriptionDate(yesterday);
		team.prePersist();
		assertEquals(yesterday, team.getInscriptionDate());
	}
}
