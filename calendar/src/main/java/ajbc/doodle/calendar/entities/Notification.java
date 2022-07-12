package ajbc.doodle.calendar.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ajbc.doodle.calendar.enums.Units;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString

@Entity
@Table(name = "Notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer notificationId;

	@Column(insertable = false, updatable = false)
	private Integer eventId;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "eventId")
	private Event event;

	@Column(insertable = false, updatable = false)
	private Integer userId;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "userId")
	private User user;

	private String title;

	@Enumerated(EnumType.STRING)
	private Units units;

	private Integer quantity;
	private boolean inactive;

	public Notification(Event event, User user, String title, Units units, Integer qiantity) {
		this.event = event;
		this.user = user;
		this.title = title;
		this.units = units;
		this.quantity = qiantity;
	}

}
