package ai.preferred.cerebro.index.lsh.builder;

import ai.preferred.cerebro.index.extra.TestConst;
import ai.preferred.cerebro.index.common.DoubleCosineHandler;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalitySensitiveHashTest {

    byte[] code1 = {0, 0, 0, 0x38};
    byte[] code2 = {0, 0, 0, 0x39};
    byte[] code3 = {0, 0, 0, 0x3d};
    
    @Test
    void test(){
        LocalitySensitiveHash<double[]> lsh = new LocalitySensitiveHash<>(new DoubleCosineHandler(), TestConst.hashingVecs);

        BytesRef hashcode1 = new BytesRef(code1);
        Assertions.assertTrue(hashcode1.bytesEquals(lsh.getHashBit(TestConst.vec1)));

        BytesRef hashcode2 = new BytesRef(code2);
        Assertions.assertTrue(hashcode2.bytesEquals(lsh.getHashBit(TestConst.vec2)));

        BytesRef hashcode3 = new BytesRef(code3);
        Assertions.assertTrue(hashcode3.bytesEquals(lsh.getHashBit(TestConst.vec3)));
    }
}