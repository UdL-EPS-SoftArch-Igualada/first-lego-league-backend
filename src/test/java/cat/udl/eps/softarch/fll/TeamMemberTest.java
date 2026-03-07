package cat.udl.eps.softarch.fll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.domain.TeamMember;

class TeamMemberTest {
	private Team team;
	private TeamMember member;

	@BeforeEach
	void setUp() {
		team = new Team("UdL Eagles");
		member = new TeamMember();
		member.setName("Joan");
		member.setBirthDate(LocalDate.of(2005, 5, 20));
		member.setRole("Developer");
		member.setTeam(team);
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
		TeamMember member1 = new TeamMember();
		member1.setId(1L);

		TeamMember member2 = new TeamMember();
		member2.setId(1L);

		assertEquals(member1, member2, "Two members with the same ID should be equal");
	}
}
