package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import cat.udl.eps.softarch.fll.domain.Floater;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Floaters", description = "Repository for managing Floater entities")
@RepositoryRestResource
public interface FloaterRepository extends CrudRepository<Floater, Long>, PagingAndSortingRepository<Floater, Long> {

	@Operation(summary = "Find floater by student code",
			description = "Returns the Floater with the specified student code.")
	Optional<Floater> findByStudentCode(@Param("studentCode") String studentCode);

	@Operation(summary = "Search floaters by name",
			description = "Returns a list of Floaters whose names contain the specified text.")
	List<Floater> findByNameContainingIgnoreCase(@Param("text") String text);

	@RestResource(exported = false)
	List<Floater> findByEditionId(Long editionId);
}
