package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import cat.udl.eps.softarch.fll.domain.Referee;

@Repository
@RepositoryRestResource
public interface RefereeRepository extends CrudRepository<Referee, Long>, PagingAndSortingRepository<Referee, Long> {

	@RestResource(exported = false)
	List<Referee> findByEditionId(Long editionId);

}
