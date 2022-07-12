package ajbc.doodle.calendar.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

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

	@JsonProperty(access = Access.READ_ONLY)
	@Column(insertable = false, updatable = false)
	private Integer ownerId;

	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REMOVE)
	@JoinColumn(name = "ownerId")
	private User owner;

	private String title;
	private boolean allDay;
	private LocalDateTime startDateTime;
	private LocalDateTime endDateTime;
	private String Address;
	private String description;

	@Enumerated(EnumType.STRING)
	private RepeatOptions Repeating;

	private boolean inactive;

	@JsonIgnore
	@ManyToMany(cascade = { CascadeType.MERGE},fetch = FetchType.EAGER)
	@JoinTable(name = "Users_Events", joinColumns = @JoinColumn(name = "eventId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	private Set<User> guests = new HashSet<User>();
	
	@Transient
	private List<String> guestsEmails;

	@JsonProperty(access = Access.READ_ONLY)
	@OneToMany(mappedBy = "event", cascade = { CascadeType.MERGE } , fetch = FetchType.EAGER)
	private Set<Notification> notifications = new HashSet<Notification>();

	public Event(User owner, String title, boolean isAllDay, LocalDateTime startDateTime, LocalDateTime endDateTime,
			String address, String description, RepeatOptions repeatOptions, boolean inactive, Set<User> guests) {
		this.owner = owner;
		this.title = title;
		this.allDay = isAllDay;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		Address = address;
		this.description = description;
		this.Repeating = repeatOptions;
		this.inactive = inactive;
		this.guests = guests;
	}

	@PostPersist
	@PostUpdate
	@PostLoad
	private void updateGuestsEmails() {
		guestsEmails = guests.stream().map(User::getEmail).collect(Collectors.toList());

	}

}
