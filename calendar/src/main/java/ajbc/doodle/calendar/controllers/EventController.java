package ajbc.doodle.calendar.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.notifications_manager.NotificationManager;
import ajbc.doodle.calendar.services.EventService;

@RequestMapping("/event")
@RestController
public class EventController {

	@Autowired
	private EventService eventService;

	@Autowired
	private NotificationManager notificationManager;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createEvent(@RequestBody Event event) {
		try {
			if (!eventService.isUserExists(event.getOwnerId()))
				throw new DaoException("User " + event.getOwnerId() + " does not exist in DB");
			eventService.addEvent(event);
			event = eventService.getEventbyId(event.getEventId());
			return ResponseEntity.status(HttpStatus.CREATED).body(event);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.POST, path = "/list")
	public ResponseEntity<?> createEventsFromList(@RequestBody List<Event> events) {
		List<String> uncratedEvents = new ArrayList<String>();
		for (int i = 0; i < events.size(); i++) {
			try {
				if (!eventService.isUserExists(events.get(i).getOwnerId()))
					throw new DaoException("User " + events.get(i).getOwnerId() + " does not exist in DB");
				eventService.addEvent(events.get(i));
			} catch (DaoException e) {
				uncratedEvents.add("event " + i + " in the list wasnt created: " + e.getMessage());
			}
		}
		if (!uncratedEvents.isEmpty())
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(uncratedEvents);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<Event>> getAllEvents(@RequestParam Map<String, String> paramsMap) throws DaoException {
		List<Event> events;
		if (paramsMap.containsKey("startDateTime") && paramsMap.containsKey("endDateTime"))
			events = eventService.getEventsBetween(LocalDateTime.parse(paramsMap.get("startDateTime")),
					LocalDateTime.parse(paramsMap.get("endDateTime")));
		else
			events = eventService.getAllEvents();
		return ResponseEntity.ok(events);
	}

	@RequestMapping(method = RequestMethod.GET, path = "/user/{id}")
	public ResponseEntity<?> getAllEventsOfUserById(@PathVariable Integer id,
			@RequestParam Map<String, String> paramsMap) throws DaoException {
		List<Event> events;
		try {
			if (!eventService.isUserExists(id))
				throw new DaoException("User " + id + " does not exist in DB");
			if (paramsMap.containsKey("startDateTime") && paramsMap.containsKey("endDateTime"))
				events = eventService.getEventsOfUserBetween(id, LocalDateTime.parse(paramsMap.get("startDateTime")),
						LocalDateTime.parse(paramsMap.get("endDateTime")));
			else if (paramsMap.containsKey("minutes") && paramsMap.containsKey("houres"))
				events = eventService.getEventsOfUserInTheNext(id, Integer.parseInt(paramsMap.get("houres")),
						Integer.parseInt(paramsMap.get("minutes")));
			else
				events = eventService.getAllEventsOfUser(id);
			return ResponseEntity.ok(events);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}

	}

	@RequestMapping(method = RequestMethod.GET, path = "/user/{id}/upcoming")
	public ResponseEntity<?> getUpcomingEventsOfUser(@PathVariable Integer id) throws DaoException {
		try {
			if (!eventService.isUserExists(id))
				throw new DaoException("User " + id + " does not exist in DB");
			List<Event> events = eventService.getUpcomingEventsOfUser(id);
			return ResponseEntity.ok(events);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}

	}

	@RequestMapping(method = RequestMethod.PUT, path = "/{eventId}/owner/{ownerId}")
	public ResponseEntity<?> updateEvent(@RequestBody Event event, @PathVariable Integer eventId,
			@PathVariable Integer ownerId) {
		try {
			// TODO check event exist
			if (!eventService.userIsOwner(eventId, ownerId))
				throw new DaoException("Only the owner can update the event");
			Event oldEvent = eventService.getEventbyId(eventId);
			event.setEventId(eventId);
			eventService.updateEvent(event, ownerId);
			event = eventService.getEventbyId(eventId);
			if (!oldEvent.getStartDateTime().isEqual(event.getStartDateTime())) {
				List<Notification> notifications = eventService.getNotificationsOfEvent(eventId);
				notificationManager.updatedNotificationsFromList(notifications);
			}
			return ResponseEntity.status(HttpStatus.OK).body(event);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.PUT, path = "/list")
	public ResponseEntity<?> updateEventsFromList(@RequestBody Map<Integer, Event> eventsMap) {
		// TODO what to do with the notifications
		List<String> uncupdatedEvents = new ArrayList<String>();
		List<Integer> ids = eventsMap.keySet().stream().collect(Collectors.toList());
		for (var id : ids) {
			try {
				eventsMap.get(id).setEventId(id);
				eventService.updateEvent(eventsMap.get(id), eventsMap.get(id).getOwnerId());
			} catch (DaoException e) {
				uncupdatedEvents.add("event with id " + id + " wasnt updated: " + e.getMessage());
			}
		}
		if (!uncupdatedEvents.isEmpty())
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(uncupdatedEvents);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
	public ResponseEntity<?> deleteEvent(@PathVariable Integer id, @RequestParam String deleteType) {
		try {
			Event event = eventService.getEventbyId(id);
			List<Notification> notifications = eventService.getNotificationsOfEvent(id);
			if (deleteType.equalsIgnoreCase("HARD"))
				eventService.hardDeleteEvenet(event);
			else
				eventService.softDeleteEvenet(event);
			notificationManager.deleteNotifications(notifications);
			return ResponseEntity.status(HttpStatus.OK).body(event);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/list")
	public ResponseEntity<?> deleteEventsFromList(@RequestBody List<Integer> ids, @RequestParam String deleteType) {
		// TODO what to do with the notifications
		List<String> notDeleted = new ArrayList<String>();
		List<Event> events = new ArrayList<Event>();
		Event event;
		for (var id : ids) {
			try {
				event = eventService.getEventbyId(id);
				events.add(event);
			} catch (DaoException e) {
				notDeleted.add("event with id " + id + " wasnt deleted: " + e.getMessage());
			}
		}
		try {
			if (deleteType.equalsIgnoreCase("HARD"))
				for (var e : events)
					eventService.hardDeleteEvenet(e);
			else
				for (var e : events)
					eventService.softDeleteEvenet(e);
			if (notDeleted.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(events);
			else
				return ResponseEntity.status(HttpStatus.valueOf(500)).body(notDeleted);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}

	}

}
