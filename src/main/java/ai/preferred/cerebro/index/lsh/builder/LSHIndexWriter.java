package ai.preferred.cerebro.index.lsh.builder;

import ai.preferred.cerebro.index.common.VecHandler;
import ai.preferred.cerebro.index.ids.ExternalID;
import ai.preferred.cerebro.index.lsh.exception.SameNameException;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.utils.IndexConst;

import java.io.*;
import java.nio.file.Paths;

import static ai.preferred.cerebro.index.utils.IndexConst.Sp;


/**
 *
 * Wrapper class containing an instance of Lucene's {@link IndexWriter}
 * that facilitates the indexing of both text objects and latent feature
 * vectors.\n
 * <p>
 * @author hpminh@apcs.vn
 */
public class LSHIndexWriter<TVector> implements Closeable {
    protected final IndexWriter delegate;
    protected final VecHandler<TVector> handler;
    private LocalitySensitiveHash<TVector>[] hashFuncs = null;
    //protected PersonalizedDocFactory<TVector> docFactory = null;

    /**
     * Constructor for index creation (creating new index in an empty directory)\n
     * <p>
     * In case a path to LSH vectors object is not specify the indexwriter will still load,
     * but any operation involving latent item vector will throw a {@link NullPointerException}.\n
     * <p>
     * @param indexDirectoryPath directory to the folder which will contain the index files.
     * @param splitVecs Hashing vectors.
     * @throws IOException this is triggered when a path or file does not exist.
     */
    public LSHIndexWriter(String indexDirectoryPath, VecHandler<TVector> handler, TVector[]... splitVecs) throws Exception {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
        delegate = new IndexWriter(indexDirectory, iwc);

        //assign and save vector handler
        this.handler = handler;
        if(handler == null)
            throw new Exception("Vector handler not provided");
        IndexUtils.saveVectorHandler(indexDirectoryPath + Sp + IndexConst.VECHANDLERFILE, handler);

        //assign and save hashing vectors
        if(splitVecs != null){
            hashFuncs = new LocalitySensitiveHash[splitVecs.length];
            for (int i = 0; i < splitVecs.length; i++) {
                hashFuncs[i] = new LocalitySensitiveHash(handler, splitVecs[i]);
            }
            handler.save(indexDirectoryPath + Sp + IndexConst.HASHVECFILE, splitVecs);
        }
        else
            throw new Exception("Hash function not provided");
    }


    /**
     * Constructor for index modification (add, delete, update on an already existing index)
     * @param indexDirectoryPath directory to the folder containing the index files.
     * @param splitVecPath directory to the file containing the hashing vectors.
     * @throws Exception this is triggered when a path or file does not exist or there
     * is an error in type casting.
     */
    public LSHIndexWriter(String indexDirectoryPath, String splitVecPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
        delegate = new IndexWriter(indexDirectory, iwc);

        //load up vector handler
        this.handler = IndexUtils.loadVectorHandler(indexDirectoryPath + Sp + IndexConst.VECHANDLERFILE);

        //load and assign hashing vectors
        splitVecPath = splitVecPath == null ? indexDirectoryPath + Sp + IndexConst.HASHVECFILE : splitVecPath;
        File vectorFile = new File(splitVecPath);
        if(IndexUtils.checkFileExist(vectorFile)){
            TVector[][] splitVecs = handler.load(vectorFile);
            hashFuncs = new LocalitySensitiveHash[splitVecs.length];
            for (int i = 0; i < splitVecs.length; i++) {
                hashFuncs[i] = new LocalitySensitiveHash<>(handler, splitVecs[i]);
            }
        }
        else
            System.out.println("Hash file not present");

    }



    /**
     * Closes all open resources and releases the write lock.
     * <p>
     * Note that this may be a costly operation, so, try to re-use
     * a single delegate instead of closing and opening a new one.
     *
     * <p><b>NOTE</b>: You must ensure no other threads are still making
     * changes at the same time that this method is invoked.</p>
     */
    final public void close() throws IOException {
        delegate.close();
    }

    /**
     * Determines the minimal number of documents required before the buffered
     * in-memory documents are flushed as a new Segment. Large values generally
     * give faster indexing.
     * <p>
     * When this is set, the delegate will flush every maxBufferedDocs added
     * documents. Pass in {@link IndexWriterConfig#DISABLE_AUTO_FLUSH} to prevent
     * triggering a flush due to number of buffered documents. Note that if
     * flushing by RAM usage is also enabled, then the flush will be triggered by
     * whichever comes first.
     * <p>
     * Disabled by default (delegate flushes by RAM usage).
     * <p>
     * Takes effect immediately, but only the next time a document is added,
     * updated or deleted.
     *
     * @see #setMaxBufferRAMSize(double)
     * @throws IllegalArgumentException
     *           if maxBufferedDocs is enabled but smaller than 2, or it disables
     *           maxBufferedDocs when ramBufferSize is already disabled.
     */
    final public void setMaxBufferDocNum(int num){
        delegate.getConfig().setMaxBufferedDocs(num);
    }


    /**
     * Determines the amount of RAM that may be used for buffering added documents
     * and deletions before they are flushed to the Directory. Generally for
     * faster indexing performance it's best to flush by RAM usage instead of
     * document count and use as large a RAM buffer as you can.
     * <p>
     * When this is set, the delegate will flush whenever buffered documents and
     * deletions use this much RAM. Pass in
     * {@link IndexWriterConfig#DISABLE_AUTO_FLUSH} to prevent triggering a flush
     * due to RAM usage. Note that if flushing by document count is also enabled,
     * then the flush will be triggered by whichever comes first.
     * <p>
     * The maximum RAM limit is inherently determined by the JVMs available
     * memory. Yet, an {@link IndexWriter} session can consume a significantly
     * larger amount of memory than the given RAM limit since this limit is just
     * an indicator when to flush memory resident documents to the Directory.
     * Flushes are likely happen concurrently while other threads adding documents
     * to the delegate. For application stability the available memory in the JVM
     * should be significantly larger than the RAM buffer used for indexing.
     * <p>
     * <b>NOTE</b>: the account of RAM usage for pending deletions is only
     * approximate. Specifically, if you delete by Query, Lucene currently has no
     * way to measure the RAM usage of individual Queries so the accounting will
     * under-estimate and you should compensate by either calling commit() or refresh()
     * periodically yourself.
     * <p>
     * <b>NOTE</b>: It's not guaranteed that all memory resident documents are
     * flushed once this limit is exceeded.
     * <p>
     *
     * The default value is {@link IndexWriterConfig#DEFAULT_RAM_BUFFER_SIZE_MB}.
     *
     * <p>
     * Takes effect immediately, but only the next time a document is added,
     * updated or deleted.
     *
     * @see IndexWriterConfig#setRAMPerThreadHardLimitMB(int)
     *
     * @throws IllegalArgumentException
     *           if ramBufferSize is enabled but non-positive, or it disables
     *           ramBufferSize when maxBufferedDocs is already disabled
     */
    final public void setMaxBufferRAMSize(double mb){
        delegate.getConfig().setRAMBufferSizeMB(mb);
    }


    /**
     * Delete a document by its unique ID. Note that
     * you should let Cerebro handle the ID field
     * automatically, only passing in the ID value
     * either as an integer or a string.
     *
     * @param ID ID of the document to delete
     * @throws IOException
     */
    public void deleteByID(ExternalID ID) throws IOException {
        Term term = new Term(IndexConst.IDFieldName, new BytesRef(ID.getByteValues()));
        delegate.deleteDocuments(term);
    }

    /**
     * Merging segments together.
     * @throws IOException
     */
    public void mergeSegments() throws IOException {
        int optimalNofSegments = Runtime.getRuntime().availableProcessors();
        delegate.getConfig().setUseCompoundFile(true);
        delegate.getConfig().getMergePolicy().setNoCFSRatio(1.0);
        delegate.forceMerge(optimalNofSegments);
    }

    /**
     * Use this function to index a Document containing latent vector.
     *
     * @param ID unique ID of the document.
     * @param features the latent feature vector to index.
     */
    public void idxPersonalizedDoc(ExternalID ID, TVector features, IndexableField... fields) throws Exception {
        if(this.hashFuncs == null)
            throw new Exception("Hashing Vecs not provided");
        Document doc = new Document();
        StringField idField = new StringField(IndexConst.IDFieldName, new BytesRef(ID.getByteValues()), Field.Store.YES);
        doc.add(idField);
        /* Storing double vector */
        StoredField vecField = new StoredField(IndexConst.VecFieldName, handler.vecToBytes(features));
        doc.add(vecField);
        /* adding hashcode */
        for (int i = 0; i < hashFuncs.length; i++) {
            BytesRef hashcode = hashFuncs[i].getHashBit(features);
            doc.add(new StringField(IndexConst.HashFieldName + i, hashcode, Field.Store.YES));
        }
        for(IndexableField field : fields){
            if(checkReservedFieldName(field.name()))
                throw new SameNameException();
            doc.add(field);
        }
        delegate.addDocument(doc);
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
     */
    public void idxTextDoc(ExternalID ID, IndexableField... fields) throws Exception{
        Document doc = new Document();
        StringField idField = new StringField(IndexConst.IDFieldName, new BytesRef(ID.getByteValues()), Field.Store.YES);
        doc.add(idField);
        for(IndexableField field : fields){
            if(checkReservedFieldName(field.name()))
                throw new SameNameException();
            doc.add(field);
        }
        delegate.addDocument(doc);
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
        boolean c = fieldname.contains(IndexConst.HashFieldName);
        return a || b || c ;
    }


}
