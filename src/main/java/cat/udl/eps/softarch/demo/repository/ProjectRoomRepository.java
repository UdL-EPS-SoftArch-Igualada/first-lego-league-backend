package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.ProjectRoom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

@RepositoryRestResource
public interface ProjectRoomRepository extends CrudRepository<ProjectRoom, String>, PagingAndSortingRepository<ProjectRoom, String> {

	List<ProjectRoom> findAll();

}
