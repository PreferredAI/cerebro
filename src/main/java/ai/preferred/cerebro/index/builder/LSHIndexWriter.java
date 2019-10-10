package ai.preferred.cerebro.index.builder;

import ai.preferred.cerebro.common.ExternalID;
import ai.preferred.cerebro.common.IntID;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.exception.DocNotClearedException;
import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.*;
import java.nio.file.Paths;

import static ai.preferred.cerebro.index.utils.IndexUtils.loadHashVec;

/**
 *
 * Wrapper class containing an instance of Lucene's {@link IndexWriter}
 * that facilitates the indexing of both text objects and latent feature
 * vectors.\n
 * <p>
 * Note that Right now LSHIndexWriter is not thread-safe due to the way it
 * uses PersonalizedDocFactory. This will be fixed in a near future version.
 *
 * @author hpminh@apcs.vn
 */
public abstract class LSHIndexWriter<TVector> implements Closeable {
    protected IndexWriter delegate;
    protected PersonalizedDocFactory<TVector> docFactory = null;

    /**
     * Constructor using an existing LSHash Vector object. This will try to allocate
     * as much memory as possible for the writing buffer.\n
     * <p>
     * In case a path to LSH vectors object is not specify the indexwriter will still load,
     * but any operation involving latent item vector will throw a {@link NullPointerException}.\n
     * <p>
     * @param indexDirectoryPath directory to the folder containing the index files.
     * @param splitVecs LSH vectors.
     * @throws IOException this is triggered when a path or file does not exist.
     */
    public LSHIndexWriter(String indexDirectoryPath, TVector[] splitVecs) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
        delegate = new IndexWriter(indexDirectory, iwc);
        if(splitVecs != null){
            docFactory = new PersonalizedDocFactory<TVector>(splitVecs);
            saveHashVecFile(indexDirectoryPath + "\\splitVec.o", splitVecs);
        }

        else
            System.out.println("Hash function not provided");
    }


    /**
     * Constructor randomizing a new hashtable, then save it to the same folder
     * containing the index file and save metadata to database.\n
     * <p>
     * Note that this is intented to worked with other unreleased components of
     * Cerebro. As such it is not recommended to instantiate {@link LSHIndexWriter}
     * this way.
     * @param indexDirectoryPath directory to the folder containing the index files.
     * @param splitVecPath directory to the file containing the hashing vectors.
     * @throws Exception this is triggered when a path or file does not exist or there
     * is an error in type casting.
     *

     */

    public LSHIndexWriter(String indexDirectoryPath, String splitVecPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
        delegate = new IndexWriter(indexDirectory, iwc);
        if(splitVecPath != null){
            TVector[] splitVecs = (TVector[]) loadHashVec(splitVecPath);
            docFactory = new PersonalizedDocFactory(splitVecs);
        }
        else {
            File f = new File(indexDirectoryPath + "\\splitVec.o");
            if(f.exists() && !f.isDirectory()) {
                TVector[] splitVecs = (TVector[]) loadHashVec(f.getAbsolutePath());
                docFactory = new PersonalizedDocFactory(splitVecs);
            }
            else
                System.out.println("Hash file not present");
        }
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
     * @throws UnsupportedDataType thrown when the ID arg is not an instance of {@link String} or an int
     *
     */
    public void deleteByID(Object ID) throws IOException, UnsupportedDataType {
        Term term = null;
        if(ID instanceof String)
            term = new Term(IndexConst.IDFieldName, (String)ID);
        else if(ID instanceof Integer)
            term = new Term(IndexConst.IDFieldName, new BytesRef(IndexUtils.intToByte(((Integer) ID).intValue())));
        else
            throw new UnsupportedDataType();
        delegate.deleteDocuments(term);
    }

    /**
     * With multithreading trying to get all of the index
     * in one segment has no advantage. You should let Lucene
     * decide when to carry out the index optimization.
     *
     * @throws IOException
     *
     */
    public void optimize() throws IOException {
        int optimalNofSegments = Runtime.getRuntime().availableProcessors();
        delegate.getConfig().setUseCompoundFile(true);
        delegate.getConfig().getMergePolicy().setNoCFSRatio(1.0);
        delegate.forceMerge(optimalNofSegments);
    }

    /**
     * This method lists all the acceptable files in the given directory
     * and pass them individually to {@link #indexFile(File)} to index
     * the file content.
     *
     * @param dataDirPath directory to the folder containing the data
     * @param filter an object to filter out all the type of file we
     *               don't want to read.
     * @throws IOException
     */
    final public void createIndexFromDir(String dataDirPath, FileFilter filter)
            throws IOException {
        //get all files in the data directory
        File[] files = new File(dataDirPath).listFiles();

        for (File file : files) {
            if(!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()
                    && filter.accept(file)
            ){
                indexFile(file);
            }
        }
    }

    /**
     * Self-implement this function to parse information from your file to be indexed.
     *
     * It is not necessary to implement this function if you don't plan to
     * call {@link #createIndexFromDir(String dataDirPath, FileFilter filter)}. It is, however,
     * provided so that you can customize the way your data can be parsed.
     *
     * If you are utilizing personalized search function, plz use docFactory to create your Documents.
     * Do not try to create Lucene document directly if you want to use personalized search.
     * See the function {@link #createIndexFromVecData(TVector[])} as an example
     * of how to work with docFactory.
     */
    abstract public void indexFile(File file) throws IOException;


    /**
     * Your customizable function to index a group of information object as a single Document in the index
     * structure
     *
     * @param ID                   external ID object, could be ID generated by a seperate DB, or your own defined ID, ...etc.
     * @param personalizedFeatures latent feature vectors
     * @param textualInfo          other infomations about the object to be indexed
     */
    abstract public void indexAsOneDocument(ExternalID ID, TVector personalizedFeatures, String... textualInfo) throws Exception;


    /**
     * This method indexes the given set of vectors, using the
     * order number of a document as ID.
     *
     * @param itemVecs the set of item latent vector to be indexes.
     * @throws IOException
     * @throws DocNotClearedException this exception is triggered when
     * a call to {@link PersonalizedDocFactory#createPersonalizedDoc(ExternalID, TVector)}
     * is not paired with a call to {@link PersonalizedDocFactory#getDoc()}.
     */
    public void createIndexFromVecData(TVector[] itemVecs) throws Exception {
        for(int i = 0; i < itemVecs.length; i++){
            docFactory.createPersonalizedDoc(new IntID(delegate.numDocs()), itemVecs[i]);
            delegate.addDocument(docFactory.getDoc());
        }
    }

    private void saveHashVecFile(String splitVecFilename, TVector[] vecs){
        Kryo kryo = new Kryo();
        try {
            Output output = new Output(new FileOutputStream(splitVecFilename));
            kryo.writeClassAndObject(output, vecs);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
