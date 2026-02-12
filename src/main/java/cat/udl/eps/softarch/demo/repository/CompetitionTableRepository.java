package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.CompetitionTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitionTableRepository extends JpaRepository<CompetitionTable, String> {

}
