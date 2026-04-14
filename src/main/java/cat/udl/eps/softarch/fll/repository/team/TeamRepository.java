package cat.udl.eps.softarch.fll.repository.team;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import cat.udl.eps.softarch.fll.domain.Team;

@RepositoryRestResource(path = "teams")
public interface TeamRepository extends CrudRepository<Team, Long>, PagingAndSortingRepository<Team, Long> {

	List<Team> findByCity(@Param("city") String city);

	List<Team> findByFoundationYearGreaterThan(@Param("year") int year);

	List<Team> findByEducationalCenter(@Param("educationalCenter") String educationalCenter);

	List<Team> findByCategory(@Param("category") String category);

	List<Team> findByMembersRole(@Param("role") String role);

	boolean findbyName(@Param("name") String Name);
	Optional<Team> findById(Long id);




	@RestResource(exported = false)
	boolean existsByIdAndRegisteredEditionsId(Long teamId, Long editionId);

	@RestResource(exported = false)
	@Query("select distinct t from Team t left join fetch t.registeredEditions where t.id = :id")
	Optional<Team> findByIdWithRegisteredEditions(@Param("id") Long id);
}