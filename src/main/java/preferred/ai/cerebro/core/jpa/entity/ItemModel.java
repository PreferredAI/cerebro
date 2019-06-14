package preferred.ai.cerebro.core.jpa.entity;

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
	@NamedQuery(name = ItemModel.GET_ALL_ITEM_MODEL_BY_MODEL_ID, query = "SELECT im FROM ItemModel im "
			+ "WHERE im.model.modelId = :modelId"),
	@NamedQuery(name = ItemModel.GET_ITEM_MODEL_BY_ITEM_ID_AND_MODEL_ID, query = "SELECT im FROM ItemModel im "
			+ "WHERE im.itemId = :itemId "
			+ "AND im.model.modelId = :modelId")})

@Entity
@Table(name = "tbl_itemmodel")
@IdClass(ItemModelId.class)
public class ItemModel implements Serializable {
	private static final long serialVersionUID = -2031908364586239883L;

	public static final String GET_ALL_ITEM_MODEL_BY_MODEL_ID = "GetAllItemModelByModeId";
	public static final String GET_ITEM_MODEL_BY_ITEM_ID_AND_MODEL_ID = "GetItemModelByItemIdAndModeId";
	
	@Id
	@Column(name = "ItemId", nullable = false, length = 128)
	private String itemId;
	
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
	
	public ItemModel(){}

	public ItemModel(Model model, String itemId, String representation, Date updatedTime, int status) {
		super();
		this.model = model;
		this.itemId = itemId;
		this.representation = representation;
		this.updatedTime = updatedTime;
		this.status = status;
	}

	public String getItemId() { return itemId; }
	public void setItemId(String itemId) { this.itemId = itemId; }
	
	public Model getModel() { return model; }
	public void setModel(Model model) { this.model = model; }

	public String getRepresentation() { return representation; }
	public void setRepresentation(String representation) { this.representation = representation; }

	public Date getUpdatedTime() { return updatedTime; }
	public void setUpdatedTime(Date updatedTime) { this.updatedTime = updatedTime; }

	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }
}
