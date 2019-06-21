package ai.preferred.cerebro.core.entity;


import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SparseVectorTest {
	
	SparseVector  sv1      = new SparseVector(); 
	SparseVector  sv2      = new SparseVector();
//	TreeMap<String, Double> sv1_elements = new TreeMap<String, Double>();
//	TreeMap<String, Double> sv2_elements = new TreeMap<String, Double>();
	


			
/*	@BeforeEach
	public void setUp() throws Exception {
		// sv1 = [1.0, 0.0, 1.0]
		sv1_elements.put("0", 1.0); sv1_elements.put("2", 1.0);
		sv1.setElements(sv1_elements);
		
		// sv2 = [0.0, 1.0, 0.0]
		sv2_elements.put("1", 1.0);
		sv2.setElements(sv2_elements);
		
		// sum = [1.0, 1.0, 1.0]
		sum_elements.put("0", 1.0); sum_elements.put("1", 1.0); sum_elements.put("2", 1.0);
		
		sv1_string = "0:1.0,2:1.0";
	} */
	
	@Test
	public void testAdd() {
		TreeMap<String, Double> sv1_elements = new TreeMap<String, Double>();
		TreeMap<String, Double> sv2_elements = new TreeMap<String, Double>();
		TreeMap<String, Double> sum_elements = new TreeMap<String, Double>();
		
		sv1_elements.put("0", 1.0); sv1_elements.put("2", 1.0);
		sv1.setElements(sv1_elements);
		
		// sv2 = [0.0, 1.0, 0.0]
		sv2_elements.put("1", 1.0);
		sv2.setElements(sv2_elements);
		
		// sum = [1.0, 1.0, 1.0]
		sum_elements.put("0", 1.0); sum_elements.put("1", 1.0); sum_elements.put("2", 1.0);
		
		sv1.add(sv2);
		for (String key:sv1.getElements().keySet()){
			Assertions.assertEquals(sum_elements.get(key), sv1.getElements().get(key));
		}
		
	}
	
	@Test
	public void testInnerProduct() {
		TreeMap<String, Double> sv1_elements = new TreeMap<String, Double>();
		TreeMap<String, Double> sv2_elements = new TreeMap<String, Double>();
		sv1_elements.put("0", 1.0); sv1_elements.put("2", 1.0);
		sv1.setElements(sv1_elements);
		
		// sv2 = [0.0, 1.0, 0.0]
		sv2_elements.put("1", 1.0);
		sv2.setElements(sv2_elements);
		
		double innProd = sv1.innerProduct(sv2);
		Assertions.assertEquals(0, innProd);
	}
	
	@Test
	public void testToString() {
		TreeMap<String, Double> sv1_elements = new TreeMap<String, Double>();
		sv1_elements.put("0", 1.0); sv1_elements.put("2", 1.0);
		sv1.setElements(sv1_elements);
		
		String sv1_string = "0:1.0,2:1.0";
		
		String sv1_to_string = sv1.toString();
		Assertions.assertEquals(sv1_string, sv1_to_string);
	}
	
	@Test
	public void testConvertFromString() {
		TreeMap<String, Double> sv1_elements = new TreeMap<String, Double>();
		sv1_elements.put("0", 1.0); sv1_elements.put("2", 1.0);
		sv1.setElements(sv1_elements);
		
		String sv1_string = "0:1.0,2:1.0";
		
		SparseVector sv1_from_string = SparseVector.convertFromString(sv1_string);
		for (String key:sv1.getElements().keySet()){
			Assertions.assertEquals(sv1.getElements().get(key), sv1_from_string.getElements().get(key));
		}
	}

}
