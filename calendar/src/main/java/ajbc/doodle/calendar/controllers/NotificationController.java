package ajbc.doodle.calendar.controllers;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ajbc.doodle.calendar.PushProp;
import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.NotificationDao;

import ajbc.doodle.calendar.entities.Notification;

import ajbc.doodle.calendar.notifications_manager.NotificationManager;
import ajbc.doodle.calendar.services.NotificationServcie;

@RequestMapping("/notifications")
@RestController
public class NotificationController {

	@Autowired
	private NotificationDao notificationDao;

	@Autowired
	private NotificationServcie notificationService;

	@Autowired
	private PushProp pushProps;

	@Autowired
	private NotificationManager notificationManager;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createNotification(@RequestBody Notification notification, @RequestParam int userId,
			@RequestParam Integer eventId) {
		try {
			// TODO check if user logged in
			notificationService.addNotificationToEventOfUser(userId, eventId, notification);
			notification = notificationService.getLastLoggedNotification(eventId, eventId);
			notificationManager.addNotification(notification);
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getNotifications(@RequestParam Map<String, String> map) {
		List<Notification> notifications;
		try {
			if (map.containsKey("userId") && map.containsKey("eventId"))
				notifications = notificationService.getAllNotificationsOfUserForEvent(
						Integer.parseInt(map.get("userId")), Integer.parseInt(map.get("eventId")));
			else
				notifications = notificationService.getAllNotifications();
			return ResponseEntity.status(HttpStatus.OK).body(notifications);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@GetMapping(path = "/publicSigningKey", produces = "application/octet-stream")
	public byte[] publicSigningKey() {
		return pushProps.getServerKeys().getPublicKeyUncompressed();
	}

	@GetMapping(path = "/publicSigningKeyBase64")
	public String publicSigningKeyBase64() {
		return pushProps.getServerKeys().getPublicKeyBase64();
	}

	@PostConstruct
	public void update() throws DaoException {
		notificationManager.setPushProps(pushProps);
		List<Notification> notifications = notificationDao.getAllNotifications();
		notificationManager.inntializeNotificationsQueue(notifications);
	}

	@Scheduled(initialDelay = 3_000, fixedDelay = 1000_000)
	public void run() throws DaoException, InterruptedException {
		notificationManager.initiateThread();
	}

}
