package ajbc.doodle.calendar.daos;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.entities.Notification;

@Transactional(rollbackFor = { DaoException.class }, readOnly = true)
public interface NotificationDao {

	// CRUD

	public default Notification getNotificationsById(Integer notificationId) throws DaoException {
		throw new DaoException("Method not implemented");
	} 
	
	@Transactional(readOnly = false)
	public default void addNotificationToDb(Notification notification) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	@Transactional(readOnly = false)
	public default void updateNotificationToDb(Notification notification) throws DaoException {
		throw new DaoException("Method not implemented");
	}

	// Queries

	public default List<Notification> getAllNotifications() throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default List<Notification> getNotificationsByUserIdAndEventId(Integer userId, Integer eventId) throws DaoException {
		throw new DaoException("Method not implemented");
	} 
	
	public default List<Notification> getAllActiveNotifications() throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	
}
