package cat.udl.eps.softarch.demo.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.demo.domain.Floater;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Floaters", description = "Repository for managing Floater entities")
@RepositoryRestResource
public interface FloaterRepository extends CrudRepository<Floater, Long>, PagingAndSortingRepository<Floater, Long> {

	@Operation(summary = "Find floaters by student code",
			description = "Returns a list of Floaters with the specified student code.")
	List<Floater> findByStudentCode(@Param("studentCode") String studentCode);

	@Operation(summary = "Search floaters by name",
			description = "Returns a list of Floaters whose names contain the specified text.")
	List<Floater> findByNameContainingIgnoreCase(@Param("text") String text);
}

