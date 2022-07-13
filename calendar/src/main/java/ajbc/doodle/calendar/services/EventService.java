package ajbc.doodle.calendar.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

@Component()
public class EventService {

	@Autowired
	private EventDao eventDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private NotificationDao notificationDao;

	// CRUD

	public void addEvent(Event event) throws DaoException {
		event.setOwner(userDao.getUserById(event.getOwnerId()));
		Set<User> participants = getParticipants(event.getGuestsEmails());
		participants.add(event.getOwner());
		event.setGuests(participants);
		eventDao.addEvent(event);
	}

	public void updateEvent(Event event, int ownerId) throws DaoException {
		event.setOwner(userDao.getUserById(ownerId));
		Set<User> participants = getParticipants(event.getGuestsEmails());
		event.setGuests(participants);
		eventDao.updateEvent(event);
	}

	// Queries

	public Event getEventbyId(Integer eventId) throws DaoException {
		return eventDao.getEventById(eventId);
	}

	public List<Event> getAllEventsOfUser(Integer userId) throws DaoException {
		User user = userDao.getUserById(userId);
		List<Event> events = user.getEvents().stream().collect(Collectors.toList());
		return filterNotificationsByUserId(userId, events);
	}

	public List<Event> getAllEvents() throws DaoException {
		return eventDao.getAllEvents();
	}

	public List<Event> getUpcomingEventsOfUser(Integer userId) throws DaoException {
		User user = userDao.getUserById(userId);
		List<Event> events = user.getEvents().stream()
				.filter(event -> event.getStartDateTime().isAfter(LocalDateTime.now())).collect(Collectors.toList());
		return filterNotificationsByUserId(userId, events);
	}

	public List<Event> getEventsOfUserBetween(Integer userId, LocalDateTime start, LocalDateTime end)
			throws DaoException {
		User user = userDao.getUserById(userId);
		List<Event> events = user.getEvents().stream()
				.filter(event -> event.getStartDateTime().isAfter(start) && event.getEndDateTime().isBefore(end))
				.collect(Collectors.toList());
		return filterNotificationsByUserId(userId, events);
	}

	public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) throws DaoException {
		return eventDao.getEventsBetween(start, end);
	}

	public List<Event> getEventsOfUserInTheNext(int userId, int hours, int minutes) throws DaoException {
		LocalDateTime eventBeforTime = LocalDateTime.now().minusHours(hours).minusMinutes(minutes);
		return getEventsOfUserBetween(userId, eventBeforTime, LocalDateTime.now());
	}

	public boolean userIsOwner(int eventId, int userId) throws DaoException {
		Event event = eventDao.getEventById(eventId);
		return (event.getOwnerId() == userId);
	}

	private Set<User> getParticipants(List<String> emails) throws DaoException {
		Set<User> participants = new HashSet<User>();
		for (var email : emails)
			participants.add(userDao.getUserByEmail(email));
		return participants;
	}

	private List<Event> filterNotificationsByUserId(Integer userId, List<Event> events) {
		List<Event> result = new ArrayList<>();
		for (Event event : events) {
			Set<Notification> notifications = event.getNotifications().stream().filter(n -> n.getUserId() == userId)
					.collect(Collectors.toSet());
			event.setNotifications(notifications);
			result.add(event);
		}
		return result;
	}

	public boolean isUserExists(Integer userId) {
		try {
			userDao.getUserById(userId);
			return true;
		} catch (DaoException e) {
			return false;
		}
	}

	public void softDeleteEvenet(Event event) throws DaoException {
		event.setInactive(true);
		eventDao.updateEvent(event);
	}

	public void hardDeleteEvenet(Event event) throws DaoException {
		 event.setOwner(null);
		 List<Notification> notifications = event.getNotifications().stream().collect(Collectors.toList());
		 notificationDao.deleteAll(notifications);
		 eventDao.deleteEvent(event);
	}

}
