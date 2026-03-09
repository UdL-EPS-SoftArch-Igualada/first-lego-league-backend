package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.fll.domain.Award;
import cat.udl.eps.softarch.fll.domain.Edition;
import cat.udl.eps.softarch.fll.domain.Team;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Awards", description = "Repository for managing awards and prizes")
@RepositoryRestResource
public interface AwardRepository extends JpaRepository<Award, Long> {

	@Operation(summary = "Find awards by edition",
		description = "Returns all awards presented in a specific edition.")
	List<Award> findByEdition(@Param("edition") Edition edition);

	@Operation(summary = "Find awards by winner",
		description = "Returns all awards won by a specific team.")
	List<Award> findByWinner(@Param("winner") Team winner);

	@Operation(summary = "Find awards by partial winner name",
		description = "Returns all awards where the winning team's name contains the given string (case-insensitive).")
	List<Award> findByWinnerNameContainingIgnoreCase(@Param("name") String name);
}