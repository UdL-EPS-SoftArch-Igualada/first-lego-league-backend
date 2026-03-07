package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import cat.udl.eps.softarch.fll.domain.Ranking;

@RepositoryRestResource(path = "rankings")
public interface RankingRepository extends JpaRepository<Ranking, Long> {

	@Override
	@RestResource(exported = false)
	<S extends Ranking> S save(S entity);

	@Override
	@RestResource(exported = false)
	void delete(Ranking entity);

	@Override
	@RestResource(exported = false)
	void deleteById(Long id);

	List<Ranking> findAllByOrderByPositionAsc();
}
