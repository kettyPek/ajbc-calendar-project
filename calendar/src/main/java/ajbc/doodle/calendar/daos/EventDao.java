package ajbc.doodle.calendar.daos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.entities.Event;

@Transactional(rollbackFor = { DaoException.class }, readOnly = true)
public interface EventDao {

	@Transactional(readOnly = false)
	public default void addEvent(Event event) throws DaoException {
		throw new DaoException("Method not implemented");
	}

	@Transactional(readOnly = false)
	public default void updateEvent(Event event) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	@Transactional(readOnly = false)
	public default void deleteEvent(Event event) throws DaoException {
		throw new DaoException("Method not implemented");
	}

	public default Event getEventById(Integer eventId) throws DaoException {
		throw new DaoException("Method not implemented");
	}

	public default List<Event> getAllEvents() throws DaoException {
		throw new DaoException("Method not implemented");
	}

	public default List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end)throws DaoException {
		throw new DaoException("Method not implemented");
	}

}
