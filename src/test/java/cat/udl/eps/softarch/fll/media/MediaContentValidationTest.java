package cat.udl.eps.softarch.fll.media;

import cat.udl.eps.softarch.fll.domain.DomainValidationException;
import cat.udl.eps.softarch.fll.domain.MediaContent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaContentValidationTest {

	@Test
	void validConstruction() {
		assertDoesNotThrow(() -> MediaContent.create("https://example.com/video.mp4", "video"));
	}

	@Nested
	class InvalidUrl {

		@Test
		void nullUrlThrows() {
			assertThrows(DomainValidationException.class,
				() -> MediaContent.create(null, "video"));
		}

		@Test
		void blankUrlThrows() {
			assertThrows(DomainValidationException.class,
				() -> MediaContent.create("", "video"));
		}
	}

	@Nested
	class InvalidType {

		@Test
		void nullTypeThrows() {
			assertThrows(DomainValidationException.class,
				() -> MediaContent.create("https://example.com/video.mp4", null));
		}

		@Test
		void blankTypeThrows() {
			assertThrows(DomainValidationException.class,
				() -> MediaContent.create("https://example.com/video.mp4", ""));
		}
	}
}
