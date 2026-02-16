package cat.udl.eps.softarch.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.demo.domain.Team;

@RepositoryRestResource(collectionResourceRel = "teams", path = "teams")
public interface TeamRepository extends JpaRepository<Team, String> {
	List<Team> findByCity(@Param("city") String city);

	List<Team> findByFoundationYearGreaterThan(int year);

	List<Team> findByEducationalCenter(String educationalCenter);

	List<Team> findByCategory(String category);

	List<Team> findByMembersRole(@Param("role") String role);

	List<Team> findByNameContainingIgnoreCase(@Param("name") String name);
}
