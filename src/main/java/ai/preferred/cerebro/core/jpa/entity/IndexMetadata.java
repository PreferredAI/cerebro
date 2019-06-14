package ai.preferred.cerebro.core.jpa.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
	@NamedQuery(name = IndexMetadata.GET_INDEX_METADATA_BY_ID, query = "SELECT im FROM IndexMetadata im "
			+ "WHERE im.id = :id"),
	@NamedQuery(name = IndexMetadata.GET_LIST_INDEX_METADATA_BY_MODEL_CODE, query = "SELECT im FROM IndexMetadata im "
			+ "WHERE im.model.modelCode = :modelCode"),
	@NamedQuery(name = IndexMetadata.GET_LIST_INDEX_METADATA_BY_TYPE_ID, query = "SELECT im FROM IndexMetadata im "
			+ "WHERE im.type.id = :typeId")})
@Entity
@Table(name = "tbl_index")
public class IndexMetadata implements Serializable {
	private static final long serialVersionUID = 4446729250740301382L;
	
	public static final String GET_INDEX_METADATA_BY_ID = "GetIndexMetadataById";
	public static final String GET_LIST_INDEX_METADATA_BY_MODEL_CODE = "GetListIndexMetadataByModelCode";
	public static final String GET_LIST_INDEX_METADATA_BY_TYPE_ID = "GetListIndexMetadataByTypeId";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", nullable = false, length = 11)
	private int id;
	
	@Column(name = "Name", nullable = false, length = 32)
	private String name;
	
	@ManyToOne(targetEntity = Model.class)
	@JoinColumn(name = "ModelId", referencedColumnName = "ModelId",  nullable = false)
	private Model model;
	
	@ManyToOne(targetEntity = IndexType.class)
	@JoinColumn(name = "IndexTypeId", referencedColumnName = "Id",  nullable = false)
	private IndexType type;

	@Column(name = "Settings", nullable = false, length = 255)
	private String settings;
	
	@Column(name = "IndexPath", nullable = false, length = 255)
	private String indexPath;
	
	@Column(name = "SupporterPath", length = 255)
	private String supporterPath;
	
	
	@Column(name = "Status", nullable = false, length = 11)
	private int status;
	
	public IndexMetadata(){}
	
	public IndexMetadata(Model model, IndexType type, String settings, String indexPath, String supporterPath, int status) {
		this("NA", model, type, settings, indexPath, supporterPath, status);
	}

	public IndexMetadata(String name, Model model, IndexType type, String settings, String indexPath, String supporterPath, int status) {
		super();
		this.name = name;
		this.model = model;
		this.type = type;
		this.settings = settings;
		this.indexPath = indexPath;
		this.supporterPath = supporterPath;
		this.status = status;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public Model getModel() { return model; }
	public void setModel(Model model) { this.model = model; }

	public IndexType getType() { return type; }
	public void setType(IndexType type) { this.type = type; }

	public String getSettings() { return settings; }
	public void setSettings(String settings) { this.settings = settings; }
	
	public String getIndexPath() { return indexPath;}
	public void setIndexPath(String indexPath) { this.indexPath = indexPath;}
	
	public String getSupporterPath() { return supporterPath; }
	public void setSupporterPath(String supporterPath) { this.supporterPath = supporterPath; }

	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }
}
