package ajbc.doodle.calendar.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ajbc.doodle.calendar.services.NotificationServcie;

@RequestMapping("/notifications")
@RestController
public class NotificationController {
	
	
	@Autowired
	private NotificationServcie notificationService;

}
