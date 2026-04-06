package cat.udl.eps.softarch.fll.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cat.udl.eps.softarch.fll.controller.edition.dto.EditionVolunteersResponse;
import cat.udl.eps.softarch.fll.controller.volunteer.dto.VolunteerSummaryResponse;
import cat.udl.eps.softarch.fll.domain.volunteer.Volunteer;
import cat.udl.eps.softarch.fll.exception.EditionVolunteerException;
import cat.udl.eps.softarch.fll.repository.edition.EditionRepository;
import cat.udl.eps.softarch.fll.repository.volunteer.FloaterRepository;
import cat.udl.eps.softarch.fll.repository.volunteer.JudgeRepository;
import cat.udl.eps.softarch.fll.repository.volunteer.RefereeRepository;
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
			throw new EditionVolunteerException("EDITION_NOT_FOUND", "Edition with id " + editionId + " not found");
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
