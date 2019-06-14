package preferred.ai.cerebro.index.builder;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import preferred.ai.cerebro.index.exception.DocNotClearedException;
import preferred.ai.cerebro.index.exception.SameNameException;
import preferred.ai.cerebro.index.store.DoubleStoredField;
import preferred.ai.cerebro.index.store.VectorField;
import preferred.ai.cerebro.index.utils.IndexConst;
import preferred.ai.cerebro.index.utils.IndexUtils;

import java.util.Iterator;

/**
 * This class handles the creation of Document object
 * to ensure that there is no conflict in field name
 * and that all the hashcoding behaves as intended.
 */
public class PersonalizedDocFactory {
    private LocalitySensitiveHash hashFunc = null;
    private Document doc;

    PersonalizedDocFactory(double [][] splitVecs){
        hashFunc = new LocalitySensitiveHash(splitVecs);
    }

    PersonalizedDocFactory(){}
    /**
     * @param ID unique ID of the document.
     * @param features the latent feature vector to index.
     * @throws DocNotClearedException this exception is triggered when
     * a call to {@link #create(Object, double[])} is not paired with a
     * call to {@link #getDoc()}.
     *
     * Only use this function to construct a Document containing latent vector.
     * To add additional fields to the Document, use {@link #addField(IndexableField...)}.
     * Call {@link #getDoc()} to pass the Document to IndexWriter or before creating a new Document.
     */
    public void create(Object ID, double[] features) throws Exception {
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
        doc.add(idField);
        /* Storing double vector */
        VectorField vecField = new VectorField(features);
        doc.add(vecField);
        /* adding hashcode */
        BytesRef hashcode = hashFunc.getHashBit(features);
        doc.add(new StringField(IndexConst.HashFieldName, hashcode, Field.Store.YES));
    }


    /**
     *
     * @param ID unique ID of the document
     * @param fields the custom fields
     * @throws SameNameException this is triggered when one of your custom field has name
     * identical to Cerebro reserved word. See more detail at {@link IndexConst}.
     * @throws DocNotClearedException this exception is triggered when
     * a call to {@link #create(Object, double[])} is not paired with a
     * call to {@link #getDoc()}.
     *
     * Call this function to construct a generic text-only Document.
     * Should you need to add latent vector later call getDoc
     * and start anew with the other create method.
     */
    public void create(Object ID, IndexableField... fields) throws SameNameException, DocNotClearedException {
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
        doc.add(idField);
        for(IndexableField field : fields){
            if(checkReservedFieldName(field.name()))
                throw new SameNameException("Same name exception");
            this.doc.add(field);
        }
    }

    /**
     *
     * @param fields
     * @throws SameNameException this is triggered when one of your custom field has name
     * identical to Cerebro reserved word. See more detail at {@link IndexConst}.
     *
     * After calling {@link #create(Object, double[])} to create a document with latent vector
     * if you still want add more custom fields to a Document then use this function.
     */
    public void addField(IndexableField... fields) throws SameNameException {
        for(IndexableField f : fields){
            /* Name of any other fields must not coincide with the name of any reserved field */
            if(checkReservedFieldName(f.name()))
                throw new SameNameException("Same name exception");
            this.doc.add(f);
        }
    }

    /**
     *
     * @return the Document object being built since the last {@link #create(Object, double[])}
     * or {@link #create(Object, IndexableField...)} call.
     *
     * After calling this function the pointer doc become null again.
     */
    public Document getDoc(){
        Document t = this.doc;
        this.doc = null;
        return t;
    }

    /**
     * @param fieldname
     * @return true if the fieldname is the similar to one of the reserved words.
     */
    public boolean checkReservedFieldName(String fieldname){
        boolean a = fieldname.equals(IndexConst.IDFieldName);
        boolean b = fieldname.equals(IndexConst.VecFieldName);
        boolean c = fieldname.equals(IndexConst.HashFieldName);
        //boolean d = fieldname.equals(IndexConst.VecLenFieldName);
        return a || b || c ;//|| d;
    }
}
