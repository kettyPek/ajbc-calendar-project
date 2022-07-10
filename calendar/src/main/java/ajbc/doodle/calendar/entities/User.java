package ajbc.doodle.calendar.entities;

import java.time.LocalDate;
import java.util.HashSet;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "Users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;

	private String firstName;
	private String lastName;

	@Column(unique = true)
	private String email;

	private LocalDate birthDate;
	private LocalDate joinDate;
	private boolean inactive;
	private boolean loggedIn;

	@JsonProperty(access = Access.WRITE_ONLY)
	private String endPoint;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String p256dh;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String auth;

	@JsonIgnore
	@ManyToMany(mappedBy = "guests", fetch = FetchType.EAGER)
	private Set<Event> events = new HashSet<Event>();

	public User(String fristName, String lastName, String email, LocalDate birthDate, LocalDate joinDate) {
		this.firstName = fristName;
		this.lastName = lastName;
		this.email = email;
		this.birthDate = birthDate;
		this.joinDate = joinDate;
		this.inactive = false;
		this.loggedIn = false;
	}

}
