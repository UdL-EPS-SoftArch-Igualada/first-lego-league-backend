package cat.udl.eps.softarch.fll.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.domain.Referee;
import jakarta.persistence.LockModeType;

@Repository
@RepositoryRestResource
public interface MatchRepository extends
	CrudRepository<Match, Long>,
	PagingAndSortingRepository<Match, Long>,
	JpaSpecificationExecutor<Match> {

@Query("""
			SELECT m FROM Match m
			WHERE m.referee = :referee
			AND m.startTime < :newMatchEndTime
			AND m.endTime > :newMatchStartTime
			AND m.id <> :currentMatchId
			""")
	List<Match> findOverlappingAssignments(
			@Param("referee") Referee referee,
			@Param("newMatchStartTime") LocalTime newMatchStartTime,
			@Param("newMatchEndTime") LocalTime newMatchEndTime,
			@Param("currentMatchId") Long currentMatchId);

	@Query("""
			SELECT COUNT(m) > 0 FROM Match m
			WHERE m.competitionTable = :table
			AND m.startTime < :newMatchEndTime
			AND m.endTime > :newMatchStartTime
			AND (:currentMatchId IS NULL OR m.id <> :currentMatchId)
			""")
	@RestResource(exported = false)
	boolean existsOverlappingAssignmentsForTable(
			@Param("table") CompetitionTable table,
			@Param("newMatchStartTime") LocalTime newMatchStartTime,
			@Param("newMatchEndTime") LocalTime newMatchEndTime,
			@Param("currentMatchId") Long currentMatchId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM Match m WHERE m.id = :id")
	@RestResource(exported = false)
	Optional<Match> findByIdForUpdate(@Param("id") Long id);

	@Query("""
			SELECT m FROM Match m
			WHERE m.competitionTable.id = :tableId
			AND m.startTime < :newEndTime
			AND m.endTime > :newStartTime
			AND (:currentMatchId IS NULL OR m.id <> :currentMatchId)
			""")
	List<Match> findOverlappingMatchesByTable(
			@Param("tableId") String tableId,
			@Param("newStartTime") LocalTime newStartTime,
			@Param("newEndTime") LocalTime newEndTime,
			@Param("currentMatchId") Long currentMatchId);
}
