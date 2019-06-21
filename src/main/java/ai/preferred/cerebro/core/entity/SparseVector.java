package ai.preferred.cerebro.core.entity;

import java.util.TreeMap;

import ai.preferred.cerebro.core.util.NumberUtils;
import ai.preferred.cerebro.core.util.StringUtils;
/**
 * Sparse vector
 * @author ddle.2015
 *
 */
public class SparseVector extends AbstractVector {
	private static final long serialVersionUID = 163539530080124298L;

	private TreeMap<String, Double> elements = new TreeMap<String, Double>();

	public SparseVector() {}

	@Override
	public void add(AbstractVector v) {
		// Validate v
		validateInput(v);

		SparseVector v_cast = (SparseVector) v;
		for(String v_key: v_cast.getElements().keySet()){
			double val = 0;
			if(elements.containsKey(v_key))
				val = elements.get(v_key) + v_cast.getElements().get(v_key);
			else val = v_cast.getElements().get(v_key);
			elements.put(v_key, val);
		}
	}

	@Override
	public void divide(double c) {
		if(c < 0)
			throw new UnsupportedOperationException("The value should be possitive");

		for(String key: elements.keySet()){
			double val = elements.get(key);
			elements.put(key, val/c);
		}
	}

	@Override
	public double innerProduct(AbstractVector v) {
		// Validate v
		validateInput(v);

		SparseVector v_cast = (SparseVector) v;
		double sum = 0.0;
		for(String v_key: v_cast.getElements().keySet()){
			if(elements.containsKey(v_key))
				sum += elements.get(v_key) * v_cast.getElements().get(v_key);
		}
		
		return sum;
	}

	@Override
	public int length() {
		return elements.size();
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		int count = 0;
		for(String key: elements.keySet()){
			buffer.append(key).append(":").append(elements.get(key));
			if(count < elements.size() - 1) buffer.append(",");
			count++;
		}
		return buffer.toString();
	}

	@Override
	public void validateInput(AbstractVector v){
		if(!(v instanceof SparseVector))
			throw new ClassCastException("The two vectors have different type");
	}
	
	@Override
	public AbstractVector clone() {
		SparseVector v = new SparseVector();
		v.setElements(elements);
		return v;
	}
	
	public static SparseVector convertFromString(String s) {
		String[] els = s.split(",");
		SparseVector v = new SparseVector();

		TreeMap<String, Double> vals = new TreeMap<String, Double>();
		for(String el: els){
			String[] parts = el.split(":");
			double val = NumberUtils.parseDouble(parts[1]);
			vals.put(parts[0], val);
		}
		
		v.setElements(vals);
		return v;
	}

	public TreeMap<String, Double> getElements() { return elements; }
	public void setElements(TreeMap<String, Double> elements) { this.elements = elements;}

	@Override
	public void setElement(int index, double value) {
		if(index >= elements.size()) 
			throw new IndexOutOfBoundsException();
		elements.put(StringUtils.parseString(index), value);
	}

	@Override
	public double getElement(int index) {
		if(index >= elements.size()) 
			throw new IndexOutOfBoundsException();
		return elements.get(StringUtils.parseString(index));
	}
}
