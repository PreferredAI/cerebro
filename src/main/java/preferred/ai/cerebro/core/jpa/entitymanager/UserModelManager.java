package preferred.ai.cerebro.core.jpa.entitymanager;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import preferred.ai.cerebro.core.jpa.entity.UserModel;
import preferred.ai.cerebro.core.jpa.util.QueryUtils;

public class UserModelManager extends AbstractManager<UserModel> {

	public UserModelManager(EntityManager entityManager) {
		super(entityManager);
	}

	public List<UserModel> getAllUserModelByModelId(int modelId){
		Query query = entityManager
				.createNamedQuery(UserModel.GET_ALL_USER_MODEL_BY_MODEL_ID);
		query.setParameter("modelId", modelId);
		return QueryUtils.getResultList(query);
	}

	public List<UserModel> getUserModelByUserId(String userId){
		Query query = entityManager
				.createNamedQuery(UserModel.GET_USER_MODEL_BY_USER_ID_AND_MODEL_ID);
		query.setParameter("userId", userId);
		return QueryUtils.getResultList(query);
	}
	
	public UserModel getUserModelByUserIdAndModelId(String userId, int modelId){
		Query query = entityManager
				.createNamedQuery(UserModel.GET_USER_MODEL_BY_USER_ID_AND_MODEL_ID);
		query.setParameter("userId", userId);
		query.setParameter("modelId", modelId);
		return QueryUtils.getSingleResult(query);
	}
	
	public boolean exist(String userId) {
		return !getUserModelByUserId(userId).isEmpty();
	}
	
	public String getRandomUserId() {
		Query query = entityManager
				.createNamedQuery(UserModel.GET_RANDOM_USERID);
		query.setMaxResults(1);
		return QueryUtils.getSingleResult(query);
	}

}