package ajbc.doodle.calendar.daos;


import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.User;


@SuppressWarnings("unchecked")
@Component(value = "eventHT")
public class EventHibernateTemplate implements EventDao{

	@Autowired
	private HibernateTemplate template;

	// CRUD
	@Override
	public void addEvent(Event event) throws DaoException {
		template.persist(event);
	}

	// Queries
	
	@Override
	public Event getEventById(Integer eventId) throws DaoException {
		Event event = template.get(Event.class, eventId);
		if(event == null)
			throw new DaoException("Event doesnt exists");
		return event;
	}

	@Override
	public List<Event> getAllEvents() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Event.class);
		return (List<Event>) template.findByCriteria(criteria);
	}
	
	
	
	

	

	
	
	
	
	
	
	
	
	
	
}
