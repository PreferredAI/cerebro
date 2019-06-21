package ai.preferred.cerebro.index.builder;

import ai.preferred.cerebro.index.demo.TestConst;
import ai.preferred.cerebro.index.exception.DocNotClearedException;
import ai.preferred.cerebro.index.exception.SameNameException;
import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.utils.IndexConst;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonalizedDocFactoryTest {
    PersonalizedDocFactory docFactory;
    
    @BeforeAll
    void init(){
        docFactory = new PersonalizedDocFactory(TestConst.hashingVecs);
    }
    
    @AfterEach
    void freeDoc(){
        docFactory.getDoc();
    }
    
    @Test
    void createPersonalizedDoc(){
        try {
            docFactory.createPersonalizedDoc("A01", TestConst.vec1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        TextField content = new TextField(IndexConst.CONTENTS, TestConst.text1, Field.Store.NO);
        try {
            docFactory.addField(content);
        } catch (SameNameException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createTextDoc() {
        TextField content = new TextField(IndexConst.CONTENTS, TestConst.text2, Field.Store.NO);
        try {
            docFactory.createTextDoc("rwer", content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    void testDocNotCleared() {
        Assertions.assertThrows(DocNotClearedException.class, ()->{
            docFactory.createPersonalizedDoc("A01", TestConst.vec1);
            docFactory.createPersonalizedDoc("A01", TestConst.vec2);
        });
    }
    
    @Test
    void testSameName() {
        Assertions.assertThrows(SameNameException.class, ()->{
            docFactory.createPersonalizedDoc("A01", TestConst.vec1);
            TextField content = new TextField(IndexConst.VecFieldName, TestConst.text1, Field.Store.NO);
            docFactory.addField(content);
        });
    }
    
    @Test
    void testUnsupportedDataType(){
        Assertions.assertThrows(UnsupportedDataType.class, ()->{
            docFactory.createPersonalizedDoc(2.3, TestConst.vec1);
        });
    }
}