package ai.preferred.cerebro.core.entity;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DenseVectorTest {
	
	DenseVector v1        = new DenseVector(2); 
	DenseVector v2        = new DenseVector(2);
	double[] sum_elements = null;
	double[] v1_half_elements = null;
	String v1_string      = null;
	

	@BeforeEach
	public void setUp() throws Exception {
		double[] v1_elements      = new double[]{1.0, 1.0};
		double[] v2_elements      = new double[]{1.0, -1.0};
		v1.setElements(v1_elements);
		v2.setElements(v2_elements);
		
		sum_elements     		  = new double[]{2.0, 0.0};
		v1_half_elements          = new double[]{0.5, 0.5};
		v1_string 				  = "0:1.0,1:1.0"; 
	}

	@Test
	public void testAdd() {
		v1.add(v2);
		Assertions.assertArrayEquals(sum_elements, v1.getElements());
	}
	
	@Test
	public void testDivision() {
		v1.divide(2);
		Assertions.assertArrayEquals(v1_half_elements, v1.getElements());
	}
	
	@Test
	public void testInnerProduct() {
		double innProd = v1.innerProduct(v2);
		Assertions.assertEquals(0, innProd);
	}
	
	@Test
	public void testToString() {
		String v1_to_string = v1.toString();
		Assertions.assertEquals(v1_string, v1_to_string);
	}
	
	@Test
	public void testConvertFromString() {
		DenseVector vec_from_string = (DenseVector) DenseVector.convertFromString(v1_string); 
		Assertions.assertArrayEquals(v1.getElements(), vec_from_string.getElements());
	}
	

}
