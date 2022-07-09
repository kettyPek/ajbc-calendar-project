package ajbc.doodle.calendar.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.Notification;

@SuppressWarnings("unchecked")
@Component(value = "notificationHT")
public class NotificationHiberanteTemplate implements NotificationDao {

	@Autowired
	private HibernateTemplate template;

	// CRUD

	@Override
	public void addNotificationToDb(Notification notification) throws DaoException {
		template.persist(notification);
	}

	// Queries

	@Override
	public List<Notification> getAllNotifications() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		return (List<Notification>) template
				.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

}
