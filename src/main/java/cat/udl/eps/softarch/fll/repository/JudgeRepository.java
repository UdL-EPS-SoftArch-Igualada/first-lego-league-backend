package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import cat.udl.eps.softarch.fll.domain.Judge;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Judges", description = "Repository for managing Judge entities")
@RepositoryRestResource
public interface JudgeRepository extends CrudRepository<Judge, Long>, PagingAndSortingRepository<Judge, Long> {

	List<Judge> findAll();

	@RestResource(exported = false)
	List<Judge> findByEditionId(Long editionId);

	@Operation(summary = "Search judges by name",
			description = "Returns a list of Judges whose names contain the specified text (case-insensitive).")
	@Query("select j from Judge j where lower(j.name) like lower(concat('%', :name, '%'))")
	List<Judge> findByNameContaining(@Param("name") String name);

}
