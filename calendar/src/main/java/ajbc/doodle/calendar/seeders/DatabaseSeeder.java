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
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.enums.RepeatOptions;
import ajbc.doodle.calendar.enums.Units;
import ajbc.doodle.calendar.services.NotificationServcie;
import ajbc.doodle.calendar.services.UserService;

@Component
public class DatabaseSeeder {

	private final RepeatOptions NONE_REP = RepeatOptions.NONE;
	private final Units REMINDER_UNITS = Units.MINUTES;
	private final LocalDateTime EVENT_DATE_TIME = LocalDateTime.now().plusMinutes(7);
	private final LocalDateTime EVENT_DATE_TIME_END = LocalDateTime.now().plusHours(2);
	private final int NOTIFY_BEFORE = 1;

	@Autowired
	private UserService userService;
	
	@Autowired
	private EventDao eventDao;
	
	@Autowired
	private NotificationServcie notificationService;

	@EventListener
	public void seed(ContextRefreshedEvent event) throws DaoException {
		seedUsersTable();
		seedEventTable();
		seedNotificationTable();
	}

	private void seedUsersTable() throws DaoException {
		List<User> users = userService.getAllUsers();
		if (users == null || users.size() == 0) {
			userService.addUser(new User("ketty", "pekarsky", "ketty@gmail.com", LocalDate.of(1996, 05, 02)));
			userService.addUser(new User("dani", "kravtsov", "dani@gmail.com", LocalDate.of(1994, 04, 06)));
			userService.addUser(new User("nikol", "pekarsky", "nikol@gmail.com", LocalDate.of(2004, 04, 15)));
		}
	}

	private void seedEventTable() throws DaoException {
		List<Event> events = eventDao.getAllEvents();
		if (events == null || events.size() == 0) {
			User owner1 = userService.getUserById(1);
			User owner2 = userService.getUserById(2);
			Set<User> guests1 = new HashSet<User>();
			guests1.add(owner1);
			guests1.add(userService.getUserById(3));
			Set<User> guests2 = new HashSet<User>();
			guests2.add(owner2);
			guests2.add(userService.getUserById(1));
			guests2.add(userService.getUserById(3));

			eventDao.addEvent(new Event(owner1, "Exam", false, EVENT_DATE_TIME, EVENT_DATE_TIME_END, "Technion", "Java",
					NONE_REP, false, guests1));

			eventDao.addEvent(new Event(owner2, "Party", false, EVENT_DATE_TIME, EVENT_DATE_TIME_END, "Home",
					"Birthday", NONE_REP, false, guests2));
		}
	}

	private void seedNotificationTable() throws DaoException {
		List<Notification> notifications = notificationService.getAllNotifications();
		if (notifications == null || notifications.size() == 0) {
			User user1 = userService.getUserById(1);
			User user2 = userService.getUserById(2);
			User user3 = userService.getUserById(3);

			Event event1 = eventDao.getEventById(1);
			Event event2 = eventDao.getEventById(2);
			
			notificationService.addNotification(new Notification(event1, user1, event1.getTitle(), REMINDER_UNITS, 0));
			notificationService.addNotification(new Notification(event2, user2, event2.getTitle(), REMINDER_UNITS, 0));
			
			notificationService.addNotification(new Notification(event1, user1, "Study", REMINDER_UNITS, NOTIFY_BEFORE));
			notificationService.addNotification(new Notification(event1, user1, "Study", REMINDER_UNITS, NOTIFY_BEFORE + 1));
			notificationService.addNotification(new Notification(event2, user3, "Clean up", REMINDER_UNITS, NOTIFY_BEFORE + 2));
			notificationService.addNotification(new Notification(event2, user2, "Buy gift", REMINDER_UNITS, NOTIFY_BEFORE + 2));
			notificationService.addNotification(new Notification(event2, user1, "Congrat", REMINDER_UNITS, NOTIFY_BEFORE + 3));
			
		}
	}

}
