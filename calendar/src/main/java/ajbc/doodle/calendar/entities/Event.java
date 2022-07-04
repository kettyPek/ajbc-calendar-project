package ajbc.doodle.calendar.entities;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
	
	private Integer ownerId;
	private String title;
	private boolean isAllDay;
	private LocalDateTime startDateTime;
	private LocalDateTime endDateTime;
	private String Address;
	private String description;
	private RepeatOptions repeatOptions;
	private Integer inactive;
	
	@ManyToMany(mappedBy = "users_events")
	private List<User> guestsIds;

}
