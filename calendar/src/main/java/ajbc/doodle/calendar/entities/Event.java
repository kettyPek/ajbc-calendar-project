package ajbc.doodle.calendar.entities;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import ajbc.doodle.calendar.enums.RepeatOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "Events")
public class Event {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer eventId;
	
	@Column(updatable = false)
	private Integer ownerId;
	
	private String title;
	private boolean allDay;
	private LocalDateTime startDateTime;
	private LocalDateTime endDateTime;
	private String Address;
	private String description;
	
	@Enumerated(EnumType.STRING)
	private RepeatOptions Repeating; 
	
	private boolean inactive;
	
	@ManyToMany(mappedBy="eventIds",cascade = {CascadeType.MERGE})
	private List<User> guestsIds;
	
	@OneToMany(cascade = {CascadeType.MERGE})
	@JoinColumn(name = "notificationId")
	private List<Notification> notifications;

	public Event(Integer ownerId, String title, boolean isAllDay, LocalDateTime startDateTime,
			LocalDateTime endDateTime, String address, String description, RepeatOptions repeatOptions,
			boolean inactive, List<User> guestsIds) {
		this.ownerId = ownerId;
		this.title = title;
		this.allDay = isAllDay;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		Address = address;
		this.description = description;
		this.Repeating = repeatOptions;
		this.inactive = inactive;
		this.guestsIds = guestsIds;
	}
	
	

}
