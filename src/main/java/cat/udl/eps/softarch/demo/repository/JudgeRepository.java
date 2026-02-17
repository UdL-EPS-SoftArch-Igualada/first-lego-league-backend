package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Judge;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

@RepositoryRestResource
public interface JudgeRepository extends CrudRepository<Judge, Long>, PagingAndSortingRepository<Judge, Long> {

	List<Judge> findAll();

}
