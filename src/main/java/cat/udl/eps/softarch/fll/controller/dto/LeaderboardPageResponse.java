package cat.udl.eps.softarch.fll.controller.dto;

import java.util.List;

public record LeaderboardPageResponse(
		Long editionId,
		int page,
		int size,
		long totalElements,
		List<LeaderboardItemResponse> items
) {}
