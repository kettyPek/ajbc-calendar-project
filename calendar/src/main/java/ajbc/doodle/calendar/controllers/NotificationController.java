package ajbc.doodle.calendar.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ajbc.doodle.calendar.daos.DaoException;

import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.services.NotificationServcie;

@RequestMapping("/notifications")
@RestController
public class NotificationController {
	
	
	@Autowired
	private NotificationServcie notificationService;
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createNotification(@RequestBody Notification notification, @RequestParam int userId ,@RequestParam Integer eventId) {
		try {
			// TODO check if user logged in
			notificationService.addNotificationToEventOfUser(userId, eventId, notification);
			// TODO send the created notification
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllNotifications() {
		try {
			List<Notification> notifications = notificationService.getAllNotifications();
			return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

}
