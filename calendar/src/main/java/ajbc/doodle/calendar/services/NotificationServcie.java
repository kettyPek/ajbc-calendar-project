package ajbc.doodle.calendar.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;

@Component()
public class NotificationServcie {

	@Autowired
	private NotificationDao notificationDao;

	@Autowired
	private EventDao eventDao;

	@Autowired
	private UserDao userDao;
	
	// CRUD
	
	public Notification getNotificationById(Integer notificationId) throws DaoException {
		return notificationDao.getNotificationsById(notificationId);
	}

	public void addNotificationToEventOfUser(Notification notification) throws DaoException {
		notification.setEvent(eventDao.getEventById(notification.getEventId()));
		notification.setUser(userDao.getUserById(notification.getUserId()));
		notificationDao.addNotificationToDb(notification);
	}
	
	public void updateNotificaion(Notification notification,Integer notificationId) throws DaoException {
		Notification oldNotification = notificationDao.getNotificationsById(notificationId);
		notification.setNotificationId(notificationId);
		notification.setUser(oldNotification.getUser());
		notification.setEvent(oldNotification.getEvent());
		notificationDao.updateNotificationToDb(notification);
	}
	
	public void hardDeleteNotification(Notification notification) throws DaoException {
		notificationDao.deleteNotification(notification);
	}
	
	// Queries
	
	public List<Notification> getNotificationsByEventId(Integer eventId) throws DaoException{
		eventDao.getEventById(eventId);
		return notificationDao.getNotificationsByEventId(eventId);
	}

	public List<Notification> getAllNotifications() throws DaoException {
		return notificationDao.getAllNotifications();
	}
	
	public List<Notification> getAllNotificationsOfUserForEvent(Integer userId, Integer eventId) throws DaoException {
		return notificationDao.getNotificationsByUserIdAndEventId(userId, eventId);
	} 
	
	@Transactional
	public Notification getLastLoggedNotification(Integer userId, Integer eventId) throws DaoException {
		List<Notification> notifications =  notificationDao.getNotificationsByUserIdAndEventId(userId, eventId);
		return  notifications.get(notifications.size()-1);
	}

	public boolean userIsParticipant(int eventId, int userId) throws DaoException {
		Event event = eventDao.getEventById(eventId);
		return event.getGuests().stream().map(User::getUserId).anyMatch(id -> id == userId);
	}

	public void softDeleteNotification(Notification notification) throws DaoException {
		notification.setInactive(true);
		notificationDao.updateNotificationToDb(notification);
		
	}
	
	public void hardDeleteListOfNotifications(List<Notification> notifications) throws DaoException {
		notificationDao.deleteAll(notifications);
	}
	
	public Notification getLastAdded() throws DaoException {
		return notificationDao.getLastAdded();
	}
	
	public boolean isEventExist(Integer evenId) {
		try {
			eventDao.getEventById(evenId);
			return true;
		}catch (DaoException e) {
			return false;
		}	
	}
	
	public boolean isUserExist(Integer userId) {
		try {
			userDao.getUserById(userId);
			return true;
		}catch (DaoException e) {
			return false;
		}
	}



}
