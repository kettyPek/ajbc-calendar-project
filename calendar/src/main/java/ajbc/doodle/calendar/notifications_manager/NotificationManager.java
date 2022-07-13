package ajbc.doodle.calendar.notifications_manager;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.PushProp;
import ajbc.doodle.calendar.daos.DaoException;

import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;

import ajbc.doodle.calendar.notifications_manager.threads.SendNotification;
import ajbc.doodle.calendar.services.NotificationManagerService;

import lombok.Setter;

@Setter
@Component
public class NotificationManager {

	private final int INITIAL_CAPACITY = 2;

	@Autowired
	private NotificationManagerService managerService;

	private PushProp pushProps;
	
	private ExecutorService threadPool;
	private Thread th;
	
	protected PriorityBlockingQueue<Notification> notificationsQueue = new PriorityBlockingQueue<Notification>(
			INITIAL_CAPACITY, new Comparator<Notification>() {
				@Override
				public int compare(Notification n1, Notification n2) {
					return n1.getAlertDateTime().isBefore(n2.getAlertDateTime()) ? -1 : 1;
				}
			});

	public void getNotificatiosFromDb() throws DaoException {
		List<Notification> notificationsList = managerService.getActiveNotifications();
		for (int i = 0; i < notificationsList.size(); i++)
			notificationsQueue.add(notificationsList.get(i));
	}

	public void initiateThread() throws DaoException {
		th = new Thread(() -> {
			try {
				run();
			} catch (DaoException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println("interrapted");
				e.printStackTrace();
			}
		});
		th.start();

	}

	@Transactional
	public void run() throws DaoException, InterruptedException {
		List<Notification> notificationsToSendNow;
		Notification nextNotification;
		User user;
		Duration duration;
		while (!notificationsQueue.isEmpty()) {
			try {
				nextNotification = notificationsQueue.peek();

				// Calculate delay for next notification
				duration = Duration.between(LocalDateTime.now(), nextNotification.getAlertDateTime());
				System.out.println("next notification: " + nextNotification.getAlertDateTime());
				System.out.println("sleep for " + duration.toSeconds());

				// sleep only if delay is positive
				if (duration.getSeconds() > 0)
					Thread.sleep(duration.toSeconds() * 1000);
			} catch (InterruptedException e) {
				System.out.println("interrupted");
				break;
			}

			// insert all notifications with same time and date to the list
			notificationsToSendNow = new ArrayList<Notification>();
			while (!notificationsQueue.isEmpty() && Duration
					.between(LocalDateTime.now(), notificationsQueue.peek().getAlertDateTime()).toSeconds() <= 0
					&& !notificationsQueue.isEmpty()) {
				notificationsToSendNow.add(notificationsQueue.poll());
			}

			// execute list of notifications
			threadPool = Executors.newCachedThreadPool();
			for (var notif : notificationsToSendNow) {
				user = managerService.getUserOfNotification(notif);
				if (user.isLoggedIn())
					threadPool.execute(new SendNotification(user, notif, pushProps));
				// every notification get deactivated
				managerService.InactivateNotification(notif);
			}
		}
	}

	// handling notification queue

	public void addNotification(Notification notification) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		addNotifiationToQueue(notification);
		initiateThread();
	}

	public void addNotificationsFromList(List<Notification> notifications) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		notifications.forEach(n -> addNotifiationToQueue(n));
		initiateThread();
	}

	public void updatedNotification(Notification notification) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		updateNotificationInQueue(notification);
		initiateThread();
	}

	public void updatedNotificationsFromList(List<Notification> notifications) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		for (var notif : notifications)
			updateNotificationInQueue(notif);
		initiateThread();
	}

	public void deleteNotification(Notification notification) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		deleteNotificationFromQueue(notification);
		initiateThread();
	}

	public void deleteNotifications(List<Notification> notifications) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		for (var notif : notifications)
			deleteNotificationFromQueue(notif);
		initiateThread();
	}

	protected void addNotifiationToQueue(Notification notification) {
		notificationsQueue.add(notification);
	}

	protected void updateNotificationInQueue(Notification notification) {
		Iterator<Notification> iterator = notificationsQueue.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getNotificationId() == notification.getNotificationId()) {
				iterator.remove();
				notificationsQueue.add(notification);
				break;
			}
		}
	}

	protected void deleteNotificationFromQueue(Notification notification) {
		Iterator<Notification> iterator = notificationsQueue.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getNotificationId() == notification.getNotificationId()) {
				iterator.remove();
				break;
			}
		}
	}

}
