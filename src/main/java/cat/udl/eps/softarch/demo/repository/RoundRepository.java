package cat.udl.eps.softarch.demo.repository;

import cat.udl.eps.softarch.demo.domain.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
	Round findByNumber(int number);
}
