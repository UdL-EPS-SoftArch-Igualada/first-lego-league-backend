package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.fll.domain.TeamMember;

@RepositoryRestResource(path = "teamMembers")
public interface TeamMemberRepository extends CrudRepository<TeamMember, Long>, PagingAndSortingRepository<TeamMember, Long> {
	List<TeamMember> findByName(@Param("name") String name);
	List<TeamMember> findByRole(@Param("role") String role);
}

