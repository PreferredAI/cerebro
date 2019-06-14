package preferred.ai.cerebro.core.jpa.entitymanager;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import preferred.ai.cerebro.core.jpa.entity.Session;
import preferred.ai.cerebro.core.jpa.util.QueryUtils;
import preferred.ai.cerebro.core.jpa.util.UUIDUtils;
import preferred.ai.cerebro.core.util.DateUtils;

public class SessionManager extends AbstractManager<Session> {
	public SessionManager(EntityManager entityManager) {
		super(entityManager);
	}

	public Session getSession(String sessionId) {
		Query query = entityManager
				.createNamedQuery(Session.GET_SESSION_BY_SESSION_ID);
		query.setParameter("sessionId", sessionId);
		return QueryUtils.getSingleResult(query);
	}

	public List<Session> getAllTimeOutSessionByUserId(String userId){
		Query query = entityManager
				.createNamedQuery(Session.GET_ALL_TIMEOUT_SESSION_BY_USER_ID);
		query.setParameter("userId", userId);
		return QueryUtils.getResultList(query);
	}

	public Session createSession(String userId, String device, String ip, String location) {
		Session session = new Session();
		session.setSessionId(UUIDUtils.newPlainUUIDString());
		session.setUserId(userId);
		session.setDevice(device);
		session.setIp(ip);
		session.setLocation(location);
		session.setLastActivityTime(new Date());
		session.setStatus(Session.STATUS_NORMAL);
		return session;
	}

	public Session validateSession(String sessionId, int timeoutMinutes, String lastRequestAction) {
		Session session = getSession(sessionId);

		if (session == null)
			return null;

		if (session.getLastActivityTime().before(DateUtils.addSeconds(new Date(), 0 - timeoutMinutes*60))) {
			remove(session);
			return null;
		}

		session.setLastActivityTime(new Date());
//		try {
//			merge(session);
//		} catch (Exception ex) {ex.printStackTrace();}
//		
		return session;
	}

	public void cleanSession(Date expireTime) {
		Query query = entityManager
				.createNamedQuery(Session.QUERY_CLEAN_SESSION);
		query.setParameter("expireTime", expireTime, TemporalType.TIMESTAMP);
		executeQuery(query);
	}

}