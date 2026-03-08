package cat.udl.eps.softarch.fll.dto;


import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchSearchPageResponse {

	@NotNull
	private Integer page;

	@NotNull
	private Integer size;

	@NotNull
	private Long totalElements;

	@NotNull
	private List<MatchSearchItemResponse> items;

}