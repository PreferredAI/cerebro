package preferred.ai.cerebro.index.utils;

import preferred.ai.cerebro.core.entity.AbstractVector;
import preferred.ai.cerebro.core.jpa.entity.IndexMetadata;
import preferred.ai.cerebro.core.jpa.entity.IndexType;
import preferred.ai.cerebro.core.jpa.entity.ItemModel;
import preferred.ai.cerebro.core.jpa.entity.Model;
import preferred.ai.cerebro.core.jpa.entitymanager.IndexMetadataManager;
import preferred.ai.cerebro.core.jpa.entitymanager.IndexTypeManager;
import preferred.ai.cerebro.core.jpa.entitymanager.ItemModelManager;
import preferred.ai.cerebro.core.jpa.entitymanager.ModelManager;
import preferred.ai.cerebro.core.jpa.util.LatentVectorUtils;
import preferred.ai.cerebro.core.jpa.util.PersistenceUtils;

import java.util.List;

/**
 * Handle communication with Cerebro's other unreleased
 * modules
 */
public class JPAUtils {
    /* get item vectors from model code */
    public static List<ItemModel> retrieveItemListByModelId(int modelId){
        ItemModelManager itemModelManager = new ItemModelManager(PersistenceUtils.getEntityManager());
        List<ItemModel>  itemModelList = itemModelManager.getAllItemModelByModelId(modelId);

        return itemModelList;
    }

    public static AbstractVector retrieveItemByItemIdAndModelId(String itemId, int modelId){
        ItemModelManager itemModelManager = new ItemModelManager(PersistenceUtils.getEntityManager());
        ItemModel im = itemModelManager.getItemModelByItemIdAndModelId(itemId, modelId);

        ModelManager modelManager = new ModelManager(PersistenceUtils.getEntityManager());
        Model m = modelManager.getModelById(modelId);

        return LatentVectorUtils.convertToLatentVector(m.useDenseVector(), im.getRepresentation());
    }

    public static Model retrieveModelByModelId(int modelId){
        ModelManager modelManager = new ModelManager(PersistenceUtils.getEntityManager());
        return modelManager.getModelById(modelId);
    }

    public static IndexType retrieveIndexTypeByType(String type){
        IndexTypeManager indexTypeManager = new IndexTypeManager(PersistenceUtils.getEntityManager());
        for(IndexType iType: indexTypeManager.getAllIndexType())
            if(iType.getType().equalsIgnoreCase(type))
                return iType;
        return null;
    }

    public static void insertIndexMetadataToDB(IndexMetadata iMeta) {
        IndexMetadataManager iMetaManager = new IndexMetadataManager(PersistenceUtils.getEntityManager());
        iMetaManager.persist(iMeta);
    }


}