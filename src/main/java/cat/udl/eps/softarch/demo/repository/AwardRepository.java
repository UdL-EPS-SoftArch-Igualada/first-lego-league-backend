package cat.udl.eps.softarch.demo.repository;

import java.util.List;
import cat.udl.eps.softarch.demo.domain.Award;
import cat.udl.eps.softarch.demo.domain.AwardId;
import cat.udl.eps.softarch.demo.domain.Edition;
import cat.udl.eps.softarch.demo.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Repository for managing (Award) entities.
 * Uses (AwardId) as the identifier type due to the composite primary key.
 */
@Tag(name = "Awards", description = "Repository for managing awards and prizes")
@RepositoryRestResource
public interface AwardRepository extends JpaRepository<Award, AwardId> {

    @Operation(summary = "Find awards by edition", 
               description = "Returns all awards presented in a specific edition.")
    List<Award> findByEdition(@Param("edition") Edition edition);

    @Operation(summary = "Find awards by winner", 
               description = "Returns all awards won by a specific team.")
    List<Award> findByWinner(@Param("winner") Team winner);
}