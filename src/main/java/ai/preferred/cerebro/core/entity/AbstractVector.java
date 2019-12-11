package ai.preferred.cerebro.core.entity;

import java.io.Serializable;
/**
 * This class defines a vector (dense or sparse)
 * @author ddle.2015
 *
 */
public abstract class AbstractVector implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public abstract void add(AbstractVector v);
	public abstract void divide(double c);
	public abstract double innerProduct(AbstractVector v);
	
	public abstract void setElement(int index, double value);
	public abstract double getElement(int index);
	
	public abstract int length();
	public abstract String toString();
	
	public abstract void validateInput(AbstractVector v);
	public abstract AbstractVector clone();
	
}
