package ai.preferred.cerebro.core.jpa.entitymanager;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ai.preferred.cerebro.core.jpa.entity.UserProfile;
import ai.preferred.cerebro.core.jpa.util.QueryUtils;

public class UserProfileManager extends AbstractManager<UserProfile> {

	public UserProfileManager(EntityManager entityManager) {
		super(entityManager);
	}
	
	public List<UserProfile> getAllUserProfiles(){
		Query query = entityManager.createNamedQuery(UserProfile.QUERY_GET_ALL_USER_PROFILES);
		return QueryUtils.getResultList(query);
	}
	
	public UserProfile getUserProfileById(String userId){
		Query query = entityManager
				.createNamedQuery(UserProfile.QUERY_GET_USER_PROFILE_BY_USER_ID);
		query.setParameter("userId", userId);
		return QueryUtils.getSingleResult(query);
	}
	
	public UserProfile getUserProfileByUserName(String userName){
		Query query = entityManager
				.createNamedQuery(UserProfile.QUERY_GET_USER_PROFILE_BY_USERNAME);
		query.setParameter("username", userName);
		return QueryUtils.getSingleResult(query);
	}
	
	public UserProfile getUserProfileByEmail(String email){
		Query query = entityManager
				.createNamedQuery(UserProfile.QUERY_GET_USER_PROFILE_BY_EMAIL);
		query.setParameter("email", email);
		return QueryUtils.getSingleResult(query);
	}
	
	public UserProfile getUserProfileByPhone(String phone){
		Query query = entityManager
				.createNamedQuery(UserProfile.QUERY_GET_USER_PROFILE_BY_PHONE);
		query.setParameter("phone", phone);
		return QueryUtils.getSingleResult(query);
	}
	
	public boolean isExisted(String userId){
		return (getUserProfileById(userId) != null);
	}
	
	public boolean isActivated(String userId) {
		UserProfile u = getUserProfileById(userId);
		if(u == null) return false;
		return u.getActivation() == UserProfile.USER_ACTIVATED;
	}
}