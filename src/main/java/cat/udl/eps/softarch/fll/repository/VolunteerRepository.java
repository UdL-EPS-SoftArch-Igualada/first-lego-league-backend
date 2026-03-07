package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import cat.udl.eps.softarch.fll.domain.Volunteer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.LockModeType;

@Tag(name = "Volunteers", description = "Repository for managing Volunteer entities")
@RepositoryRestResource
public interface VolunteerRepository extends CrudRepository<Volunteer, Long>, PagingAndSortingRepository<Volunteer, Long> {

	@Override
	@RestResource(exported = false)
	<S extends Volunteer> S save(S entity);

	@Override
	@RestResource(exported = false)
	void delete(Volunteer entity);

	@Override
	@RestResource(exported = false)
	void deleteById(Long id);

	@Operation(summary = "Find volunteers by name",
			description = "Returns a list of Volunteers whose name matches the given value.")
	List<Volunteer> findByName(@Param("name") String name);

	@Operation(summary = "Find volunteer by email",
			description = "Returns the Volunteer whose email address matches the given value.")
	Optional<Volunteer> findByEmailAddress(@Param("email") String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT v FROM Volunteer v WHERE v.id = :id")
	@RestResource(exported = false)
	Optional<Volunteer> findByIdForUpdate(@Param("id") Long id);
}

