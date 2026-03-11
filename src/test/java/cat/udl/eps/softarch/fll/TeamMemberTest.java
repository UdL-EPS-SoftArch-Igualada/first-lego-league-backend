package cat.udl.eps.softarch.fll;

import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.domain.TeamMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TeamMemberTest {
	private TeamMember member;

	@BeforeEach
	void setUp() {
		Team team = Team.create("UdL Eagles", "Igualada", 2005, "Junior");
		member = TeamMember.create("Joan", "Developer", LocalDate.of(2005, 5, 20), team);
	}

	@Test
	@DisplayName("Store basic member data")
	void testMemberData() {
		assertEquals("Joan", member.getName());
		assertEquals("Developer", member.getRole());
		assertEquals(LocalDate.of(2005, 5, 20), member.getBirthDate());
	}

	@Test
	@DisplayName("Keep relation with team")
	void testMemberTeamRelation() {
		assertNotNull(member.getTeam());
		assertEquals("UdL Eagles", member.getTeam().getName());
	}

	@Test
	@DisplayName("Compare equality by ID")
	void testEquals() {
		TeamMember member1 = TeamMember.create("Joan", "Developer", LocalDate.of(2005, 5, 20), member.getTeam());
		member1.setId(1L);

		TeamMember member2 = TeamMember.create("Pep", "Developer", LocalDate.of(2005, 5, 20), member.getTeam());
		member2.setId(1L);

		assertEquals(member1, member2, "Two members with the same ID should be equal");
	}
}
