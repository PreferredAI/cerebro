package ai.preferred.cerebro.index.lsh.builder;

import ai.preferred.cerebro.index.ids.IntID;
import ai.preferred.cerebro.index.ids.StringID;
import ai.preferred.cerebro.index.demo.TestConst;
import ai.preferred.cerebro.index.lsh.exception.DocNotClearedException;
import ai.preferred.cerebro.index.lsh.exception.SameNameException;
import ai.preferred.cerebro.index.utils.IndexConst;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonalizedDocFactoryTest {
    PersonalizedDocFactory<double[]> docFactory;
    
    @BeforeAll
    void init(){
        docFactory = new PersonalizedDocFactory<>(new VecDoubleHandler(), TestConst.hashingVecs);
    }
    
    @AfterEach
    void freeDoc(){
        docFactory.getDoc();
    }
    
    @Test
    void createPersonalizedDoc(){
        try {
            docFactory.createPersonalizedDoc(new StringID("A01"), TestConst.vec1);
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
    void createPersonalizedDocIntID(){
        try {
            docFactory.createPersonalizedDoc(new IntID(1), TestConst.vec1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    void createTextDoc() {
        TextField content = new TextField(IndexConst.CONTENTS, TestConst.text2, Field.Store.NO);
        try {
            docFactory.createTextDoc(new StringID("rwer"), content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    void createTextDocIntID() {
        TextField content = new TextField(IndexConst.CONTENTS, TestConst.text2, Field.Store.NO);
        try {
            docFactory.createTextDoc(new IntID(23), content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    void testDocNotCleared() {
        Assertions.assertThrows(DocNotClearedException.class, ()->{
            docFactory.createPersonalizedDoc(new StringID("A01"), TestConst.vec1);
            docFactory.createPersonalizedDoc(new StringID("A01"), TestConst.vec2);
        });
    }
    
    @Test
    void testSameName() {
        Assertions.assertThrows(SameNameException.class, ()->{
            docFactory.createPersonalizedDoc(new StringID("A01"), TestConst.vec1);
            TextField content = new TextField(IndexConst.VecFieldName, TestConst.text1, Field.Store.NO);
            docFactory.addField(content);
        });
        Assertions.assertThrows(DocNotClearedException.class, ()->{
            TextField content = new TextField(IndexConst.CONTENTS, TestConst.text2, Field.Store.NO);
            docFactory.createTextDoc(new StringID("A01"), content);
            TextField fieldwitherrorname = new TextField(IndexConst.VecFieldName, TestConst.text1, Field.Store.NO);
            docFactory.addField(fieldwitherrorname);
        });
    }


    @Test
    void testHashingVecNotProvided(){
        Assertions.assertThrows(Exception.class, ()->{
            PersonalizedDocFactory docFactory1 = new PersonalizedDocFactory();
            docFactory1.createPersonalizedDoc(new IntID(2), TestConst.vec1);
        });
    }

}