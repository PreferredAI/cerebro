package ai.preferred.cerebro.core.jpa.entity;

import java.io.Serializable;

import ai.preferred.cerebro.core.utils.StringUtils;

public class UserModelId implements Serializable {
	private static final long serialVersionUID = 8694739860356797551L;
	
	private String userId;
	private int model;
	
	public UserModelId() {}
	
	public UserModelId(String itemId, int modelId) {
		super();
		this.userId = itemId;
		this.model = modelId;
	}
	
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }

	public int getModel() { return model; }
	public void setModel(int model) { this.model = model; }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof UserModelId) {
			UserModelId id = (UserModelId) obj;
			return StringUtils.areEqual(userId, id.getUserId()) && model == id.getModel();
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return userId.hashCode() + model;
	}
}
