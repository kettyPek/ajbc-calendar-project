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

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;

import ajbc.doodle.calendar.enums.Units;
import ajbc.doodle.calendar.notifications_manager.threads.SendNotification;
import ajbc.doodle.calendar.services.NotificationManagerService;
import lombok.Setter;

@Setter
@Component
public class NotificationManager {

	@Autowired
	private NotificationManagerService managerService;

	private PriorityBlockingQueue<Notification> notificationsQueue = new PriorityBlockingQueue<Notification>(2,
			new Comparator<Notification>() {
				@Override
				public int compare(Notification n1, Notification n2) {
					return calculateNotificationTime(n1).isBefore(calculateNotificationTime(n2)) ? -1 : 1;
				}
			});

	private PushProp pushProps;
	private ExecutorService threadPool;
	private Thread th;

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
				System.out.println("interrupted");
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
				notificationsQueue.forEach(n -> System.out.println(n.getNotificationId()));
				nextNotification = notificationsQueue.peek();

				// Calculate delay for next notification
				duration = Duration.between(LocalDateTime.now(), calculateNotificationTime(nextNotification));
				System.out.println("next notification: " + calculateNotificationTime(nextNotification));
				System.out.println("sleep for " + duration.toSeconds());

				// sleep only if delay is positive
				if (duration.getSeconds() > 0)
					Thread.sleep(duration.toSeconds() * 1000);
			} catch (InterruptedException e) {
				System.out.println("interrupted");
				break;
			}
			// insert all notifications with same time to list
			notificationsToSendNow = new ArrayList<Notification>();
			while (!notificationsQueue.isEmpty() && Duration
					.between(LocalDateTime.now(), calculateNotificationTime(notificationsQueue.peek())).toSeconds() <= 0
					&& !notificationsQueue.isEmpty()) {
				notificationsToSendNow.add(notificationsQueue.poll());
			}

			// execute list of notifications
			threadPool = Executors.newCachedThreadPool();
			for (var notif : notificationsToSendNow) {
				user = managerService.getUserOfNotification(notif);
				if (user.isLoggedIn())
					threadPool.execute(new SendNotification(user, notif, pushProps));
				managerService.InactivateNotification(notif);
			}
		}
		System.out.println("no notifications");
	}

	public void addNotification(Notification notification) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		notificationsQueue.add(notification);
		initiateThread();
	}

	public void addNotificationsFromList(List<Notification> notifications) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		notifications.forEach(n -> notificationsQueue.add(n));
		initiateThread();
	}

	public void updatedNotification(Notification notification) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		Iterator<Notification> iterator = notificationsQueue.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getNotificationId() == notification.getNotificationId()) {
				iterator.remove();
				notificationsQueue.add(notification);
				break;
			}
		}
		initiateThread();
	}

	public void updatedNotificationsFromList(List<Notification> notifications) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		Iterator<Notification> iterator;
		for (var notif : notifications) {
			iterator = notificationsQueue.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getNotificationId() == notif.getNotificationId()) {
					iterator.remove();
					notificationsQueue.add(notif);
					break;
				}
			}
		}
		initiateThread();
	}

	public void deleteNotification(Notification notification) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		Iterator<Notification> iterator = notificationsQueue.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getNotificationId() == notification.getNotificationId()) {
				iterator.remove();
				break;
			}
		}
		initiateThread();
	}

	public void deleteNotifications(List<Notification> notifications) throws DaoException {
		if (th.isAlive())
			th.interrupt();
		Iterator<Notification> iterator;
		for (var notif : notifications) {
			iterator = notificationsQueue.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getNotificationId() == notif.getNotificationId()) {
					iterator.remove();
					break;
				}
			}
		}
		initiateThread();
	}

	public LocalDateTime calculateNotificationTime(Notification notification) {
		Event event;
		try {
			event = managerService.getEvenOfNotification(notification);
			LocalDateTime date;
			if (notification.getUnits() == Units.HOURS)
				date = event.getStartDateTime().minusHours(notification.getQuantity());
			else
				date = event.getStartDateTime().minusMinutes(notification.getQuantity());
			return date;
		} catch (DaoException e) {
			e.printStackTrace();
		}
		return null;
	}

}
