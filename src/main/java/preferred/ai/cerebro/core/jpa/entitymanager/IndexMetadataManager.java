package preferred.ai.cerebro.core.jpa.entitymanager;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import preferred.ai.cerebro.core.jpa.entity.IndexMetadata;
import preferred.ai.cerebro.core.jpa.util.QueryUtils;

public class IndexMetadataManager extends AbstractManager<IndexMetadata> {

	public IndexMetadataManager(EntityManager entityManager) {
		super(entityManager);
	}

	public IndexMetadata getIndexMetadataById(int id){
		Query query = entityManager
				.createNamedQuery(IndexMetadata.GET_INDEX_METADATA_BY_ID);
		query.setParameter("id", id);
		return QueryUtils.getSingleResult(query);
	}
	
	public List<IndexMetadata> getListIndexMetadataByModelCode(String modelCode){
		Query query = entityManager
				.createNamedQuery(IndexMetadata.GET_LIST_INDEX_METADATA_BY_MODEL_CODE);
		query.setParameter("modelCode", modelCode);
		return QueryUtils.getResultList(query);
	}
	
	public List<IndexMetadata> getListIndexMetadataByIndexType(int typeId){
		Query query = entityManager
				.createNamedQuery(IndexMetadata.GET_LIST_INDEX_METADATA_BY_TYPE_ID);
		query.setParameter("typeId", typeId);
		return QueryUtils.getResultList(query);
	}

}