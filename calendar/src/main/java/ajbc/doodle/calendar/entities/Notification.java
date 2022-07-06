package ajbc.doodle.calendar.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ajbc.doodle.calendar.enums.Units;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString

@Entity
@Table(name = "Notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer notificationId;

	@ManyToOne
    @JoinColumn(name="eventId")
	private Event event;
	
	private String title;

	@Enumerated(EnumType.STRING)
	private Units units;

	private Integer qiantity;

	public Notification(Event event, String title, Units units, Integer qiantity) {
		this.event = event;
		this.title = title;
		this.units = units;
		this.qiantity = qiantity;
	}

}
