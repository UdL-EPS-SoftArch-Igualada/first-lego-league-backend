package cat.udl.eps.softarch.demo.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.demo.domain.ScientificProject;


@RepositoryRestResource
public interface ScientificProjectRepository extends CrudRepository<ScientificProject, Long>,
	PagingAndSortingRepository<ScientificProject, Long> {

	List<ScientificProject> findByScoreGreaterThanEqual(@Param("minScore") Integer minScore);
}

