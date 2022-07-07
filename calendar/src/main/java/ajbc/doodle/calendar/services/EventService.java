package ajbc.doodle.calendar.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
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

	// CRUD
	public void addEvent(Event event, Integer id) throws DaoException {
		event.setOwner(userDao.getUserById(id));
		eventDao.addEvent(event);
	}

	public void updateEvent(Event event) throws DaoException {
		eventDao.updateEvent(event);
	}

	// Queries

	public Event getEventbyId(Integer eventId) throws DaoException {
		return eventDao.getEventById(eventId);
	}

	@Transactional
	public List<Event> getAllEventsOfUser(Integer userId) throws DaoException {
		User user = userDao.getUserById(userId);
		List<Event> events = user.getEvents().stream().collect(Collectors.toList());
		List<Event> result = new ArrayList<>();
		for (Event event : events) {
			Set<Notification> notifications = event.getNotifications().stream().filter(n -> n.getUserId() == userId).collect(Collectors.toSet());
			event.setNotifications(notifications);
			result.add(event);		
		}
		return result;
	}

	public List<Event> getAllEvents() throws DaoException {
		return eventDao.getAllEvents();
	}

	public boolean userIsOwner(int eventId, int userId) throws DaoException {
		Event event = eventDao.getEventById(eventId);
		return (event.getOwnerId() == userId);
	}

}
