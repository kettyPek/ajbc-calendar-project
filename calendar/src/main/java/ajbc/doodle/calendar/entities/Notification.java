package ajbc.doodle.calendar.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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

	private Integer eventId;
	private String title;

	@Enumerated(EnumType.STRING)
	private Units units;

	private Integer qiantity;

	public Notification(Integer eventId, String title, Units units, Integer qiantity) {
		this.eventId = eventId;
		this.title = title;
		this.units = units;
		this.qiantity = qiantity;
	}

}
