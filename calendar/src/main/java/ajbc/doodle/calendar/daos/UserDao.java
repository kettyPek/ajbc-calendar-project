package ajbc.doodle.calendar.daos;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ajbc.doodle.calendar.entities.User;

@Transactional(rollbackFor = { DaoException.class }, readOnly = true)
public interface UserDao {
	
	// CRUD
	@Transactional(readOnly = false)
	public default void addUser(User user) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	@Transactional(readOnly = false)
	public default void updateUser(User user) throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	// Queries
	public default List<User> getAllUsers() throws DaoException {
		throw new DaoException("Method not implemented");
	}
	
	public default User getUserById(int id) throws DaoException{
		throw new DaoException("Method not implemented");
	}
	
	public User getUserByEmail(String email) throws DaoException;

	@Transactional(readOnly = false)
	public void hardDeleteUser(User user) throws DaoException;

	@Transactional(readOnly = false)
	public void softDeleteUser(User user) throws DaoException;
	
}
