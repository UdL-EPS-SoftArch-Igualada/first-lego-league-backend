package cat.udl.eps.softarch.fll.repository.project;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.fll.domain.ScientificProject;


@RepositoryRestResource
public interface ScientificProjectRepository extends JpaRepository<ScientificProject, Long> {

	List<ScientificProject> findByScoreGreaterThanEqual(@Param("minScore") Integer minScore);

	List<ScientificProject> findByTeamId(Long teamId);
	List<ScientificProject> findByEditionId(@Param("editionId") Long editionId);
}

