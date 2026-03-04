package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Team;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposed as a REST resource via Spring Data REST.
 */
@Tag(name = "Match Results", description = "Repository for managing match scores and results")
@RepositoryRestResource
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

	@Override
	@RestResource(exported = false)
	<S extends MatchResult> S save(S entity);

	@Override
	@RestResource(exported = false)
	void delete(MatchResult entity);

	@Override
	@RestResource(exported = false)
	void deleteById(Long id);

	@Operation(summary = "Find results by match",
			description = "Returns a list of results associated with a specific match.")
	List<MatchResult> findByMatch(@Param("match") Match match);

	@Operation(summary = "Find results by team",
			description = "Returns a list of results achieved by a specific team.")
	List<MatchResult> findByTeam(@Param("team") Team team);

	boolean existsByMatch(@Param("match") Match match);

}
