package ajbc.doodle.calendar.notifications_manager.threads;

import java.util.PriorityQueue;

import org.springframework.beans.factory.annotation.Autowired;

import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Notification;

public class PushNotifications implements Runnable{
	
	@Autowired
	private UserDao userDao;

	private PriorityQueue<Notification> notificationsQueue;
	
	public PushNotifications(PriorityQueue<Notification> notificationsQueue) {
		this.notificationsQueue = notificationsQueue;
	}
	@Override
	public void run() {
	}
	

}
