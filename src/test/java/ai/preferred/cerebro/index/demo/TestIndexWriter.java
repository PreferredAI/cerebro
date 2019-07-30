package ai.preferred.cerebro.index.demo;

import ai.preferred.cerebro.index.builder.LuIndexWriter;
import ai.preferred.cerebro.index.builder.PersonalizedDocFactory;
import ai.preferred.cerebro.index.utils.IndexConst;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.io.File;
import java.io.IOException;

public class TestIndexWriter extends LuIndexWriter {
    public TestIndexWriter(String indexDirectoryPath, String splitVecPath) throws IOException {
        super(indexDirectoryPath, splitVecPath);
    }

    @Override
    public void indexFile(File file) throws IOException {

    }

    public void setDocFactory(PersonalizedDocFactory docFactory){
        this.docFactory = docFactory;
    }

    public void indexTest(String text, double[] vec) throws Exception {
        docFactory.createPersonalizedDoc(writer.numDocs(), vec);
        TextField textField = new TextField(IndexConst.CONTENTS, text, Field.Store.NO);
        docFactory.addField(textField);
        writer.addDocument(docFactory.getDoc());
    }
}
