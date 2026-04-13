package cat.udl.eps.softarch.fll.repository.match;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.MatchResult;
import cat.udl.eps.softarch.fll.domain.Team;
import cat.udl.eps.softarch.fll.repository.ranking.projection.LeaderboardRowProjection;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Match Results", description = "Repository for managing match scores and results")
@RepositoryRestResource
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

	boolean existsByTeam_IdAndMatch_Round_Id(Long teamId, Long roundId);

	@Query("""
        SELECT 
            mr.team.id as teamId,
            mr.team.name as teamName,
            SUM(mr.score) as totalScore
        FROM MatchResult mr
        GROUP BY mr.team.id, mr.team.name
        ORDER BY totalScore DESC
    """)
	List<LeaderboardRowProjection> getLeaderboard();

	@Override
	@RestResource(exported = false)
	<S extends MatchResult> S save(S entity);

	@Override
	@RestResource(exported = false)
	void delete(MatchResult entity);

	@Override
	@RestResource(exported = false)
	void deleteById(Long id);

	@Operation(summary = "Find results by match")
	List<MatchResult> findByMatch(@Param("match") Match match);

	@Operation(summary = "Find results by team")
	List<MatchResult> findByTeam(@Param("team") Team team);

	@RestResource(exported = false)
	@Query(value = """
        SELECT 
            mr.team.id as teamId,
            mr.team.name as teamName,
            SUM(mr.score) as totalScore,
            COUNT(mr) as matchesPlayed
        FROM MatchResult mr
        WHERE mr.match.round.edition.id = :editionId
        GROUP BY mr.team.id, mr.team.name
        ORDER BY SUM(mr.score) DESC, COUNT(mr) DESC, mr.team.name ASC
    """,
		countQuery = """
        SELECT COUNT(DISTINCT mr.team.id)
        FROM MatchResult mr
        WHERE mr.match.round.edition.id = :editionId
    """)
	Page<LeaderboardRowProjection> findLeaderboardByEditionId(
		@Param("editionId") Long editionId,
		Pageable pageable
	);

	boolean existsByMatch(Match match);

	boolean existsByMatch_Id(Long matchId);
}