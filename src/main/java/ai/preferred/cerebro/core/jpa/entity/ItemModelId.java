package ai.preferred.cerebro.core.jpa.entity;

import java.io.Serializable;

import ai.preferred.cerebro.core.utils.StringUtils;

public class ItemModelId implements Serializable{
	private static final long serialVersionUID = 1565499982993981436L;
	
	private String itemId;
	private int model;
	
	public ItemModelId() {}
	
	public ItemModelId(String itemId, int modelId) {
		super();
		this.itemId = itemId;
		this.model = modelId;
	}
	
	public String getItemId() { return itemId; }
	public void setItemId(String itemId) { this.itemId = itemId; }

	public int getModel() { return model; }
	public void setModel(int model) { this.model = model; }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ItemModelId) {
			ItemModelId id = (ItemModelId) obj;
			return StringUtils.areEqual(itemId, id.getItemId()) && model == id.getModel();
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return itemId.hashCode() + model;
	}
}
