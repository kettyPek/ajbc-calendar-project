package ajbc.doodle.calendar.notifications_manager;

import java.time.LocalDateTime;
import java.time.Duration;

import java.util.Comparator;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	public void inntializeNotificationsQueue(List<Notification> notificationsList) {
		executorService = Executors.newCachedThreadPool();
		for (int i = 0; i < notificationsList.size(); i++)
			notificationsQueue.add(notificationsList.get(i));
	}

	public void run() throws DaoException, InterruptedException {
		notificationsQueue.forEach(n -> {
			try {
				System.out.println(calculateNotificationTime(n));
			} catch (DaoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
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
				Thread.sleep((duration.toSeconds() - 60) * 1000);
			}
		}
	}
	
	public void addNotification(Notification notification) {
		notificationsQueue.add(notification);
	}

//	public void run() throws DaoException {
//		User user;
//		Object message;
//		System.out.println("in run()");
//		Notification not ,notNext;
//		Duration duration ;
//		while(!notificationsQueue.isEmpty()) {
//			not = notificationsQueue.poll();
//			notNext = notificationsQueue.peek();
//			user = userDao.getUserById(not.getUserId());
//			System.out.println("now: " + calculateNotificationTime(not));
//			if(notNext!=null) {
//				duration = Duration.between(calculateNotificationTime(notNext),LocalDateTime.now());
//				System.out.println("next: " + calculateNotificationTime(notNext));
//				System.out.println("minutes : "  + duration.toMinutes());
//			}
//			if(user.isLoggedIn()) {
//				message = new PushMessage("message: ", not.getTitle());
//				byte[] result;
//				try {
//					result = pushProps.getCryptoService().encrypt(pushProps.getObjectMapper().writeValueAsString(message), user.getP256dh(),
//							user.getAuth(), 0);
//					sendPushMessage(user.getEndPoint(), result);
//					System.out.println("massge: " + not.getTitle() + " sent to user " + user.getEmail());
//					
//					
//				} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
//						| InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
//						| BadPaddingException | JsonProcessingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
//	}

	private LocalDateTime calculateNotificationTime(Notification notification) throws DaoException {
		Event event = eventDao.getEventById(notification.getEventId());
		LocalDateTime date;
		if (notification.getUnits() == Units.HOURS)
			date = event.getStartDateTime().minusHours(notification.getQuantity());
		else
			date = event.getStartDateTime().minusMinutes(notification.getQuantity());
		return date;
	}

}
