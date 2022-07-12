package ajbc.doodle.calendar.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Component;

import ajbc.doodle.calendar.entities.User;

@SuppressWarnings("unchecked")
@Component(value = "userHT")
public class UserHibernateTemplate implements UserDao{

	@Autowired
	private HibernateTemplate template;

	// CRUD
	@Override
	public void addUser(User user) throws DaoException {
		if(emailExistInDb(user))
			throw new DaoException("Email already exists");
		template.persist(user);
	}
	
	@Override
	public void updateUser(User user) throws DaoException {
		if(!userExistInDb(user.getUserId()))
			throw new DaoException("No such user in DB");
		if(emailExistInDb(user))
			throw new DaoException("Email already exists");
		template.merge(user);
	}

	// Queries
	@Override
	public User getUserById(int userId) throws DaoException {
		User user = template.get(User.class, userId);
		if (user == null)
			throw new DaoException("No such user in DB");
		return user;
	}

	@Override
	public List<User> getAllUsers() throws DaoException {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		return (List<User>) template.findByCriteria(criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
	}

	@Override
	public User getUserByEmail(String email) throws DaoException  {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		Criterion criterion = Restrictions.eq("email", email);
		criteria.add(criterion);
		List<User> users = (List<User>)template.findByCriteria(criteria);
		if(users == null || users.size() == 0)
			throw new DaoException("No such user in DB");
		return users.get(0);
	}

	@Override
	public void hardDeleteUser(User user) throws DaoException {
		// TODO hard delete doesn't work
		template.delete(user);
	}

	@Override
	public void softDeleteUser(User user) throws DaoException {
		user.setInactive(true);
		updateUser(user);	
	}
	
	private boolean emailExistInDb(User user) throws DaoException {
		List<User> users = getAllUsers();
		for(User u : users) {
			if(u.getEmail().equals(user.getEmail()) && u.getUserId() != user.getUserId())
				return true;
		}
		return false;
	}
	
	private boolean userExistInDb(Integer userId) {
		User user = template.get(User.class, userId);
		if (user == null)
			return false;
		return true;
	}
	
}
