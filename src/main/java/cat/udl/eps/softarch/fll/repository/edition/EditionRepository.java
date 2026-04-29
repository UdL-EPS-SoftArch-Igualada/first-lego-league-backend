package cat.udl.eps.softarch.fll.repository.edition;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import cat.udl.eps.softarch.fll.domain.edition.Edition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

@Tag(name = "Editions", description = "Repository for managing Edition entities")
@RepositoryRestResource
public interface EditionRepository extends JpaRepository<Edition, Long> {

	@RestResource(exported = false)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from Edition e where e.id = :id")
	Optional<Edition> findByIdForUpdate(@Param("id") Long id);

	@Operation(summary = "Search editions by year",
			description = "Returns a list of Editions for the specified year.")
	List<Edition> findByYear(@Param("year") Integer year);

	@Operation(summary = "Search editions by venue name",
			description = "Returns a list of Editions for the specified venue.")
	List<Edition> findByVenueName(@Param("venueName") String venueName);

	@Operation(summary = "Search editions by partial venue name",
		description = "Returns a list of Editions whose venue name contains the given string (case-insensitive).")
	List<Edition> findByVenueNameContainingIgnoreCase(@Param("venueName") String venueName);

}
