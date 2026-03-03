package cat.udl.eps.softarch.fll.api.dto;

public record AssignJudgeResponse(
	String roomId,
	String judgeId,
	String role,
	String status
) {}