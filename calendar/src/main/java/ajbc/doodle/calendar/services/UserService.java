package ajbc.doodle.calendar.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.webpush.Subscription;

@Component()
public class UserService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private EventDao eventDao;

	@Autowired
	private NotificationDao notificationDao;

	public void addUser(User user) throws DaoException {
		user.setJoinDate(LocalDate.now());
		userDao.addUser(user);
	}

	public void updateUser(User user) throws DaoException {
		userDao.updateUser(user);
	}

	public User getUserById(Integer userId) throws DaoException {
		User user = userDao.getUserById(userId);
		user.setEvents(filterdEventsToUsersNotificationsOnly(user));
		return user;
	}

	public List<User> getAllUsers() throws DaoException {
		// TODO not displaying notifications
		List<User> users = userDao.getAllUsers();
		for (int i = 0; i < users.size(); i++) {
			users.get(i).setEvents(filterdEventsToUsersNotificationsOnly(users.get(i)));
		}
		return users;
	}

	public List<User> getAllUsersInEvent(Integer eventId) throws DaoException {
		Event event = eventDao.getEventById(eventId);
		return event.getGuests().stream().collect(Collectors.toList());
	}

	public List<User> getAllUsersWithEventBetween(LocalDateTime start, LocalDateTime end) throws DaoException {
		List<Event> events = eventDao.getEventsBetween(start, end);
		Set<User> users = new HashSet<User>();
		for (var e : events)
			for (var g : e.getGuests())
				users.add(g);
		return users.stream().collect(Collectors.toList());
	}

	public User getUserByEmail(String email) throws DaoException {
		User user = userDao.getUserByEmail(email);
		user.setEvents(filterdEventsToUsersNotificationsOnly(user));
		return user;
	}

	public void hardDeleteUser(User user) throws DaoException {
		Set<Event> usersEvents = user.getEvents();
		for (var event : usersEvents) {
			if (event.getOwnerId() == user.getUserId())
				event.setOwner(null);
			event.setGuests(event.getGuests().stream().filter(u -> u.getUserId() != user.getUserId())
					.collect(Collectors.toSet()));
			eventDao.updateEvent(event);
		}
		List<Notification> usersNotifications = notificationDao.getAllNotificationsByUserId(user.getUserId());
		notificationDao.deleteAll(usersNotifications);
		userDao.hardDeleteUser(user);
	}

	public void softDeleteUser(User user) throws DaoException {
		userDao.softDeleteUser(user);
	}

	public void logInUser(Subscription subscription, User user) throws DaoException {
		user.setEndPoint(subscription.getEndpoint());
		user.setP256dh(subscription.getKeys().getP256dh());
		user.setAuth(subscription.getKeys().getAuth());
		user.setLoggedIn(true);
		userDao.updateUser(user);
	}

	public void logOutUser(User user) throws DaoException {
		user.setEndPoint(null);
		user.setLoggedIn(false);
		userDao.updateUser(user);
	}

	private Set<Event> filterdEventsToUsersNotificationsOnly(User user) {
		Set<Event> events = user.getEvents();
		Set<Event> eventsFilterd = new HashSet<Event>();
		Set<Notification> notifications;
		for (Event event : events) {
			notifications = event.getNotifications().stream().filter(n -> n.getUserId() == user.getUserId())
					.collect(Collectors.toSet());
			event.setNotifications(notifications);
			eventsFilterd.add(event);
		}
		return eventsFilterd;
	}

	public List<Notification> getAllnotificationsOfUser(Integer userId) throws DaoException {
		return notificationDao.getAllNotificationsByUserId(userId);
	}

}
