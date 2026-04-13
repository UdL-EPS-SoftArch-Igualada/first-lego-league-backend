package cat.udl.eps.softarch.fll.repository.team;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import cat.udl.eps.softarch.fll.domain.Team;

@RepositoryRestResource(path = "teams")
public interface TeamRepository extends JpaRepository<Team, Long> {

	boolean existsByIdAndRegisteredEditionsId(Long teamId, Long editionId);

	@Query("""
        SELECT t FROM Team t
        LEFT JOIN FETCH t.registeredEditions
        WHERE t.id = :id
    """)
	Optional<Team> findByIdWithRegisteredEditions(Long id);

	List<Team> findByCity(@Param("city") String city);

	List<Team> findByFoundationYearGreaterThan(@Param("year") int year);

	List<Team> findByEducationalCenter(@Param("educationalCenter") String educationalCenter);

	List<Team> findByCategory(@Param("category") String category);

	List<Team> findByMembersRole(@Param("role") String role);

	Optional<Team> findByName(@Param("name") String name);

	@RestResource(exported = false)
	@Query("""
        SELECT DISTINCT t 
        FROM Team t 
        LEFT JOIN FETCH t.registeredEditions 
        WHERE t.name = :name
    """)
	Optional<Team> findByNameWithRegisteredEditions(@Param("name") String name);
}