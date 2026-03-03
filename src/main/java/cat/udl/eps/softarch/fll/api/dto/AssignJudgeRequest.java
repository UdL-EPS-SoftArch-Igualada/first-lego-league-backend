package cat.udl.eps.softarch.fll.api.dto;

public record AssignJudgeRequest(
	String roomId,
	String judgeId,
	boolean isManager
) {}