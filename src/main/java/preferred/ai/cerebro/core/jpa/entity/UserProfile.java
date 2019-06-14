package preferred.ai.cerebro.core.jpa.entity;

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

@NamedQueries({
	@NamedQuery(name = UserProfile.QUERY_GET_ALL_USER_PROFILES, query = "SELECT u FROM UserProfile u" + " WHERE u.status=0"),
	@NamedQuery(name = UserProfile.QUERY_GET_USER_PROFILE_BY_USERNAME, query = "SELECT u" + " FROM UserProfile u" + " WHERE u.username=:username"),
	@NamedQuery(name = UserProfile.QUERY_GET_USER_PROFILE_BY_USER_ID, query = "SELECT u" + " FROM UserProfile u" + " WHERE u.userId=:userId"),
	@NamedQuery(name = UserProfile.QUERY_GET_USER_PROFILE_BY_EMAIL, query = "SELECT u" + " FROM UserProfile u" + " WHERE u.email=:email"),
	@NamedQuery(name = UserProfile.QUERY_GET_USER_PROFILE_BY_PHONE, query = "SELECT u" + " FROM UserProfile u" + " WHERE u.phone=:phone")
})

@Entity
@Table(name = "tbl_userprofile")
public class UserProfile implements Serializable {
	private static final long serialVersionUID = -9154967562173683888L;

	public static final String QUERY_GET_ALL_USER_PROFILES = "GetAllUserProfiles";
	public static final String QUERY_GET_USER_PROFILE_BY_USERNAME = "GetUserProfileByUsername";
	public static final String QUERY_GET_USER_PROFILE_BY_USER_ID = "GetUserProfileByProfileId";
	public static final String QUERY_GET_USER_PROFILE_BY_EMAIL = "GetUserProfileByEmail";
	public static final String QUERY_GET_USER_PROFILE_BY_PHONE = "GetUserProfileByPhone";

	public static final int USER_NOT_ACTIVATED = 0;
	public static final int USER_ACTIVATED = 1;
	
	public static final int USER_STATUS_NORMAL = 0;
	public static final int USER_STATUS_NEED_UPDATE = 1;

	public static final int USER_TYPE_ADMINISTATOR = 1;
	public static final int USER_TYPE_NORMAL = 0;
	
	public static final int USER_GENDER_FEMALE = 0;
	public static final int USER_GENDER_MALE = 1;

	@Id
	@Column(name = "UserId", nullable = false, length = 255)
	private String userId;

	@Column(name = "Username", nullable = false, length = 128)
	private String username;

	@Column(name = "Password", nullable = false, length = 255)
	private String password;

	@Column(name = "Name", nullable = true, length = 255)
	private String name;

	@Column(name = "Gender", nullable = true, length = 11)
	private int gender;

	@Column(name = "Email", nullable = true, length = 255)
	private String email;

	@Column(name = "Phone", nullable = true, length = 32)
	private String phone;

	@Column(name = "Photo")
	private String photo;
	
	@Column(name = "Language", nullable = true, length = 32)
	private String language;

	@Column(name = "UserType", nullable = false, length = 11)
	private int userType;

	@Column(name = "Activation", nullable = false, length = 11)
	private int activation;

	@Column(name = "LastActiveTime", nullable = true)
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date lastActiveTime;

	@Column(name = "Status", nullable = false, length = 11)
	private int status;

	public UserProfile() {}

	public UserProfile(String userId, String username, String password, String name, int gender, String email, String phone, String photo,
			String language, int userType, int activation, Date lastActiveTime, int status) {
		super();
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.name = name;
		this.gender = gender;
		this.email = email;
		this.phone = phone;
		this.photo = photo;
		this.language = language;
		this.userType = userType;
		this.activation = activation;
		this.lastActiveTime = lastActiveTime;
		this.status = status;
	}

	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public int getUserType() { return userType; }
	public void setUserType(int userType) { this.userType = userType; }

	public int getGender() { return gender; }
	public void setGender(int gender) { this.gender = gender; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	
	public String getPhoto() { return photo; }
	public void setPhoto(String photo) { this.photo = photo; }

	public String getLanguage() { return language; }
	public void setLanguage(String language) { this.language = language; }

	public int getActivation() { return activation; }
	public void setActivation(int activation) { this.activation = activation; }

	public Date getLastActiveTime() { return lastActiveTime; }
	public void setLastActiveTime(Date lastActiveTime) { this.lastActiveTime = lastActiveTime; }

	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }

	public String toString() {
		return String.valueOf(getUserId());
	}
}