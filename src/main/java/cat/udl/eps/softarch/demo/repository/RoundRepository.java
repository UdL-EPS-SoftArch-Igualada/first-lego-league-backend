package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Round;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RepositoryRestResource
public interface RoundRepository extends CrudRepository<Round, Long>, PagingAndSortingRepository<Round, Long> {
	Optional<Round> findByNumber(int number);
}
