package cat.udl.eps.softarch.fll.repository;

import java.time.LocalTime;
import org.springframework.data.jpa.domain.Specification;
import cat.udl.eps.softarch.fll.domain.Match;

public class MatchSpecifications {

	private MatchSpecifications() {}

	public static Specification<Match> timeOverlap(LocalTime startFrom, LocalTime endTo) {
		return (root, query, cb) -> {

			if (startFrom != null && endTo != null) {
				return cb.and(
					cb.lessThan(root.get("startTime"), endTo),
					cb.greaterThan(root.get("endTime"), startFrom)
				);
			}

			if (startFrom != null) {
				return cb.greaterThan(root.get("endTime"), startFrom);
			}

			if (endTo != null) {
				return cb.lessThan(root.get("startTime"), endTo);
			}

			return cb.conjunction();
		};
	}

	public static Specification<Match> hasTable(String tableId) {
		return (root, query, cb) ->
			tableId == null ? null :
				cb.equal(root.get("competitionTable").get("id"), tableId);
	}

	public static Specification<Match> hasRound(Long roundId) {
		return (root, query, cb) ->
			roundId == null ? null :
				cb.equal(root.get("round").get("id"), roundId);
	}
}