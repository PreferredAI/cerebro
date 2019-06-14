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

import ai.preferred.cerebro.core.entity.Parameter;
import ai.preferred.cerebro.core.util.StringUtils;

@NamedQueries({
	@NamedQuery(name = Feedback.GET_FEEDBACK_BY_ID, query = "SELECT f FROM Feedback f "
			+ "WHERE f.id = :id"),
	@NamedQuery(name = Feedback.GET_ALL_FEEDBACK_BY_TYPE, query = "SELECT f FROM Feedback f "
			+ "WHERE f.type = :type"),
	@NamedQuery(name = Feedback.GET_ALL_FEEDBACK_BY_SESSION_ID_AND_TYPE, query = "SELECT f FROM Feedback f "
			+ "WHERE f.sessionId = :sessionId "
			+ "AND f.type = :type"),
	@NamedQuery(name = Feedback.GET_ALL_FEEDBACK_BY_SESSION_ID, query = "SELECT f FROM Feedback f "
			+ "WHERE f.sessionId = :sessionId"),
	@NamedQuery(name = Feedback.GET_ALL_FEEDBACK_BY_SESSION_ID_AND_STATUS, query = "SELECT f FROM Feedback f "
			+ "WHERE f.sessionId = :sessionId "
			+ "AND f.status = :status"),
	@NamedQuery(name = Feedback.UPDATE_FEEDBACK_STATUS_BY_ID, query = "UPDATE Feedback f "
			+ "SET f.status = :status "
			+ "WHERE f.id = :id")
	})

@Entity
@Table(name = "tbl_feedback")
public class Feedback implements Serializable{
	private static final long serialVersionUID = 4315689097886225189L;
	
	public static final String GET_FEEDBACK_BY_ID = "GetFeedbackById";
	public static final String GET_ALL_FEEDBACK_BY_TYPE = "GetAllFeedbackByType";
	public static final String GET_ALL_FEEDBACK_BY_SESSION_ID_AND_TYPE = "GetAllFeedbackBySessionIdAndType";
	public static final String GET_ALL_FEEDBACK_BY_SESSION_ID = "GetAllFeedbackBySessionId";
	public static final String GET_ALL_FEEDBACK_BY_SESSION_ID_AND_STATUS = "GetAllFeedbackBySessionIdAndStatus";
	public static final String UPDATE_FEEDBACK_STATUS_BY_ID = "UpdateFeedbackStatusById";

	public static final int TYPE_CLICK = 0;
	public static final int TYPE_FAVORITE = 1;
	public static final int TYPE_RATE = 2;
	public static final int TYPE_SEARCH = 3;
	public static final int TYPE_ADD_TO_CART = 4;
	public static final int TYPE_PURCHASE = 5;
	
	public static final int TYPE_SELECT_BRAND=6;
	public static final int TYPE_SELECT_PRODUCT=7;
	public static final int TYPE_VIEW_HOMEPAGE=8;
	
	public static final int STATUS_CHECKED = 0;
	public static final int STATUS_UNCHECKED = 1;
	
	@Id
	@Column(name = "FeedbackId", nullable = false, length = 128)
	private String id;

	@Column(name = "SessionId", nullable = false, length = 128)
	private String sessionId;

	@Column(name = "Type", nullable = false, length = 11)
	private int type;
	
	@Column(name = "Object", nullable = false)
	private String object;

	@Column(name = "Metadata", nullable=true)
	private String metadata;

	@Column(name = "CreatedTime", nullable = true)
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "Status", nullable = false, length = 11)
	private int status;
	
	public Feedback(){}
	
	public Feedback(String id, String sessionId, int type, String object, String metadata) {
		this(id, sessionId, type, object, metadata, new Date(), STATUS_UNCHECKED);
	}

	public Feedback(String id, String sessionId, int type, String object, String metadata, Date createdTime, int status) {
		super();
		this.id = id; 
		this.sessionId = sessionId;
		this.type = type;
		this.object = object;
		this.metadata = metadata;
		this.createdTime = createdTime;
		this.status = status;
	}
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getSessionId() { return sessionId; }
	public void setSessionId(String sessionId) { this.sessionId = sessionId; }

	public int getType() { return type; }
	public void setType(int type) { this.type = type; }
	
	public String getObject() { return object;}
	public void setObject(String object) { this.object = object; }

	public String getMetadata() { return metadata; }
	public void setMetadata(String metadata) { this.metadata = metadata; }

	public Date getCreatedTime() { return createdTime; }
	public void setCreatedTime(Date createdTime) { this.createdTime = createdTime;}

	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }
	
	public Parameter getMetadataAsParams(){
		Parameter params = new Parameter();
		String[] attrs = StringUtils.explode(this.metadata, ',');
		for(String attr: attrs){
			String[] parts = StringUtils.explode(attr, "[:]");
			params.add(parts[0], parts[1]);
		}
		return params;
	}
}
