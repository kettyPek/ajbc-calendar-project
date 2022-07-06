package ajbc.doodle.calendar.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.NotificationDao;

@Component()
public class NotificationServcie {
	
	@Autowired
	private NotificationDao notificationDao;
	
	

}
