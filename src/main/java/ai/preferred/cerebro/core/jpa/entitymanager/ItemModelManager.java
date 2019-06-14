package ai.preferred.cerebro.core.jpa.entitymanager;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ai.preferred.cerebro.core.jpa.entity.ItemModel;
import ai.preferred.cerebro.core.jpa.util.QueryUtils;

public class ItemModelManager extends AbstractManager<ItemModel> {

	public ItemModelManager(EntityManager entityManager) {
		super(entityManager);
	}

	public List<ItemModel> getAllItemModelByModelId(int modelId){
		Query query = entityManager
				.createNamedQuery(ItemModel.GET_ALL_ITEM_MODEL_BY_MODEL_ID);
		query.setParameter("modelId", modelId);
		return QueryUtils.getResultList(query);
	}
	
	public ItemModel getItemModelByItemIdAndModelId(String itemId, int modelId){
		Query query = entityManager
				.createNamedQuery(ItemModel.GET_ITEM_MODEL_BY_ITEM_ID_AND_MODEL_ID);
		query.setParameter("itemId", itemId);
		query.setParameter("modelId", modelId);
		return QueryUtils.getSingleResult(query);
	}

}