package cat.udl.eps.softarch.fll.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.controller.edition.dto.EditionCompetitionTableResponse;
import cat.udl.eps.softarch.fll.controller.edition.dto.EditionTableMatchResponse;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.exception.EditionCompetitionTableNotFoundException;
import cat.udl.eps.softarch.fll.repository.edition.EditionRepository;
import cat.udl.eps.softarch.fll.repository.match.MatchRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EditionCompetitionTableService {

	private final EditionRepository editionRepository;
	private final MatchRepository matchRepository;

	@Transactional(readOnly = true)
	public List<EditionCompetitionTableResponse> getTablesByEdition(Long editionId) {
		if (!editionRepository.existsById(editionId)) {
			throw new EditionCompetitionTableNotFoundException();
		}

		Map<String, List<EditionTableMatchResponse>> tables = new LinkedHashMap<>();
		for (Match match : matchRepository.findScheduledTableMatchesByEditionId(editionId)) {
			String tableId = match.getCompetitionTable().getId();
			tables.computeIfAbsent(tableId, ignored -> new ArrayList<>())
				.add(new EditionTableMatchResponse(
					match.getId(),
					match.getStartTime() == null ? null : match.getStartTime().toString(),
					match.getEndTime() == null ? null : match.getEndTime().toString()));
		}

		return tables.entrySet().stream()
			.map(entry -> new EditionCompetitionTableResponse(entry.getKey(), entry.getValue()))
			.toList();
	}
}
