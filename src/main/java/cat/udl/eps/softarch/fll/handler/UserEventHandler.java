package cat.udl.eps.softarch.fll.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterLinkSave;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeLinkSave;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import cat.udl.eps.softarch.fll.domain.identity.User;
import cat.udl.eps.softarch.fll.repository.identity.UserRepository;

@Component
@RepositoryEventHandler
public class UserEventHandler {

	final Logger logger = LoggerFactory.getLogger(UserEventHandler.class);

	final UserRepository userRepository;

	public UserEventHandler(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@HandleBeforeCreate
	public void handleUserPreCreate(User user) {
		logger.info("Before creating: {}", user);
	}

	@HandleBeforeSave
	public void handleUserPreSave(User user) {
		logger.info("Before updating: {}", user);
		if (user.isPasswordPlainText()) {
			user.setPasswordReset(true);
		}
	}

	@HandleBeforeDelete
	public void handleUserPreDelete(User user) {
		logger.info("Before deleting: {}", user);
	}

	@HandleBeforeLinkSave
	public void handleUserPreLinkSave(User user, Object o) {
		logger.info("Before linking: {} to {}", user, o);
	}

	@HandleAfterCreate
	public void handleUserPostCreate(User user) {
		logger.info("After creating: {}", user);
		user.encodePassword();
		userRepository.save(user);
	}

	@HandleAfterSave
	public void handleUserPostSave(User user) {
		logger.info("After updating: {}", user);
		if (user.isPasswordReset()) {
			user.encodePassword();
		}
		userRepository.save(user);
	}

	@HandleAfterDelete
	public void handleUserPostDelete(User user) {
		logger.info("After deleting: {}", user);
	}

	@HandleAfterLinkSave
	public void handleUserPostLinkSave(User user, Object o) {
		logger.info("After linking: {} to {}", user, o);
	}
}
