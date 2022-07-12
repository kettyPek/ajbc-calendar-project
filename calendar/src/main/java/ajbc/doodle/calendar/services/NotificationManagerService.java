package ajbc.doodle.calendar.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;

@Component
public class NotificationManagerService {
	
	@Autowired
	private NotificationDao notificationDao;

	@Autowired
	private EventDao eventDao;

	@Autowired
	private UserDao userDao;
	
	public User getUserOfNotification(Notification notification) throws DaoException {
		return userDao.getUserById(notification.getUserId());
	}
	
	public Event getEvenOfNotification(Notification notification) throws DaoException {
		return eventDao.getEventById(notification.getEventId());
	}
	
	public void InactivateNotification(Notification notification) throws DaoException {
		notification = notificationDao.getNotificationsById(notification.getNotificationId());
		notification.setInactive(true);
		notificationDao.updateNotificationToDb(notification);
	}

	public List<Notification> getActiveNotifications() throws DaoException {
		return notificationDao.getAllActiveNotifications();
	}
	
	

}
