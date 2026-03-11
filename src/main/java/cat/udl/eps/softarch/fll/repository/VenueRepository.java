package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.fll.domain.Venue;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource
public interface VenueRepository extends CrudRepository<Venue, Long>, PagingAndSortingRepository<Venue, Long> {

	Optional<Venue> findByName(String name);

	List<Venue> findByCity(@Param("city") String city);

	@RestResource(path = "findByNameContaining", rel = "findByNameContaining")
	List<Venue> findByNameContainingIgnoreCase(@Param("name") String name);
}
