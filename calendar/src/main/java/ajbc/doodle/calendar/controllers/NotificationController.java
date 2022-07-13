package ajbc.doodle.calendar.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	public ResponseEntity<?> createNotification(@RequestBody Notification notification) {
		try {
			if (notificationService.userIsParticipant(notification.getEventId(), notification.getUserId())) {
				notificationService.addNotificationToEventOfUser(notification);
				notification = notificationService.getLastAdded();
				notificationManager.addNotification(notification);
				return ResponseEntity.status(HttpStatus.CREATED).body(notification);
			}
			throw new DaoException("User is not participating in ther event");
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.POST, path = "/list")
	public ResponseEntity<?> createNotificationsFromList(@RequestBody List<Notification> notifications) {
		try {
			Notification lastLogged;
			List<Notification> loggedNotifications = new ArrayList<Notification>();
			for(var notif : notifications) 
				if(!notificationService.userIsParticipant(notif.getEventId(), notif.getUserId()))
					throw new DaoException("User is not participating in ther event");
			for(var notif : notifications) {
				notificationService.addNotificationToEventOfUser(notif);
				lastLogged = notificationDao.getLastAdded();
				loggedNotifications.add(lastLogged);
			}
			notificationManager.addNotificationsFromList(loggedNotifications);
			return ResponseEntity.status(HttpStatus.CREATED).body(loggedNotifications);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{id}")
	public ResponseEntity<?> getNotificationbyId(@PathVariable Integer id) {
		try {
			Notification notification = notificationService.getNotificationById(id);
			return ResponseEntity.status(HttpStatus.OK).body(notification);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/{id}")
	public ResponseEntity<?> updateNotification(@RequestBody Notification notification, @PathVariable Integer id) {
		try {
			notificationService.updateNotificaion(notification, id);
			notification = notificationService.getNotificationById(id);
			notificationManager.updatedNotification(notification);
			return ResponseEntity.status(HttpStatus.OK).body(notification);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<?> updateNotificationsFromList(@RequestBody Map<Integer, Notification> notificationsMap) {
		try {
			List<Integer> ids = notificationsMap.keySet().stream().collect(Collectors.toList());
			for (var id : ids) 
				notificationService.updateNotificaion(notificationsMap.get(id), id);
			List<Notification> updatedNotifications = getNotificationsById(ids);
			notificationManager.updatedNotificationsFromList(updatedNotifications);
			return ResponseEntity.status(HttpStatus.OK).body(updatedNotifications);
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
			if (map.containsKey("eventId"))
				notifications = notificationService.getNotificationsByEventId(Integer.parseInt(map.get("eventId")));
			else
				notifications = notificationService.getAllNotifications();
			return ResponseEntity.status(HttpStatus.OK).body(notifications);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
	public ResponseEntity<?> deleteNotification(@PathVariable Integer id, @RequestParam String deleteType) {
		try {
			Notification notification = notificationDao.getNotificationsById(id);
			if (deleteType.equalsIgnoreCase("HARD"))
				notificationService.hardDeleteNotification(notification);
			else
				notificationService.softDeleteNotification(notification);
			notificationManager.deleteNotification(notification);
			return ResponseEntity.status(HttpStatus.OK).body(notification);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteNotificationListOfNotifications(@RequestBody List<Integer> ids,
			@RequestParam String deleteType) {
		try {
			List<Notification> notifications = getNotificationsById(ids);
			if (deleteType.equalsIgnoreCase("HARD"))
				notificationService.hardDeleteListOfNotifications(notifications);
			else
				for (var notif : notifications)
					notificationService.softDeleteNotification(notif);
			notificationManager.deleteNotifications(notifications);
			return ResponseEntity.status(HttpStatus.OK).body(ids);
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

	@Scheduled(initialDelay = 3_000, fixedDelay = 1000_000)
	public void run() throws DaoException, InterruptedException {
		notificationManager.setPushProps(pushProps);
		notificationManager.getNotificatiosFromDb();
		notificationManager.initiateThread();
	}

	private List<Notification> getNotificationsById(List<Integer> ids) throws DaoException {
		List<Notification> notifications = new ArrayList<Notification>();
		for (var id : ids)
			notifications.add(notificationService.getNotificationById(id));
		return notifications;
	}
	
	
}
