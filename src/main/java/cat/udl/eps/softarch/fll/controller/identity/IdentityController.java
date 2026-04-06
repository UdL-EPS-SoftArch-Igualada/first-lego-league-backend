package cat.udl.eps.softarch.fll.controller.identity;

import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import cat.udl.eps.softarch.fll.domain.User;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Identity", description = "Controller for managing authenticated user identity")
@BasePathAwareController
public class IdentityController {

	@RequestMapping("/identity")
	public @ResponseBody PersistentEntityResource getAuthenticatedUserIdentity(PersistentEntityResourceAssembler resourceAssembler) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		User user = (User) authentication.getPrincipal();
		if (user == null) {
			return null;
		}
		return resourceAssembler.toFullResource(user);
	}
}
