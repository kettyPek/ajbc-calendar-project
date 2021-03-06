package ajbc.doodle.calendar.daos;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Criteria;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.Event;

@SuppressWarnings("unchecked")
@Component(value = "eventHT")
public class EventHibernateTemplate implements EventDao {

	@Autowired
	private HibernateTemplate template;

	// CRUD
	@Override
	public void addEvent(Event event) throws DaoException {
		template.persist(event);
	}

	@Override
	public void updateEvent(Event event) throws DaoException {
		template.merge(event);
	}
	
	@Override
	public void deleteEvent(Event event) throws DaoException {
		template.delete(event);
	}


	// Queries
	
	@Override
	public Event getEventById(Integer eventId) throws DaoException {
		Event event = template.get(Event.class, eventId);
		if (event == null)
			throw new DaoException("Event doesnt exists");
		return event;
	}

	@Override
	public List<Event> getAllEvents() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Event.class);
		return (List<Event>) template.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

	@Override
	public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Event.class);
		criteria.add(Restrictions.ge("startDateTime", start)).add(Restrictions.le("endDateTime", end));
		return (List<Event>) template.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

}
