package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Referee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeRepository extends JpaRepository<Referee, Long> {

}
