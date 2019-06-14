package preferred.ai.cerebro.core.jpa.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
	@NamedQuery(name = IndexType.GET_ALL_INDEX_TYPE, query = "SELECT it FROM IndexType it"),
	@NamedQuery(name = IndexType.GET_INDEX_TYPE_BY_ID, query = "SELECT it FROM IndexType it "
			+ "WHERE it.id = :typeId")})
@Entity
@Table(name = "tbl_indextype")
public class IndexType implements Serializable {
	private static final long serialVersionUID = 8912126496605092997L;
	
	public static final String GET_ALL_INDEX_TYPE = "GetAllIndexType";
	public static final String GET_INDEX_TYPE_BY_ID = "GetIndexTypeById";
	
	public static final int TYPE_EXHAUSTIVE = 0;
	public static final int TYPE_LSH = 1;
	public static final int TYPE_INVERTED = 2;
	
	
	@Id
	@Column(name = "Id", nullable = false, length = 11)
	private int id;

	@Column(name = "Type", nullable = false, length = 256)
	private String type;
	
	@Column(name = "Status", nullable = false, length = 11)
	private int status;

	public IndexType(){}
	
	public IndexType(String type) {
		super();
		this.type = type;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }
}
