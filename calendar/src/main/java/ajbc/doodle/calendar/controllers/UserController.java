package ajbc.doodle.calendar.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ajbc.doodle.calendar.daos.DaoException;

import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.webpush.Subscription;
import ajbc.doodle.calendar.entities.webpush.SubscriptionEndpoint;
import ajbc.doodle.calendar.notifications_manager.NotificationManager;
import ajbc.doodle.calendar.services.UserService;

/**
 * Handle User's API requests
 * 
 * @author ketty
 *
 */
@RequestMapping("/users")
@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private NotificationManager notificationManager;

	/**
	 * Returns users from database by certain parameters
	 * 
	 * @param params map of parameters , key - parameter name, value - parameter
	 *               value
	 * @return list of users. eventId : users of event by eventId. startDateTime and
	 *         endDateTime : users with event between given dates. no parameters :
	 *         all users in database.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllUsers(@RequestParam Map<String, String> params) {
		try {
			List<User> users;
			if (params.containsKey("eventId"))
				users = userService.getAllUsersInEvent(Integer.parseInt(params.get("eventId")));
			else if (params.containsKey("startDateTime") && params.containsKey("endDateTime"))
				users = userService.getAllUsersWithEventBetween(LocalDateTime.parse(params.get("startDateTime")),
						LocalDateTime.parse(params.get("endDateTime")));
			else
				users = userService.getAllUsers();
			return ResponseEntity.status(HttpStatus.OK).body(users);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	/**
	 * Creates a user and inserts it to database
	 * 
	 * @param user - user to create
	 * @return created user
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createUser(@RequestBody User user) {
		try {
			userService.addUser(user);
			user = userService.getUserById(user.getUserId());
			return ResponseEntity.status(HttpStatus.CREATED).body(user);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	/**
	 * Create users from list and insert to database
	 * 
	 * @param users - list of users to create
	 * @return - list of created users
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/list")
	public ResponseEntity<?> createUserFromList(@RequestBody List<User> users) {
		try {
			List<User> createdUsers = new ArrayList<User>();
			for (var user : users) {
				userService.addUser(user);
				createdUsers.add(userService.getUserById(user.getUserId()));
			}
			return ResponseEntity.status(HttpStatus.CREATED).body(createdUsers);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	/**
	 * Updates user in database
	 * 
	 * @param user - user to update
	 * @param id   - user`s id
	 * @return updated user if action succeeded, otherwise returns exception details
	 */
	@RequestMapping(method = RequestMethod.PUT, path = "/{id}")
	public ResponseEntity<?> updateUser(@RequestBody User user, @PathVariable Integer id) {
		try {
			user.setUserId(id);
			userService.updateUser(user);
			user = userService.getUserById(id);
			return ResponseEntity.status(HttpStatus.OK).body(user);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	/**
	 * Update list of users in database
	 * 
	 * @param users - map of users. key - user's id, value - user
	 * @return list of updated users if action succeeded, otherwise returns
	 *         unsuccessful updates and exception details
	 */
	@RequestMapping(method = RequestMethod.PUT, path = "/list")
	public ResponseEntity<?> updateUsersFromList(@RequestBody Map<Integer, User> users) {
		List<String> unudatedUsers = new ArrayList<String>();
		List<Integer> ids = users.keySet().stream().collect(Collectors.toList());
		for (var id : ids) {
			try {
				users.get(id).setUserId(id);
				userService.updateUser(users.get(id));
			} catch (DaoException e) {
				unudatedUsers.add("user with id " + id + "wasnt updated: " + e.getMessage());
			}
		}
		if (!unudatedUsers.isEmpty())
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(unudatedUsers);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	/**
	 * Get user from database by user's id
	 * 
	 * @param id - user's id
	 * @return - user if action succeeded, otherwise returns exception details
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/id/{id}")
	public ResponseEntity<?> getUserById(@PathVariable Integer id) {
		try {
			User user = userService.getUserById(id);
			return ResponseEntity.ok(user);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}

	}

	/**
	 * Get user from database by user's email
	 * 
	 * @param email - user's email
	 * @return - user if action succeeded, otherwise returns exception details
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/email/{email}")
	public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
		try {
			User user = userService.getUserByEmail(email);
			return ResponseEntity.ok(user);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	/**
	 * Delete user from database by user's id
	 * 
	 * @param id         - user's id
	 * @param deleteType - SOFT: deactivate user. HARD: hard delete from database
	 * @return delete user if action succeeded, otherwise returns exception details
	 */
	@RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Integer id, @RequestParam String deleteType) {
		try {
			User user = userService.getUserById(id);

			// delete all user`s notifications in NotificationManager
			List<Notification> notifications = userService.getAllnotificationsOfUser(id);
			notificationManager.deleteNotifications(notifications);

			if (deleteType.equalsIgnoreCase("HARD"))
				userService.hardDeleteUser(user);
			else
				userService.softDeleteUser(user);
			return ResponseEntity.ok(user);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	/**
	 * Log in user
	 * 
	 * @param subscription - user's subscription
	 * @param email        - user's email
	 * @return response massage if action succeeded, otherwise returns exception
	 *         details
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/login/{email}")
	public ResponseEntity<?> logIn(@RequestBody Subscription subscription,
			@PathVariable(required = false) String email) {
		try {
			User user = userService.getUserByEmail(email);
			userService.logInUser(subscription, user);
			return ResponseEntity.ok().body("User logged in");
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	/**
	 * Log out user
	 * 
	 * @param email - user's email
	 * @return response massage if action succeeded, otherwise returns exception
	 *         details
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/logout/{email}")
	public ResponseEntity<?> logOut(@PathVariable(required = false) String email) {
		try {
			User user = userService.getUserByEmail(email);
			userService.logOutUser(user);
			return ResponseEntity.ok().body("User logged out");
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	/**
	 * Check if user subscribed
	 * 
	 * @param subscription - user's subscription
	 * @return - true if user subscribed, otherwise returns false;
	 * @throws DaoException
	 */
	@PostMapping("/isSubscribed")
	public boolean isSubscribed(@RequestBody SubscriptionEndpoint subscription) throws DaoException {
		List<User> users = userService.getAllUsers();
		for (User u : users)
			if (u.getEndPoint() != null && u.getEndPoint().equals(subscription.getEndpoint()))
				return true;
		return false;
	}

}
