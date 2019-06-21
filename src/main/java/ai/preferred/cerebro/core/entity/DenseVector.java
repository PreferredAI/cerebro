package ai.preferred.cerebro.core.entity;

import ai.preferred.cerebro.core.util.NumberUtils;
/**
 * Dense vector
 * @author ddle.2015
 *
 */
public class DenseVector extends AbstractVector {
	private static final long serialVersionUID = 3321081266694588388L;
	
	private double[] elements;
	
	public DenseVector(int nbFactors) {
		elements = new double[nbFactors];
	}
	
	@Override
	public void add(AbstractVector v) {
		// Validate v
		validateInput(v);
		
		DenseVector v_cast = (DenseVector) v;
		for(int i = 0; i < elements.length; i++)
			elements[i] += v_cast.getElements()[i];
	}

	@Override
	public void divide(double c) {
		if(c < 0)
			throw new UnsupportedOperationException("The value should be possitive");
		
		for(int i = 0; i < elements.length; i++)
			elements[i] /= c;
	}

	@Override
	public double innerProduct(AbstractVector v) {
		// Validate v
		validateInput(v);
		
		DenseVector v_cast = (DenseVector) v;
		double sum =  0.0;
		for(int i = 0; i < elements.length; i++)
			sum += elements[i] * v_cast.getElements()[i];
		
		return sum;
	}
	
	@Override
	public int length() {
		// TODO Auto-generated method stub
		return elements.length;
	}
	
	@Override
	public void validateInput(AbstractVector v){
		if(!(v instanceof DenseVector))
			throw new ClassCastException("The two vectors have different type");
		
		if(length() != v.length())
			throw new UnsupportedOperationException("The two vectors have different lengths!");       
	}
	
	@Override
	public AbstractVector clone() {
		DenseVector v = new DenseVector(elements.length);
		v.setElements(elements);
		return v;
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < elements.length; i++){
			buffer.append(i).append(":").append(elements[i]);
			if(i < elements.length - 1) buffer.append(",");
		}
		return buffer.toString();
	}
	
	public static AbstractVector convertFromString(String s) {
		String[] els = s.split(",");
		
		DenseVector v = new DenseVector(els.length);
		
		double[] vals = new double[els.length];
		for(String el: els){
			String[] parts = el.split(":");
			int index  = NumberUtils.parseInt(parts[0]);
			double val = NumberUtils.parseDouble(parts[1]);
			vals[index] = val;
		}
		v.setElements(vals);
		return v;
	}

	public double[] getElements() { return elements; }
	public void setElements(double[] elements) { this.elements = elements; }

	@Override
	public void setElement(int index, double value) {
		if(index >= elements.length) 
			throw new IndexOutOfBoundsException();
		elements[index] = value;
	}

	@Override
	public double getElement(int index) {
		if(index >= elements.length) 
			throw new IndexOutOfBoundsException();
		return elements[index];
	}
	
	
}
