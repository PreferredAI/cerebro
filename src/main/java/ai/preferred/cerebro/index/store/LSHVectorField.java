package ai.preferred.cerebro.index.store;

import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.utils.ByteToVec;
import ai.preferred.cerebro.index.utils.IndexUtils;
import ai.preferred.cerebro.index.utils.VecToByte;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

public class LSHVectorField<TVector> extends StoredField {
    VecToByte<TVector> vecToByte;
    ByteToVec<TVector> byteToVec;

    public LSHVectorField(String name, TVector value,
                          VecToByte<TVector> vecToByte,
                          ByteToVec<TVector> byteToVec) {
        super(name, vecToByte.change(value));
        this.byteToVec = byteToVec;
        //this.vecToByte = vecToByte;

    }

    public LSHVectorField(String name, TVector value) throws Exception{
        super(name, (byte[]) null);
        if (value.getClass() == float[].class){
            this.vecToByte = (VecToByte<TVector>)((VecToByte<float[]>) IndexUtils::floatVecToBytes);
            this.byteToVec = (ByteToVec<TVector>)((ByteToVec<float[]>) IndexUtils::getFloatFeatureVector);
        }
        else if(value.getClass() == double[].class){
            this.vecToByte = (VecToByte<TVector>)((VecToByte<double[]>) IndexUtils::doubleVecToBytes);
            this.byteToVec = (ByteToVec<TVector>)((ByteToVec<double[]>) IndexUtils::getDoubleFeatureVector);
        }
        else
            throw new UnsupportedDataType(value.getClass());
        this.fieldsData  = new BytesRef(vecToByte.change(value));
    }

    public TVector getVector(){
        BytesRef cast = (BytesRef) this.fieldsData;
        return byteToVec.change(cast.bytes);
    }

    public void changeFieldContent(TVector vec){
        this.fieldsData = new BytesRef(vecToByte.change(vec));
    }

    public void changeFieldContent(IndexableField field){
        this.fieldsData = field.binaryValue();
    }

}
