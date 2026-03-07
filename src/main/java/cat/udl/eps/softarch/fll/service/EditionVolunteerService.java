package cat.udl.eps.softarch.fll.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.controller.dto.EditionVolunteersResponse;
import cat.udl.eps.softarch.fll.controller.dto.VolunteerSummaryResponse;
import cat.udl.eps.softarch.fll.domain.Volunteer;
import cat.udl.eps.softarch.fll.repository.EditionRepository;
import cat.udl.eps.softarch.fll.repository.FloaterRepository;
import cat.udl.eps.softarch.fll.repository.JudgeRepository;
import cat.udl.eps.softarch.fll.repository.RefereeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EditionVolunteerService {

	private final EditionRepository editionRepository;
	private final RefereeRepository refereeRepository;
	private final JudgeRepository judgeRepository;
	private final FloaterRepository floaterRepository;

	@Transactional(readOnly = true)
	public EditionVolunteersResponse getVolunteersGroupedByType(Long editionId) {
		if (!editionRepository.existsById(editionId)) {
			throw new NoSuchElementException("EDITION_NOT_FOUND");
		}

		var referees = refereeRepository.findByEditionId(editionId).stream()
				.map(this::toSummary)
				.toList();
		var judges = judgeRepository.findByEditionId(editionId).stream()
				.map(this::toSummary)
				.toList();
		var floaters = floaterRepository.findByEditionId(editionId).stream()
				.map(this::toSummary)
				.toList();

		return new EditionVolunteersResponse(referees, judges, floaters);
	}

	private VolunteerSummaryResponse toSummary(Volunteer volunteer) {
		return new VolunteerSummaryResponse(
				volunteer.getId(),
				volunteer.getName(),
				volunteer.getEmailAddress(),
				volunteer.getPhoneNumber());
	}
}
