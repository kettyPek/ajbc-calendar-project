package ajbc.doodle.calendar.notifications_manager;

import java.time.LocalDateTime;
import java.time.Duration;import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.PushProp;
import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;

import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;

import ajbc.doodle.calendar.enums.Units;
import ajbc.doodle.calendar.notifications_manager.threads.SendNotification;
import lombok.Setter;

@Setter
@Component
public class NotificationManager {

	@Autowired
	private UserDao userDao;

	@Autowired
	private EventDao eventDao;

	private PriorityBlockingQueue<Notification> notificationsQueue = new PriorityBlockingQueue<Notification>(2,
			new Comparator<Notification>() {
				@Override
				public int compare(Notification n1, Notification n2) {
						return calculateNotificationTime(n1).isBefore(calculateNotificationTime(n2)) ? -1 : 1;
				}
			});

	private PushProp pushProps;
	private ExecutorService executorService;
	private Thread th;

	public void inntializeNotificationsQueue(List<Notification> notificationsList) {
		executorService = Executors.newCachedThreadPool();
		notificationsList = notificationsList.stream().filter(n -> calculateNotificationTime(n).isAfter(LocalDateTime.now())).collect(Collectors.toList());
		for (int i = 0; i < notificationsList.size(); i++)
			notificationsQueue.add(notificationsList.get(i));
	}

	public void initiateThread() throws DaoException {
		System.out.println("in initiateThread()");
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

	public void run() throws DaoException, InterruptedException {
		Notification currntNotification, nextNotification;
		User user;
		Duration duration;
		while (!notificationsQueue.isEmpty()) {
			nextNotification = notificationsQueue.peek();
			duration = Duration.between(LocalDateTime.now(), calculateNotificationTime(nextNotification));
			System.out.println("next notification: " + calculateNotificationTime(nextNotification));
			try {
				Thread.sleep(duration.toSeconds() * 1000);
			} catch (InterruptedException e) {
				System.out.println("interrupted");
				break;
			}
			currntNotification = notificationsQueue.poll();
			user = userDao.getUserById(currntNotification.getUserId());
			if (user.isLoggedIn())
				executorService.execute(new SendNotification(user, currntNotification, pushProps));
			else
				System.out.println("not executed");
		}
		System.out.println("no notifications");
	}

	public void addNotification(Notification notification) throws DaoException {
		th.interrupt();
		notificationsQueue.add(notification);
		initiateThread();
	}

	public void updatedNotification(Notification notification) throws DaoException {
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

	public void deleteNotification(Notification notification) throws DaoException {
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

	public LocalDateTime calculateNotificationTime(Notification notification)  {
		Event event;
		try {
			event = eventDao.getEventById(notification.getEventId());
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
