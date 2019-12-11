package ai.preferred.cerebro.core.jpa.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ai.preferred.cerebro.core.entity.Parameter;
import ai.preferred.cerebro.core.utils.StringUtils;

@NamedQueries({
	@NamedQuery(name = Model.GET_MODEL_BY_CODE, query = "SELECT m FROM Model m "
			+ "WHERE m.modelCode = :modelCode"),
	@NamedQuery(name = Model.GET_MODEL_BY_ID, query = "SELECT m FROM Model m "
			+ "WHERE m.modelId = :modelId")
})

@Entity
@Table(name = "tbl_model")
public class Model implements Serializable {
	private static final long serialVersionUID = 6783646528264205835L;
	
	public static final String GET_MODEL_BY_ID = "GetModelById";
	public static final String GET_MODEL_BY_CODE = "GetModelByCode";
	
	public static final String MODEL_CODE_PMF = "pmf";
	public static final String MODEL_CODE_IPBR = "ibpr";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ModelId", nullable = false)
	private int modelId;
	
	@Column(name = "ModelCode", nullable = false, length = 32)
	private String modelCode;

	@Column(name = "Description", nullable = true, length = 128)
	private String description;
	
	@Column(name = "UseDenseVector", nullable = false, columnDefinition = "TINYINT(1)")
	private boolean useDenseVector;
	
	@Column(name = "Setting", nullable = false)
	private String setting;
	
	@Column(name = "CreatedTime", nullable = true)
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name = "Status", nullable = false, length = 11)
	private int status;

	public Model(){}

	public Model(String modelCode, String description, boolean useDenseVector, 
			String setting, Date createdTime, int status) {
		super();
		this.modelCode = modelCode;
		this.description = description;
		this.useDenseVector = useDenseVector;
		this.setting = setting;
		this.createdTime = createdTime;
		this.status = status;
	}

	public int getModelId() { return modelId; }
	public void setModelId(int modelId) { this.modelId = modelId; }
	
	public String getModelCode() { return modelCode; }
	public void setModelCode(String modelCode) { this.modelCode = modelCode; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	
	public boolean useDenseVector() { return useDenseVector; }
	public void setUseDenseVector(boolean useDenseVector) { this.useDenseVector = useDenseVector;}

	public String getSetting() { return setting; }
	public void setSetting(String setting) { this.setting = setting; }
	
	public Date getCreatedTime() { return createdTime; }
	public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }
	
	public Parameter getSettingAsParams(){
		Parameter params = new Parameter();
		String[] attrs = StringUtils.explode(setting, ',');
		for(String attr: attrs){
			String[] parts = StringUtils.explode(attr, "[:]");
			params.add(parts[0], parts[1]);
		}
		
		return params;
	}
}
