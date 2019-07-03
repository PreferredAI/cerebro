package ai.preferred.cerebro.core.jpa.utils;

import ai.preferred.cerebro.core.entity.AbstractVector;
import ai.preferred.cerebro.core.entity.DenseVector;
import ai.preferred.cerebro.core.entity.SparseVector;
import ai.preferred.cerebro.core.jpa.entity.ItemModel;
import ai.preferred.cerebro.core.jpa.entitymanager.ItemModelManager;
import ai.preferred.cerebro.core.jpa.entitymanager.ModelManager;

public class LatentVectorUtils {
	
	public static AbstractVector getItemLatentVector(String itemId, int modelId) {
		ModelManager modelManager = new ModelManager(PersistenceUtils.getEntityManager());
		boolean useDenseVector = modelManager.useDenseVector(modelId);

		ItemModelManager itemModelManager = new ItemModelManager(PersistenceUtils.getEntityManager());
		ItemModel im = itemModelManager.getItemModelByItemIdAndModelId(itemId, modelId);
		return convertToLatentVector(useDenseVector, im.getRepresentation());
	}
	
	public static AbstractVector convertToLatentVector(boolean isDenseVector, String s) {
		if(isDenseVector) return DenseVector.convertFromString(s);
		return SparseVector.convertFromString(s);
	}
}
