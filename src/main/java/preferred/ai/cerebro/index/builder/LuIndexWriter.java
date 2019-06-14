package preferred.ai.cerebro.index.builder;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import preferred.ai.cerebro.core.jpa.entity.IndexMetadata;
import preferred.ai.cerebro.core.jpa.entity.Model;
import preferred.ai.cerebro.index.exception.DocNotClearedException;
import preferred.ai.cerebro.index.exception.UnsupportedDataType;
import preferred.ai.cerebro.index.utils.IndexConst;
import preferred.ai.cerebro.index.utils.IndexUtils;
import preferred.ai.cerebro.index.utils.JPAUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Wrapper class containing an instance of Lucene's {@link IndexWriter}
 * that facilitates the indexing of both text objects and latent feature
 * vectors.
 *
 * Note that Right now LuIndexWriter is not thread-safe due to the way it
 * uses PersonalizedDocFactory. This will be fixed in a near future version.
 */
public abstract class LuIndexWriter implements VersatileIndexing{
    protected IndexWriter writer;
    protected PersonalizedDocFactory docFactory = null;

    /**
     * @param indexDirectoryPath directory to the folder containing the index files.
     * @param splitVecPath path to the object file containing the LSH vectors.
     * @throws IOException this is triggered when a path or file does not exist.
     *
     * Constructor using an existing LSHash Vector object. This will try to allocate
     * as much memory as possible for the writing buffer.
     *
     * In case a path LSH vectors object is not specify the indexwriter will still load,
     * but any operation involving latent item vector will throw a {@link NullPointerException}.
     */
    public LuIndexWriter(String indexDirectoryPath, String splitVecPath) throws IOException {
        //Get the maximum amount of memory for indexWriter
        long maxHeapSize = Runtime.getRuntime().maxMemory();
        long size = maxHeapSize / IndexConst.mb;
        if(size > 8192)
            size = 8192;

        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
        iwc.setRAMBufferSizeMB(size);
        writer = new IndexWriter(indexDirectory, iwc);
        if(splitVecPath != null){
            double[][] splitVecs = IndexUtils.readVectors(splitVecPath);
            docFactory = new PersonalizedDocFactory(splitVecs);
        }
        else {
            docFactory = new PersonalizedDocFactory();
        }
    }



    /**
     *
     * @param indexDirectoryPath directory to the folder containing the index files.
     * @param model model ID to decide which configuration to get from the database.
     * @param numHash number hashing vector to randomize.
     * @throws IOException this is triggered when a path or file does not exist.
     *
     * Constructor randomizing a new hashtable, then save it to the same folder
     * containing the index file and save metadata to database.
     *
     * Note that this is intented to worked with other unreleased components of
     * Cerebro. As such it is not recommended to instantiate {@link LuIndexWriter}
     * this way.
     */
    public LuIndexWriter(String indexDirectoryPath, int model, int numHash) throws IOException {
        this(indexDirectoryPath, null);
        Model m = JPAUtils.retrieveModelByModelId(model);
        int nbFactor = m.getSettingAsParams().getValueAsInt("nbFactors");
        double[][] splitVecs = IndexUtils.randomizeFeatureVectors(numHash, nbFactor, true, false);
        docFactory = new PersonalizedDocFactory(splitVecs);
        //save the actual hashing vectors to disk
        IndexUtils.saveVectors(splitVecs, indexDirectoryPath + "\\splitVec.o");

        //save metadata to database
        String settings = "numberOfHashes:" + numHash + ",numberOfHashTables:" + 1;
        IndexMetadata indexMetadata = new IndexMetadata(m, null, settings, indexDirectoryPath + "\\splitVec.o", null, 0);
        JPAUtils.insertIndexMetadataToDB(indexMetadata);
    }

    /**
     * Closes all open resources and releases the write lock.
     * <p>
     * Note that this may be a costly operation, so, try to re-use
     * a single writer instead of closing and opening a new one.
     *
     * <p><b>NOTE</b>: You must ensure no other threads are still making
     * changes at the same time that this method is invoked.</p>
     */
    final public void close() throws IOException {
        writer.close();
    }

    /**
     * Determines the minimal number of documents required before the buffered
     * in-memory documents are flushed as a new Segment. Large values generally
     * give faster indexing.
     *
     * <p>
     * When this is set, the writer will flush every maxBufferedDocs added
     * documents. Pass in {@link IndexWriterConfig#DISABLE_AUTO_FLUSH} to prevent
     * triggering a flush due to number of buffered documents. Note that if
     * flushing by RAM usage is also enabled, then the flush will be triggered by
     * whichever comes first.
     *
     * <p>
     * Disabled by default (writer flushes by RAM usage).
     *
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
        writer.getConfig().setMaxBufferedDocs(num);
    }


    /**
     * Determines the amount of RAM that may be used for buffering added documents
     * and deletions before they are flushed to the Directory. Generally for
     * faster indexing performance it's best to flush by RAM usage instead of
     * document count and use as large a RAM buffer as you can.
     * <p>
     * When this is set, the writer will flush whenever buffered documents and
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
     * to the writer. For application stability the available memory in the JVM
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
        writer.getConfig().setRAMBufferSizeMB(mb);
    }


    /**
     *
     * @param ID ID of the document to delete
     * @throws IOException
     * @throws UnsupportedDataType
     * Delete a document by its unique ID. Note that
     * you should let Cerebro handle the ID field
     * automatically, only passing in the ID value
     * either as an integer or a string.
     */
    public void deleteByID(Object ID) throws IOException, UnsupportedDataType {
        Term term = null;
        if(ID instanceof String)
            term = new Term(IndexConst.IDFieldName, (String)ID);
        else if(ID instanceof Integer)
            term = new Term(IndexConst.IDFieldName, new BytesRef(IndexUtils.intToByte(((Integer) ID).intValue())));
        else
            throw new UnsupportedDataType();
        writer.deleteDocuments(term);
        writer.close();
    }

    /**
     *
     * @throws IOException
     * With multithreading trying to get all of the index
     * in one segment has no advantage. You should let Lucene
     * decide when to carry out the index optimization.
     */
    public void optimize() throws IOException {
        writer.getConfig().setUseCompoundFile(true);
        writer.getConfig().getMergePolicy().setNoCFSRatio(1.0);
        writer.forceMerge(1);
        writer.close();
    }

    /**
     *
     * @param dataDirPath directory to the folder containing the data
     * @param filter an object to filter out all the type of file we
     *               don't want to read.
     * @throws IOException
     *
     * This method lists all the acceptable files in the given directory
     * and pass them individually to {@link #indexFile(File)} to index
     * the file content.
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
        writer.close();
    }

    /**
     * Self-implement this function to parse information from your file to be indexed.
     * If you are utilizing personalized search function, plz use docFactory to create your Documents.
     * Do not try to create Lucene document directly if you want to use personalized search.
     * See the deprecated function {@link #createIndexFromVecData(double[][])} as an example
     * of how to work with docFactory.
     */
    abstract public void indexFile(File file) throws IOException;


    /**
     * @param itemVecs the set of item latent vector to be indexes.
     * @throws IOException
     * @throws DocNotClearedException this exception is triggered when
     * a call to {@link PersonalizedDocFactory#create(Object, double[])}
     * is not paired with a call to {@link PersonalizedDocFactory#getDoc()}.
     *
     * This method indexes the given set of vectors, using the
     * order number of a document as ID, this
     * may vary from use-case to use-case.
     */
    public void createIndexFromVecData(double[][] itemVecs) throws Exception {
        for(int i = 0; i < itemVecs.length; i++){
            docFactory.create(writer.numDocs(), itemVecs[i]);
            writer.addDocument(docFactory.getDoc());
        }
        writer.close();
    }
}
