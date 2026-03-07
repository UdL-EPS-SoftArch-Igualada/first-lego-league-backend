package cat.udl.eps.softarch.fll.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import cat.udl.eps.softarch.fll.domain.CompetitionTable;
import jakarta.persistence.LockModeType;

@Repository
@RepositoryRestResource
public interface CompetitionTableRepository extends CrudRepository<CompetitionTable, String>, PagingAndSortingRepository<CompetitionTable, String> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM CompetitionTable t WHERE t.id = :id")
	@RestResource(exported = false)
	Optional<CompetitionTable> findByIdForUpdate(@Param("id") String id);
}
