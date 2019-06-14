package preferred.ai.cerebro.core.jpa.util;

import preferred.ai.cerebro.core.entity.AbstractVector;
import preferred.ai.cerebro.core.entity.DenseVector;
import preferred.ai.cerebro.core.entity.SparseVector;
import preferred.ai.cerebro.core.jpa.entity.ItemModel;
import preferred.ai.cerebro.core.jpa.entitymanager.ItemModelManager;
import preferred.ai.cerebro.core.jpa.entitymanager.ModelManager;

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
