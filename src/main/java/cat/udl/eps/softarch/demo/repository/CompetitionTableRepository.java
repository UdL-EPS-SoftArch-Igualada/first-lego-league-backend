package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.CompetitionTable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource
public interface CompetitionTableRepository extends CrudRepository<CompetitionTable, String>, PagingAndSortingRepository<CompetitionTable, String> {

}
