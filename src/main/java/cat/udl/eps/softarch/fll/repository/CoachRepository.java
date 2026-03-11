package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.fll.domain.Coach;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Coaches", description = "Repository for managing Coach entities")
@RepositoryRestResource
public interface CoachRepository extends CrudRepository<Coach, Integer>, PagingAndSortingRepository<Coach, Integer> {

	@Operation(summary = "Search coaches by name",
			description = "Returns a list of Coaches whose names contain the specified text.")
	List<Coach> findByNameContaining(@Param("text") String text);

	Optional<Coach> findByEmailAddress(@Param("email") String email);
}
