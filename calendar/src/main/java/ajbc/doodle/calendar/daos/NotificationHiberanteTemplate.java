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
	public Notification getNotificationsById(Integer notificationId) throws DaoException {
		Notification notification = template.get(Notification.class, notificationId);
		if (notification == null)
			throw new DaoException("Notification with id " + notificationId + " is not in DB");
		return notification;
	}

	@Override
	public void addNotificationToDb(Notification notification) throws DaoException {
		template.persist(notification);
	}

	@Override
	public void updateNotificationToDb(Notification notification) throws DaoException {
		template.merge(notification);
	}
	
	@Override
	public void deleteNotification(Notification notification) throws DaoException {
		template.delete(notification);
	}

	// Queries

	@Override
	public List<Notification> getNotificationsByEventId(Integer eventId) throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		criteria.add(Restrictions.eq("eventId", eventId));
		return (List<Notification>) template
				.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

	@Override
	public List<Notification> getAllNotifications() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		return (List<Notification>) template
				.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

	@Override
	public List<Notification> getNotificationsByUserIdAndEventId(Integer userId, Integer eventId) throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		Criterion criterionUserId = Restrictions.eq("userId", userId);
		Criterion criterionEventId = Restrictions.eq("eventId", eventId);
		criteria.add(criterionUserId).add(criterionEventId);
		return (List<Notification>) template
				.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));

	}

	@Override
	public List<Notification> getAllActiveNotifications() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		criteria.add(Restrictions.eq("inactive", false));
		return (List<Notification>) template
				.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

	@Override
	public List<Notification> getAllNotificationsByUserId(Integer userId) throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		criteria.add(Restrictions.eq("userId", userId));
		return (List<Notification>) template
				.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

	@Override
	public void deleteAll(List<Notification> notifications) throws DaoException {
		template.deleteAll(notifications);
	}

	@Override
	public Notification getLastAdded() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(Notification.class);
		criteria.add(Restrictions.sqlRestriction("notificationId = (select max(notificationId) from Notifications)"));
		List<Notification> notifications =  (List<Notification>) template
				.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
		return notifications.get(0);
	}
	
	

}
