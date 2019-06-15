package ai.preferred.cerebro.index.demo;

import ai.preferred.cerebro.index.builder.LuIndexWriter;
import ai.preferred.cerebro.index.exception.DocNotClearedException;
import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.File;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {

        //Create random feature vectors for testing
//        double[][] itemVecs = IndexUtils.randomizeFeatureVectors(1000000, 50, false, true);
//        IndexUtils.saveVectors(itemVecs, IndexConst.DIM_50_PATH + "itemVec_1M.o");

        //Randomly generate query vector set for the first time
        //Statistic.generateQueryAndFindTopK(1000, 20,"itemVec_1M.o");


        //Given that the query vectors has been generated before to demo
        //on others item set, we want to use these same query vectors to
        //demo on the new item set.
        //The first param is the filename of the new item set
        //The second param is the filename containing generated query
        //Statistic.refindTop20("itemVec_20M.o", "query_top20_1M.o");


        //Calculate the entropy metric of an item set
        //Statistic.generateHashVectorEntropyMetric();



        try {
            Statistic.compareAccuracyAndSpeed();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            createIndex();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        Statistic.demo();
//        try {
//            sesarchIndex();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        demoDelete();
    }



    static void demoDelete() throws IOException, UnsupportedDataType {
        LuIndexWriter indexWriter = new LuIndexWriter(TestConst.DIM_50_PATH + "index_16bits",
                TestConst.DIM_50_PATH + "splitVec_16bits\\splitVec.o") {
            @Override
            public void indexLatentVectors(Object... params) throws Exception {

            }

            @Override
            public void indexKeyWords(Object... params) throws Exception {

            }

            @Override
            public void indexFile(File file) throws IOException {

            }
        } ;
        for(int i = 5000000; i < 6000000; i++){
            indexWriter.deleteByID(i);
        }
        indexWriter.close();
    }


    static void createIndex() throws Exception {
        double[][] itemVec = IndexUtils.readVectors(TestConst.DIM_50_PATH + "itemVec_20M.o");
        LuIndexWriter indexWriter = new LuIndexWriter(TestConst.DIM_50_PATH + "index_16bits",
                TestConst.DIM_50_PATH + "splitVec_16bits\\splitVec.o") {
            @Override
            public void indexLatentVectors(Object... params) throws Exception {

            }

            @Override
            public void indexKeyWords(Object... params) throws Exception {

            }

            @Override
            public void indexFile(File file) throws IOException {

            }
        };
        try {
            indexWriter.createIndexFromVecData(itemVec);
        } catch (DocNotClearedException e) {
            e.printStackTrace();
        }
    }
}
