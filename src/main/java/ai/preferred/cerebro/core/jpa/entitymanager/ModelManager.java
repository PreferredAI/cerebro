package ai.preferred.cerebro.core.jpa.entitymanager;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ai.preferred.cerebro.core.jpa.entity.Model;
import ai.preferred.cerebro.core.jpa.util.QueryUtils;

public class ModelManager extends AbstractManager<Model> {

	public ModelManager(EntityManager entityManager) {
		super(entityManager);
	}
	
	public Model getModelById(int modelId){
		Query query = entityManager
				.createNamedQuery(Model.GET_MODEL_BY_ID);
		query.setParameter("modelId", modelId);
		return QueryUtils.getSingleResult(query);
	}

	public Model getModelByCode(String modelCode){
		Query query = entityManager
				.createNamedQuery(Model.GET_MODEL_BY_CODE);
		query.setParameter("modelCode", modelCode);
		return QueryUtils.getSingleResult(query);
	}
	
	public boolean useDenseVector(int modelId){
		return getModelById(modelId).useDenseVector();
	}

}