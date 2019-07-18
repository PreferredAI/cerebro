package ai.preferred.cerebro.core.jpa.entitymanager;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ai.preferred.cerebro.core.jpa.entity.IndexType;
import ai.preferred.cerebro.core.jpa.utils.QueryUtils;

public class IndexTypeManager extends AbstractManager<IndexType> {

	public IndexTypeManager(EntityManager entityManager) {
		super(entityManager);
	}

	public List<IndexType> getAllIndexType(){
		Query query = entityManager
				.createNamedQuery(IndexType.GET_ALL_INDEX_TYPE);
		return QueryUtils.getResultList(query);
	}
	
	public IndexType getIndexTypeById(String typeId){
		Query query = entityManager
				.createNamedQuery(IndexType.GET_INDEX_TYPE_BY_ID);
		query.setParameter("typeId", typeId);
		return QueryUtils.getSingleResult(query);
	}

}