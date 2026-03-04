package cat.udl.eps.softarch.fll.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import cat.udl.eps.softarch.fll.domain.Referee;

@Repository
@RepositoryRestResource
public interface RefereeRepository extends CrudRepository<Referee, Long>, PagingAndSortingRepository<Referee, Long> {

	Optional<Referee> findByEmailAddress(@Param("email") String email);
}
