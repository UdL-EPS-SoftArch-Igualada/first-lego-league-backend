package cat.udl.eps.softarch.fll.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class MediaContent extends UriEntity<String> {

	@Id
	@NotBlank
	private String url;

	@NotBlank
	@Column(name = "media_type")
	private String type;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	private Edition edition;

	protected MediaContent() {
	}

	public static MediaContent create(String url, String type) {
		DomainValidation.requireNonBlank(url, "url");
		DomainValidation.requireNonBlank(type, "type");

		MediaContent content = new MediaContent();
		content.url = url;
		content.type = type;
		return content;
	}

	@Override
	public @Nullable String getId() {
		return url;
	}
}
