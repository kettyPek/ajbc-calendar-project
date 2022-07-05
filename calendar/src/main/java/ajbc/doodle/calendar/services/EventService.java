package ajbc.doodle.calendar.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.entities.Event;

@Component()
public class EventService {

	@Autowired
	private EventDao eventDao;

	public void addEvent(Event event, Integer userId) throws DaoException {
		eventDao.addEvent(event);
	}

	public Event getEventbyId(Integer eventId) throws DaoException {
		return eventDao.getEventById(eventId);
	}
}
