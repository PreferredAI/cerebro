package ai.preferred.cerebro.core.jpa.entitymanager;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ai.preferred.cerebro.core.jpa.entity.Feedback;
import ai.preferred.cerebro.core.jpa.utils.QueryUtils;

public class FeedbackManager extends AbstractManager<Feedback> {

	public FeedbackManager(EntityManager entityManager) {
		super(entityManager);
	}

	public Feedback getFeedbacklById(String id){
		Query query = entityManager .createNamedQuery(Feedback.GET_FEEDBACK_BY_ID);
		query.setParameter("id", id);
		return QueryUtils.getSingleResult(query);
	}

	public List<Feedback> getAllFeedbackByType(int type){
		Query query = entityManager .createNamedQuery(Feedback.GET_ALL_FEEDBACK_BY_TYPE);
		query.setParameter("type", type);
		return QueryUtils.getResultList(query);
	}
	
	public List<Feedback> getAllFeedbackBySessionIdAndType(String sessionId, int type){
		Query query = entityManager .createNamedQuery(Feedback.GET_ALL_FEEDBACK_BY_SESSION_ID_AND_TYPE);
		query.setParameter("sessionId", sessionId);
		query.setParameter("type", type);
		return QueryUtils.getResultList(query);
	}
	
	public List<Feedback> getAllFeedbackBySessionId(String sessionId){
		Query query = entityManager .createNamedQuery(Feedback.GET_ALL_FEEDBACK_BY_SESSION_ID);
		query.setParameter("sessionId", sessionId);
		return QueryUtils.getResultList(query);
	}
	
	public List<Feedback> getAllFeedbackBySessionIdAndStatus(String sessionId, int status){
		Query query = entityManager .createNamedQuery(Feedback.GET_ALL_FEEDBACK_BY_SESSION_ID_AND_STATUS);
		query.setParameter("sessionId", sessionId);
		query.setParameter("status", status);
		return QueryUtils.getResultList(query);
	}
	
	public void updateFeedbackStatusById(String id, int status) {
		Query query = entityManager .createNamedQuery(Feedback.UPDATE_FEEDBACK_STATUS_BY_ID);
		query.setParameter("id", id);
		query.setParameter("status", status);
		executeQuery(query);
	}

}