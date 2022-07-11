package ajbc.doodle.calendar.notifications_manager;

import java.time.LocalDateTime;
import java.time.Duration;

import java.util.Comparator;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import lombok.Getter;
import lombok.Setter;

@Setter
@Component
public class NotificationManager {

	@Autowired
	private UserDao userDao;

	@Autowired
	private EventDao eventDao;

	private Queue<Notification> notificationsQueue = new PriorityQueue<Notification>(new Comparator<Notification>() {
		@Override
		public int compare(Notification n1, Notification n2) {
			try {
				return calculateNotificationTime(n1).isBefore(calculateNotificationTime(n2)) ? -1 : 1;
			} catch (DaoException e) {
				return 1;
			}
		}
	});

	private PushProp pushProps;
	private ExecutorService executorService;
	private Thread th;

	public void inntializeNotificationsQueue(List<Notification> notificationsList) {
		executorService = Executors.newCachedThreadPool();		
		for (int i = 0; i < notificationsList.size(); i++)
			notificationsQueue.add(notificationsList.get(i));
	}


	public void initiateThread() throws DaoException {
		System.out.println("in initiateThread()");
		int waitingTime = Duration.between(LocalDateTime.now(), calculateNotificationTime(notificationsQueue.peek()))
				.toSecondsPart();
		th = new Thread(() -> {try {
			run();
		} catch (DaoException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}});
		try {
			Thread.sleep(waitingTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		th.start();

	}


	public void run() throws DaoException, InterruptedException {
		Notification currntNotification, nextNotification;
		User user;
		Duration duration;
		while (!notificationsQueue.isEmpty()) {
			currntNotification = notificationsQueue.poll();
			user = userDao.getUserById(currntNotification.getUserId());
			if (user.isLoggedIn())
				executorService.execute(new SendNotification(user, currntNotification, pushProps));
			else
				System.out.println("not executed");
			nextNotification = notificationsQueue.peek();
			if (nextNotification != null) {
				duration = Duration.between(LocalDateTime.now(), calculateNotificationTime(nextNotification));
				System.out.println("next notification: " + calculateNotificationTime(nextNotification));
				System.out.println(duration.toSeconds());
				try{
					Thread.sleep(duration.toSeconds() * 1000);
				}catch(InterruptedException e) {
					System.out.println("inettapted");
					break;
				}
			}
		}
	}

	public void addNotification(Notification notification) throws DaoException {
		if(notificationIsEarliest(notification)) {
			th.interrupt();
			notificationsQueue.add(notification);
			initiateThread();
		}
		else
			notificationsQueue.add(notification);	
	}


	public LocalDateTime calculateNotificationTime(Notification notification) throws DaoException {
		Event event = eventDao.getEventById(notification.getEventId());
		LocalDateTime date;
		if (notification.getUnits() == Units.HOURS)
			date = event.getStartDateTime().minusHours(notification.getQuantity());
		else
			date = event.getStartDateTime().minusMinutes(notification.getQuantity());
		return date;
	}
	
	private boolean notificationIsEarliest(Notification notification) throws DaoException {
		LocalDateTime startOfNewNotification = calculateNotificationTime(notification);
		LocalDateTime startOfFirstInQueue = calculateNotificationTime(notificationsQueue.peek());
		return (startOfNewNotification.isBefore(startOfFirstInQueue)) ;
	}

}
