package ai.preferred.cerebro.index.builder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ExtFilterTest {
    File filetxt = new File("13812_0.txt");
    File fileTXT = new File("14721_3.TXT");
    File filerar = new File("thrdage.rar");
    File filezip = new File("wrldmp.zip");
    @Test
    void accept() {
        ExtFilter filter = new ExtFilter(".txt", ".rar");
        Assertions.assertTrue(filter.accept(filetxt));
        Assertions.assertTrue(filter.accept(fileTXT));
        Assertions.assertTrue(filter.accept(filerar));
        Assertions.assertFalse(filter.accept(filezip));
    }
}