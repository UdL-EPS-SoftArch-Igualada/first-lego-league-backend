package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import cat.udl.eps.softarch.fll.domain.Referee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Referees", description = "Repository for managing Referee entities")
@Repository
@RepositoryRestResource
public interface RefereeRepository extends CrudRepository<Referee, Long>, PagingAndSortingRepository<Referee, Long> {

	@RestResource(exported = false)
	List<Referee> findByEditionId(Long editionId);

	@Operation(summary = "Find referee by email",
			description = "Returns the Referee whose email address matches the given value.")
	Optional<Referee> findByEmailAddress(@Param("email") String email);

	@Operation(summary = "Search referees by name",
			description = "Returns a list of Referees whose names contain the specified text (case-insensitive).")
	@Query("select r from Referee r where lower(r.name) like lower(concat('%', :name, '%'))")
	List<Referee> findByNameContaining(@Param("name") String name);

}
