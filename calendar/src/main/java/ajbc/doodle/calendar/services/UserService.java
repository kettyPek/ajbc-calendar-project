package ajbc.doodle.calendar.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.User;

@Component()
public class UserService {

	@Autowired
	private UserDao userDao;

	public void addUser(User user) throws DaoException {
		userDao.addUser(user);
	}
	
	public void updateUser(User user) throws DaoException {
		userDao.updateUser(user);
	}

	public User getUserById(Integer userId) throws DaoException {
		return userDao.getUserById(userId);
	}

	public List<User> getAllUsers() throws DaoException {
		return userDao.getAllUsers();
	}

	public User getUserByEmail(String email) throws DaoException {
		return userDao.getUserByEmail(email);
	}

	public void hardDeleteUser(User user) throws DaoException {
		userDao.hardDeleteUser(user);	
	}

	public void softDeleteUser(User user) throws DaoException {
		userDao.softDeleteUser(user);	
	}
	
	
	
}
