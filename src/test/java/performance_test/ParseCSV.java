package performance_test;

//import ai.preferred.cerebro.index.common.FloatCosineHandler;
import ai.preferred.cerebro.index.common.FloatCosineHandler;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class ParseCSV {
    //this is not really a test either, rather a one-time script

    @Test
    public void parse(){
        String file = "E:\\YahooMusicDataset\\yahoo_pmf_user_50d.csv";
        //The cosine has nothing to do here, we just want a vector I/O handler
        FloatCosineHandler handler = new FloatCosineHandler();
        try (
                Reader reader = Files.newBufferedReader(Paths.get(file));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)){
            LinkedList<float[]> vecSet = new LinkedList<>();
            for (CSVRecord csvRecord : csvParser) {
                // Accessing Values by Column Index
                String [] vecStrs = csvRecord.get(0).split(":");
                float[] vec = new float[vecStrs.length];
                for (int i = 0; i < vec.length; i++) {
                    vec[i] = Float.parseFloat(vecStrs[i]);
                }
                vecSet.addLast(vec);
            }
            float[][] vecArray = vecSet.toArray(new float[vecSet.size()][]);
            handler.save("E:\\yahoo_pmf_50d\\users.o", vecArray);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
