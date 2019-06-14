package ai.preferred.cerebro.core.jpa.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@NamedQueries({
	@NamedQuery(name = UserModel.GET_ALL_USER_MODEL_BY_MODEL_ID, query = "SELECT um FROM UserModel um "
			+ "WHERE um.model.modelId = :modelId"),
	@NamedQuery(name = UserModel.GET_USER_MODEL_BY_USER_ID, query = "SELECT um FROM UserModel um "
			+ "WHERE um.userId = :userId"),
	@NamedQuery(name = UserModel.GET_USER_MODEL_BY_USER_ID_AND_MODEL_ID, query = "SELECT um FROM UserModel um "
			+ "WHERE um.userId = :userId "
			+ "AND um.model.modelId = :modelId"),
	@NamedQuery(name = UserModel.GET_RANDOM_USERID, query = "SELECT um.userId FROM UserModel um "
			+ "ORDER BY function('RAND')")})

@Entity
@Table(name = "tbl_usermodel")
@IdClass(UserModelId.class)
public class UserModel implements Serializable {
	private static final long serialVersionUID = -2031908364586239883L;

	public static final String GET_ALL_USER_MODEL_BY_MODEL_ID = "GetAllUserModelByModelId";
	public static final String GET_USER_MODEL_BY_USER_ID = "GetUserModelByUserId";
	public static final String GET_USER_MODEL_BY_USER_ID_AND_MODEL_ID = "GetUserModelByUserIdAndModelId";
	public static final String GET_RANDOM_USERID = "GetRandomUserId";

	@Id
	@Column(name = "UserId", nullable = false)
	private String userId;

	@Id
	@ManyToOne(targetEntity = Model.class)
	@JoinColumn(name = "ModelId", referencedColumnName = "ModelId",  nullable = false)
	private Model model;

	@Column(name = "Representation", nullable = false)
	private String representation;

	@Column(name = "UpdatedTime", nullable = true)
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date updatedTime;

	@Column(name = "Status", nullable = false, length = 11)
	private int status;

	public UserModel(){}

	public UserModel(Model model, String userId, String representation, Date updatedTime, int status) {
		super();
		this.model = model;
		this.userId = userId;
		this.representation = representation;
		this.updatedTime = updatedTime;
		this.status = status;
	}

	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }

	public Model getModel() { return model; }
	public void setModel(Model model) { this.model = model; }

	public String getRepresentation() { return representation; }
	public void setRepresentation(String representation) { this.representation = representation; }

	public Date getUpdatedTime() { return updatedTime; }
	public void setUpdatedTime(Date updatedTime) { this.updatedTime = updatedTime; }

	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }
}