
package ajbc.doodle.calendar.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class UserTest {

	private final Integer USER_ID = null;
	private final String FIRST_NAME = "ketty";
	private final String LAST_NAME = "pekarsky";
	private final String EMAIL = "ketty@gmail.com";
	private final LocalDate BIRTH_DATE = LocalDate.of(1996, 2, 5);
	private final LocalDate JOIN_DATE = LocalDate.now();
	private final boolean INACTIVE = false;
	private final boolean LOGGED_IN = false;
	private final String END_POINT = null;
	private final String P256DH = null;
	private final String AUTH = null;
	private final Set<Event> EVENTS = new HashSet<Event>();

	private User user;

	public UserTest() {
		user = new User(FIRST_NAME, LAST_NAME, EMAIL, BIRTH_DATE);
	}

	@Test
	void ConsttructorTest() {
		assertAll(() -> assertEquals(USER_ID, user.getUserId()), () -> assertEquals(FIRST_NAME, user.getFirstName()),
				() -> assertEquals(LAST_NAME, user.getLastName()), () -> assertEquals(EMAIL, user.getEmail()),
				() -> assertEquals(BIRTH_DATE, user.getBirthDate()), () -> assertEquals(JOIN_DATE, user.getJoinDate()),
				() -> assertEquals(INACTIVE, user.isInactive()), () -> assertEquals(LOGGED_IN, user.isLoggedIn()),
				() -> assertEquals(END_POINT, user.getEndPoint()), () -> assertEquals(P256DH, user.getP256dh()),
				() -> assertEquals(AUTH, user.getAuth()), () -> assertEquals(EVENTS, user.getEvents()));
	}

	@Test
	void testGetUserId() {
		assertEquals(USER_ID, user.getUserId());
	}

	@Test
	void testSetUserId() {
		Integer expected = 1;
		user.setUserId(expected);
		;
		assertEquals(expected, user.getUserId());
	}

	@Test
	void testGetFirstName() {
		assertEquals(FIRST_NAME, user.getFirstName());
	}

	@Test
	void testSetFirstName() {
		String expected = "dani";
		user.setFirstName(expected);
		assertEquals(expected, user.getFirstName());
	}

	@Test
	void testGetLastName() {
		assertEquals(LAST_NAME, user.getLastName());
	}

	@Test
	void testSetLastName() {
		String expected = "kravtzov";
		user.setLastName(expected);
		assertEquals(expected, user.getLastName());
	}

	@Test
	void testGetEmail() {
		assertEquals(EMAIL, user.getEmail());
	}

	@Test
	void testSetEmail() {
		String expected = "dani@gmail.com";
		user.setEmail(expected);
		assertEquals(expected, user.getEmail());
	}

	@Test
	void testGetBirthDate() {
		assertEquals(BIRTH_DATE, user.getBirthDate());
	}

	@Test
	void testSetBirthDate() {
		LocalDate expected = LocalDate.of(1994, 04, 06);
		user.setBirthDate(expected);
		assertEquals(expected, user.getBirthDate());
	}

	@Test
	void testGetJoinDate() {
		assertEquals(JOIN_DATE, user.getJoinDate());
	}

	@Test
	void testSetJoinDate() {
		LocalDate expected = LocalDate.of(2015, 11, 12);
		user.setBirthDate(expected);
		assertEquals(expected, user.getBirthDate());
	}

	@Test
	void testIsInactive() {
		assertEquals(INACTIVE, user.isInactive());
	}

	@Test
	void testSetInactive() {
		boolean expected = true;
		user.setInactive(expected);
		assertEquals(expected, user.isInactive());
	}

	@Test
	void testIsLoggedIn() {
		assertEquals(LOGGED_IN, user.isLoggedIn());
	}

	@Test
	void testSetLoggedIn() {
		boolean expected = true;
		user.setLoggedIn(expected);
		assertEquals(expected, user.isLoggedIn());
	}

	@Test
	void testGetEndPoint() {
		assertEquals(END_POINT, user.getEndPoint());
	}

	@Test
	void testSetEndPoint() {
		String expected = "/test";
		user.setEndPoint(expected);
		assertEquals(expected, user.getEndPoint());
	}

	@Test
	void testGetP256dh() {
		assertEquals(P256DH, user.getP256dh());
	}

	@Test
	void testSetP256dh() {
		String expected = "/test";
		user.setP256dh(expected);
		assertEquals(expected, user.getP256dh());
	}

	@Test
	void testGetAuth() {
		assertEquals(AUTH, user.getAuth());
	}

	@Test
	void testSetAuth() {
		String expected = "/test";
		user.setAuth(expected);
		assertEquals(expected, user.getAuth());
	}

}
