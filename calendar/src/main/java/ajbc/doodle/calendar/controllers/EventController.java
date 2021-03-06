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
import ajbc.doodle.calendar.services.NotificationServcie;

/**
 * Handles Event API requests
 * 
 * @author ketty
 *
 */
@RequestMapping("/event")
@RestController
public class EventController {

	@Autowired
	private EventService eventService;

	@Autowired
	private NotificationManager notificationManager;

	/**
	 * Create new event end insert it to database
	 * 
	 * @param event - event to create
	 * @return created event
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createEvent(@RequestBody Event event) {
		try {
			if (!eventService.isUserExists(event.getOwnerId()))
				throw new DaoException("User " + event.getOwnerId() + " does not exist in DB");
			eventService.addEvent(event);
			event = eventService.getEventbyId(event.getEventId());
			Notification defaultNotification = eventService.addDefaultNotificationFOrEvent(event);
			notificationManager.addNotification(defaultNotification);
			return ResponseEntity.status(HttpStatus.CREATED).body(event);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	/**
	 * Create events from list of events
	 * 
	 * @param events - list of events
	 * @return created events, if some events were not created, returns exceptions
	 *         details
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/list")
	public ResponseEntity<?> createEventsFromList(@RequestBody List<Event> events) {
		try {
			Event event;
			List<String> uncratedEvents = new ArrayList<String>();
			List<Notification> defaultNotifications = new ArrayList<Notification>();
			for (int i = 0; i < events.size(); i++) {
				try {
					if (!eventService.isUserExists(events.get(i).getOwnerId()))
						throw new DaoException("User " + events.get(i).getOwnerId() + " does not exist in DB");
					eventService.addEvent(events.get(i));
					event = eventService.getEventbyId(events.get(i).getEventId());
					defaultNotifications.add(eventService.addDefaultNotificationFOrEvent(event));
				} catch (DaoException e) {
					uncratedEvents.add("event " + i + " in the list wasnt created: " + e.getMessage());
				}
			}
			notificationManager.addNotificationsFromList(defaultNotifications);
			if (!uncratedEvents.isEmpty())
				return ResponseEntity.status(HttpStatus.valueOf(500)).body(uncratedEvents);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	/**
	 * Get events from database by parameters
	 * 
	 * @param paramsMap - map of parameters , key - parameter name, value -
	 *                  parameter value
	 * @return list of events. startDateTime and endDateTime : events between given
	 *         dates. no parameters : all events in database.
	 * @throws DaoException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllEvents(@RequestParam Map<String, String> paramsMap) {
		try {
			List<Event> events;
			if (paramsMap.containsKey("startDateTime") && paramsMap.containsKey("endDateTime"))
				events = eventService.getEventsBetween(LocalDateTime.parse(paramsMap.get("startDateTime")),
						LocalDateTime.parse(paramsMap.get("endDateTime")));
			else
				events = eventService.getAllEvents();
			return ResponseEntity.ok(events);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}

	}

	/**
	 * Get all events of user by parameters
	 * 
	 * @param id        - user's id
	 * @param paramsMap - map of parameters , key - parameter name, value -
	 *                  parameter value startDateTime and endDateTime : events
	 *                  between given dates. minutes and hours : events which take
	 *                  place given hours and minutes before current time no
	 *                  parameters : all events in if user.
	 * @return - list of events
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/user/{id}")
	public ResponseEntity<?> getAllEventsOfUserById(@PathVariable Integer id,
			@RequestParam Map<String, String> paramsMap) {
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

	/**
	 * Get upcoming events of user
	 * 
	 * @param id - user's id
	 * @return list of events
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/user/{id}/upcoming")
	public ResponseEntity<?> getUpcomingEventsOfUser(@PathVariable Integer id) {
		try {
			if (!eventService.isUserExists(id))
				throw new DaoException("User " + id + " does not exist in DB");
			List<Event> events = eventService.getUpcomingEventsOfUser(id);
			return ResponseEntity.ok(events);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}

	}

	/**
	 * Update event
	 * 
	 * @param event   - event with updated data
	 * @param eventId - event's id
	 * @param ownerId - owner's id
	 * @return updated event if action succeeded, otherwise returns exception
	 *         details
	 */
	@RequestMapping(method = RequestMethod.PUT, path = "/{eventId}/owner/{ownerId}")
	public ResponseEntity<?> updateEvent(@RequestBody Event event, @PathVariable Integer eventId,
			@PathVariable Integer ownerId) {
		try {
			if (!eventService.userIsOwner(eventId, ownerId))
				throw new DaoException("Only the owner can update the event");

			Event oldEvent = eventService.getEventbyId(eventId);
			event.setEventId(eventId);
			eventService.updateEvent(event, ownerId);
			event = eventService.getEventbyId(eventId);
			if (!oldEvent.getStartDateTime().isEqual(event.getStartDateTime())) {
				notificationManager.updatedNotificationsFromList(eventService.updatedNotificatiosOfEvent(event));
			}
			return ResponseEntity.status(HttpStatus.OK).body(event);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	/**
	 * Update events from list of events
	 * 
	 * @param eventsMap - map of parameters , key - event's id, value - event
	 * @return list of updated events if action succeeded, otherwise returns
	 *         unsuccessful updates and exception details
	 */
	@RequestMapping(method = RequestMethod.PUT, path = "/list")
	public ResponseEntity<?> updateEventsFromList(@RequestBody Map<Integer, Event> eventsMap) {
		Event oldEvent;
		List<String> uncupdatedEvents = new ArrayList<String>();
		List<Integer> ids = eventsMap.keySet().stream().collect(Collectors.toList());
		List<Notification> notifications = new ArrayList<Notification>();
		for (var id : ids) {
			try {
				oldEvent = eventService.getEventbyId(id);
				eventsMap.get(id).setEventId(id);
				eventService.updateEvent(eventsMap.get(id), eventsMap.get(id).getOwnerId());
				if (!oldEvent.getStartDateTime().isEqual(eventsMap.get(id).getStartDateTime())) {
					notifications.addAll(eventService.updatedNotificatiosOfEvent(eventsMap.get(id)));
				}
			} catch (DaoException e) {
				uncupdatedEvents.add("event with id " + id + " wasnt updated: " + e.getMessage());
			}
		}
		try {
			notificationManager.updatedNotificationsFromList(notifications);
			if (!uncupdatedEvents.isEmpty())
				return ResponseEntity.status(HttpStatus.valueOf(500)).body(uncupdatedEvents);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}

	}

	/**
	 * Delete event
	 * 
	 * @param id         - event's id
	 * @param deleteType - SOFT: deactivate event. HARD: hard delete from database
	 * @return delete event if action succeeded, otherwise returns exception details
	 */
	@RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
	public ResponseEntity<?> deleteEvent(@PathVariable Integer id, @RequestParam String deleteType) {
		try {
			Event event = eventService.getEventbyId(id);

			// delete notifications in NotificationManager
			List<Notification> notifications = eventService.getNotificationsOfEvent(id);
			notificationManager.deleteNotifications(notifications);

			if (deleteType.equalsIgnoreCase("HARD"))
				eventService.hardDeleteEvenet(event);
			else
				eventService.softDeleteEvenet(event);
			return ResponseEntity.status(HttpStatus.OK).body(event);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}

	/**
	 * Delete list of events
	 * 
	 * @param ids        - id's of events to be deleted
	 * @param deleteType - SOFT: deactivate event. HARD: hard delete from database
	 * @return delete events if action succeeded, otherwise returns exception
	 *         details
	 */
	@RequestMapping(method = RequestMethod.DELETE, path = "/list")
	public ResponseEntity<?> deleteEventsFromList(@RequestBody List<Integer> ids, @RequestParam String deleteType) {
		List<String> notDeleted = new ArrayList<String>();
		List<Event> events = new ArrayList<Event>();
		List<Notification> notifications = new ArrayList<Notification>();
		Event event;
		for (var id : ids) {
			try {
				event = eventService.getEventbyId(id);
				events.add(event);
				notifications.addAll(eventService.getNotificationsOfEvent(id));
			} catch (DaoException e) {
				notDeleted.add("event with id " + id + " wasnt deleted: " + e.getMessage());
			}
		}
		try {
			notificationManager.deleteNotifications(notifications);
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
