package cat.udl.eps.softarch.fll.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import cat.udl.eps.softarch.fll.domain.Score;

@RepositoryRestResource(exported = false)
public interface ScoreRepository extends JpaRepository<Score, Long> {

	List<Score> findByRound_Id(Long roundId);

	boolean existsByRound_IdAndTeam_Name(Long roundId, String teamName);

}
