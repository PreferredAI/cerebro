package ai.preferred.cerebro.core.jpa.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@NamedQueries({
	@NamedQuery(name = Session.GET_SESSION_BY_SESSION_ID, query = "SELECT s FROM Session s "
			+ "WHERE s.sessionId = :sessionId"),
	@NamedQuery(name = Session.GET_ALL_TIMEOUT_SESSION_BY_USER_ID, query = "SELECT s FROM Session s "
			+ "WHERE s.userId = :userId "
			+ "AND s.status = 1"),
	@NamedQuery(name = Session.QUERY_UPDATE_SESSION, query = "UPDATE Session s "
			+ "SET s.lastActivityTime = :lastActivityTime "
			+ "WHERE s.sessionId = :sessionId"),
	@NamedQuery(name = Session.QUERY_CLEAN_SESSION, query = "UPDATE Session s "
			+ "SET s.status=" + Session.STATUS_TIMEOUT + " "
			+ "WHERE s.lastActivityTime < :expireTime") })
@Entity
@Table(name = "tbl_session")
public class Session implements Serializable {
	private static final long serialVersionUID = -4845224462796531712L;
	public static final String GET_SESSION_BY_SESSION_ID = "GetSessionBySessionId";
	public static final String GET_ALL_TIMEOUT_SESSION_BY_USER_ID = "GetAllTimeOutSessionByUserId";
	public static final String QUERY_UPDATE_SESSION = "updateSession";
	public static final String QUERY_CLEAN_SESSION = "cleanSession";
	
	public static final int STATUS_NORMAL = 0;
	public static final int STATUS_TIMEOUT = 1;

	@Id
	@Column(name = "SessionId", nullable = false, length = 100)
	private String sessionId;

	@Column(name = "UserId", nullable = false)
	private String userId;

	@Column(name = "ClientId", nullable = true, length = 255)
	private String clientId;

	@Column(name = "Ip", nullable = true, length = 255)
	private String ip;

	@Column(name = "Location", nullable = true, length = 255)
	private String location;

	@Column(name = "Device", nullable = true, length = 255)
	private String device;

	@Version
	@Column(name = "Version", nullable = false, length = 11)
	private int version;

	@Column(name = "LastActivityTime", nullable = true)
	@Temporal(value = TemporalType.TIMESTAMP)
	private java.util.Date lastActivityTime;

	@Column(name = "Status", nullable = false, length = 11)
	private int status;
	
	public Session(){}

	public Session(String sessionId, String userId, String clientId, String ip, String location, String device,
			Date lastActivityTime, int status) {
		super();
		this.sessionId = sessionId;
		this.userId = userId;
		this.clientId = clientId;
		this.ip = ip;
		this.location = location;
		this.device = device;
		this.lastActivityTime = lastActivityTime;
		this.version = 1;
		this.status = status;
	}

	public void setSessionId(String value) { this.sessionId = value; }
	public String getSessionId() { return sessionId; }

	public void setUserId(String value) { this.userId = value; }
	public String getUserId() { return userId; }

	public void setClientId(String value) { this.clientId = value; }
	public String getClientId() { return clientId; }

	public void setIp(String value) { this.ip = value; }
	public String getIp() { return ip; }

	public void setLocation(String value) { this.location = value; }
	public String getLocation() { return location; }

	public void setDevice(String value) { this.device = value; }
	public String getDevice() { return device; }


	public void setLastActivityTime(Date value) { this.lastActivityTime = value; }
	public Date getLastActivityTime() { return lastActivityTime; }
	
	public int getVersion() { return version; }
	public void setVersion(int version) { this.version = version; }

	public void setStatus(int value) { this.status = value; }
	public int getStatus() { return status; }
	
	
}