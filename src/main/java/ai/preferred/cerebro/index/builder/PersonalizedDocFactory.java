package ai.preferred.cerebro.index.builder;

import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.exception.DocNotClearedException;
import ai.preferred.cerebro.index.exception.SameNameException;
import ai.preferred.cerebro.index.store.DoubleVecField;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

/**
 * This class handles the creation of Document object
 * to ensure that there is no conflict in field name
 * and that all the hashcoding behaves as intended.
 *
 * @author hpminh@apcs.vn
 */
public class PersonalizedDocFactory<TVector> {
    private LocalitySensitiveHash<TVector> hashFunc = null;
    private Document doc;

    /**
     * Instantiate with a set of hashing vectors.
     * @param splitVecs
     */
    public PersonalizedDocFactory(TVector[] splitVecs){

        hashFunc = new LocalitySensitiveHash(bitComputer, splitVecs);
    }

    public PersonalizedDocFactory(){}
    /**
     * Only use this function to construct a Document containing latent vector.
     * To add additional fields to the Document, use {@link #addField(IndexableField...)}.
     * Call {@link #getDoc()} to pass the Document to IndexWriter or before creating a new Document.
     *
     * @param ID unique ID of the document.
     * @param features the latent feature vector to index.
     * @throws DocNotClearedException this exception is triggered when
     * a call to {@link #createPersonalizedDoc(Object, double[])} is not paired with a
     * call to {@link #getDoc()}.
     * @throws UnsupportedDataType thrown when the ID is not either a String or integer
     * (or Integer)
     */
    public void createPersonalizedDoc(Object ID, double[] features) throws Exception {
        if(this.doc != null)
            throw new DocNotClearedException();
        if(this.hashFunc == null)
            throw new Exception("Hashing Vecs not provided");
        this.doc = new Document();
        StringField idField = null;
        if(ID instanceof String)
            idField = new StringField(IndexConst.IDFieldName, (String)ID, Field.Store.YES);
        else if(ID instanceof Integer)
            idField = new StringField(IndexConst.IDFieldName,
                    new BytesRef(IndexUtils.intToByte(((Integer) ID).intValue())), Field.Store.YES);
        else
            throw new UnsupportedDataType();
        doc.add(idField);
        /* Storing double vector */
        DoubleVecField vecField = new DoubleVecField(features);
        doc.add(vecField);
        /* adding hashcode */
        BytesRef hashcode = hashFunc.getHashBit(features);
        doc.add(new StringField(IndexConst.HashFieldName, hashcode, Field.Store.YES));
    }


    /**
     * Call this function to construct a generic text-only Document.
     * Should you need to add latent vector later call getDoc
     * and start anew with the other createPersonalizedDoc method.
     *
     * @param ID unique ID of the document
     * @param fields the custom fields
     * @throws SameNameException this is triggered when one of your custom field has name
     * identical to Cerebro reserved word. See more detail at {@link IndexConst}.
     * @throws DocNotClearedException this exception is triggered when
     * a call to {@link #createPersonalizedDoc(Object, double[])} is not paired with a
     * call to {@link #getDoc()}.
     * @throws UnsupportedDataType thrown when the ID is not either a String or integer
     * (or Integer)
     *
     *
     */
    public void createTextDoc(Object ID, IndexableField... fields) throws SameNameException, DocNotClearedException, UnsupportedDataType {
        if(this.doc != null){
            throw new DocNotClearedException();
        }
        this.doc = new Document();
        StringField idField = null;
        if(ID instanceof String)
            idField = new StringField(IndexConst.IDFieldName, (String)ID, Field.Store.YES);
        else if(ID instanceof Integer)
            idField = new StringField(IndexConst.IDFieldName,
                    new BytesRef(IndexUtils.intToByte(((Integer) ID).intValue())), Field.Store.YES);
        else
            throw new UnsupportedDataType();
        doc.add(idField);
        for(IndexableField field : fields){
            if(checkReservedFieldName(field.name()))
                throw new SameNameException();
            this.doc.add(field);
        }
    }

    /**
     * After calling {@link #createPersonalizedDoc(Object, double[])} to createPersonalizedDoc a document with latent vector
     * if you still want add more custom fields to a Document then use this function.
     *
     * @param fields fields to add to the {@link Document}
     *               instance at the pointer {@link PersonalizedDocFactory#doc}
     * @throws SameNameException this is triggered when one of your custom field has name
     * identical to Cerebro reserved word. See more detail at {@link IndexConst}.
     *
     *
     *
     */
    public void addField(IndexableField... fields) throws SameNameException {
        for(IndexableField f : fields){
            /* Name of any other fields must not coincide with the name of any reserved field */
            if(checkReservedFieldName(f.name()))
                throw new SameNameException();
            this.doc.add(f);
        }
    }

    /**
     * After calling this function the pointer doc become null again.
     *
     * @return the Document object being built since the last {@link #createPersonalizedDoc(Object, double[])}
     * or {@link #createTextDoc(Object, IndexableField...)} call.
     */
    public Document getDoc(){
        Document t = this.doc;
        this.doc = null;
        return t;
    }

    /**
     * Check if fieldname is similar to any of Cerebro's reserved keywords.
     *
     * @param fieldname the field's name to be checked.
     * @return true if the fieldname is the similar to one of the reserved words.
     */
    public static boolean checkReservedFieldName(String fieldname){
        boolean a = fieldname.equals(IndexConst.IDFieldName);
        boolean b = fieldname.equals(IndexConst.VecFieldName);
        boolean c = fieldname.equals(IndexConst.HashFieldName);
        //boolean d = fieldname.equals(IndexConst.VecLenFieldName);
        return a || b || c ;//|| d;
    }
}
