package cat.udl.eps.softarch.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.demo.domain.TeamMember;

@RepositoryRestResource(path = "members")
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

	List<TeamMember> findByName(@Param("name") String name);

	List<TeamMember> findByTeamName(@Param("teamName") String teamName);
}
