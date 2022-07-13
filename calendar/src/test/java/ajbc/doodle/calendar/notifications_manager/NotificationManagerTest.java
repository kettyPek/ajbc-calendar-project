package ajbc.doodle.calendar.notifications_manager;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import ajbc.doodle.calendar.entities.Notification;



class NotificationManagerTest {
	
	private final LocalDateTime ALERT1 = LocalDateTime.now();
	private final LocalDateTime ALERT2 = LocalDateTime.now().plusHours(2);
	
	private Notification notification1;
	private Notification notification2;
	
	private NotificationManager notificationManager;

	public NotificationManagerTest() {
		notificationManager = new NotificationManager();
		notification1 = new Notification();
		notification1.setNotificationId(1);
		notification1.setAlertDateTime(ALERT1);
		notification2 = new Notification();
		notification2.setNotificationId(3);
		notification2.setAlertDateTime(ALERT2);
	}
	
	@BeforeEach
	void setDefault() {
		notificationManager.notificationsQueue.add(notification1);	
	}
	
	@Test
	@DisplayName("comparator test")
	void testaAddNotifiationToQueueComparator() {
		notificationManager.addNotifiationToQueue(notification2);
		assertEquals(notification1, notificationManager.notificationsQueue.peek());
		
	}
	
	@Test
	@DisplayName("ADD test")
	void testaAddNotifiationToQueue() {
		notificationManager.addNotifiationToQueue(notification2);
		assertTrue(notificationManager.notificationsQueue.contains(notification2));
		
	}
	
	@Test
	@DisplayName("UPDATE test")
	void testaUPDATENotifiationToQueue() {
		String expected = "test";
		notification1.setTitle(expected);
		notificationManager.updateNotificationInQueue(notification1);
		assertEquals(expected, notificationManager.notificationsQueue.peek().getTitle());
		
	}
	
	@Test
	@DisplayName("UPDATE test comparator")
	void testaUPDATENotifiationComparator() {
		notificationManager.addNotifiationToQueue(notification2);
		notification1.setAlertDateTime(ALERT1.plusDays(1));
		notificationManager.updateNotificationInQueue(notification1);
		assertEquals(notification2, notificationManager.notificationsQueue.peek());
		
	}
	
	@Test
	@DisplayName("DELETE test")
	void testaDELETEotifiationToQueue() {
		notificationManager.deleteNotificationFromQueue(notification1);
		assertTrue(notificationManager.notificationsQueue.isEmpty());
		
	}
	


}
