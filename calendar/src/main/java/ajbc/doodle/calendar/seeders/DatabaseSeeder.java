package ajbc.doodle.calendar.seeders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.enums.RepeatOptions;
import ajbc.doodle.calendar.enums.Units;

@Component
public class DatabaseSeeder {

	private final RepeatOptions NONE_REP = RepeatOptions.NONE;
	private final Units HOURS = Units.HOURS;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EventDao eventDao;

	@Autowired
	private NotificationDao notificationDao;

	@EventListener
	public void seed(ContextRefreshedEvent event) throws DaoException {
		seedUsersTable();
		seedEventTable();
		seedNotificationTable();
	}

	private void seedUsersTable() throws DaoException {
		List<User> users = userDao.getAllUsers();
		if (users == null || users.size() == 0) {
			userDao.addUser(new User("ketty", "pekarsky", "ketty@gmail.com", LocalDate.of(1996, 05, 02),
					LocalDate.now()));
			userDao.addUser(
					new User("dani", "kravtsov", "dani@gmail.com", LocalDate.of(1994, 04, 06), LocalDate.now()));
			userDao.addUser(new User("nikol", "pekarsky", "nikol@gmail.com", LocalDate.of(2004, 04, 15),
					LocalDate.now()));
		}
	}

	private void seedEventTable() throws DaoException {
		User owner1 = userDao.getUserById(1);
		User owner2 = userDao.getUserById(2);
		Set<User> guests1 = new HashSet<User>();
		guests1.add(owner1);
		guests1.add(userDao.getUserById(3));
		Set<User> guests2 = new HashSet<User>();
		guests2.add(owner2);
		guests2.add(userDao.getUserById(1));
		guests2.add(userDao.getUserById(3));

		List<Event> events = eventDao.getAllEvents();
		if (events == null || events.size() == 0) {
			eventDao.addEvent(new Event(owner1, "Exam", false, LocalDateTime.of(2022, 07, 10, 10, 0),
					LocalDateTime.of(2022, 07, 10, 15, 0), "Technion", "Java", NONE_REP, false, guests1));
			eventDao.addEvent(new Event(owner2, "Party", false, LocalDateTime.of(2022, 07, 10, 10, 0),
					LocalDateTime.of(2022, 07, 10, 15, 0), "Home", "Birthday", NONE_REP, false, guests2));
		}
	}

	private void seedNotificationTable() throws DaoException {
		User user1 = userDao.getUserById(1);
		User user2 = userDao.getUserById(2);
		User user3 = userDao.getUserById(3);

		Event event1 = eventDao.getEventById(1);
		Event event2 = eventDao.getEventById(2);

		List<Notification> notifications = notificationDao.getAllNotifications();
		if (notifications == null || notifications.size() == 0) {
			notificationDao.addNotificationToDb(new Notification(event1, user1, "Study", HOURS, 10));
			notificationDao.addNotificationToDb(new Notification(event1, user1, "Study", HOURS, 5));
			notificationDao.addNotificationToDb(new Notification(event2, user2, "Clean up", HOURS, 3));
			notificationDao.addNotificationToDb(new Notification(event2, user3, "Buy gift", HOURS, 10));
			notificationDao.addNotificationToDb(new Notification(event2, user1, "Congrat", HOURS, 1));
		}
	}

}
