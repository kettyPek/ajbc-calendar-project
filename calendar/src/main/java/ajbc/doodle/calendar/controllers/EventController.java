package ajbc.doodle.calendar.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.entities.ErrorMessage;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.services.EventService;
import ajbc.doodle.calendar.services.UserService;

@RequestMapping("/event")
@RestController
public class EventController {

	@Autowired
	private EventService eventService;

//	@RequestMapping(method = RequestMethod.GET)
//	public ResponseEntity<List<User>> getAllUsers() throws DaoException {
//		List<User> allusers = userService.getAllUsers();
//		if (allusers == null)
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//		return ResponseEntity.ok(allusers);
//	}

	@RequestMapping(method = RequestMethod.POST, path = "/{id}")
	public ResponseEntity<?> createEvent(@RequestBody Event event, @PathVariable Integer userId) {
		try {
			eventService.addEvent(event,userId);
			event = eventService.getEventbyId(event.getEventId());
			return ResponseEntity.status(HttpStatus.CREATED).body(event);
		} catch (DaoException e) {
			ErrorMessage errorMsg = new ErrorMessage();
			errorMsg.setData(e.getMessage());
			errorMsg.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMsg);
		}
	}

//	@RequestMapping(method = RequestMethod.PUT, path = "/{id}")
//	public ResponseEntity<?> updateUser(@RequestBody User user, @PathVariable Integer id) {
//		try {
//			user.setUserId(id);
//			userService.updateUser(user);
//			user = userService.getUserById(id);
//			return ResponseEntity.status(HttpStatus.OK).body(user);
//		} catch (DaoException e) {
//			ErrorMessage errorMsg = new ErrorMessage();
//			errorMsg.setData(e.getMessage());
//			errorMsg.setMessage(e.getMessage());
//			return ResponseEntity.status(HttpStatus.valueOf(500)).body(errorMsg);
//		}
//	}
//
//	@RequestMapping(method = RequestMethod.GET, path = "/id/{id}")
//	public ResponseEntity<?> getUserById(@PathVariable Integer id) throws DaoException {
//		try {
//			User user = userService.getUserById(id);
//			return ResponseEntity.ok(user);
//		} catch (DaoException e) {
//			ErrorMessage errorMsg = new ErrorMessage();
//			errorMsg.setData(e.getMessage());
//			errorMsg.setMessage(e.getMessage());
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMsg);
//		}
//
//	}
//
//	@RequestMapping(method = RequestMethod.GET, path = "/email/{email}")
//	public ResponseEntity<?> getUserByEmail(@PathVariable String email) throws DaoException {
//		try {
//			User user = userService.getUserByEmail(email);
//			return ResponseEntity.ok(user);
//		} catch (DaoException e) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//		}
//	}
//	
//	@RequestMapping(method = RequestMethod.DELETE,path = "/{id}")
//	public ResponseEntity<?> deleteUser(@PathVariable Integer id, @RequestParam String deleteType) throws DaoException {
//		try {
//			User user = userService.getUserById(id);
//			if(deleteType.toUpperCase() == "HARD")
//				userService.hardDeleteUser(user);
//			else
//				userService.softDeleteUser(user);
//			System.out.println(deleteType);
//			return ResponseEntity.ok(user);
//		} catch (DaoException e) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//		}
//	}

}
