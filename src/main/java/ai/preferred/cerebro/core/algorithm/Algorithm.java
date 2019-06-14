package ai.preferred.cerebro.core.algorithm;

import ai.preferred.cerebro.core.entity.AbstractVector;
import ai.preferred.cerebro.core.entity.Parameter;
import ai.preferred.cerebro.core.util.StringUtils;
/**
 * This class provides several utility for recommendation algorithms
 * @author ddle.2015
 *
 */

public abstract class Algorithm {
	private Parameter params = new Parameter();
	
	public abstract double score(AbstractVector userVector, AbstractVector itemVector);
	public abstract void update(AbstractVector userVector, AbstractVector v, double multiplier);
	public abstract String getType();
	
	/**
	 * Load the learning setting
	 * 
	 * @param settingStr     Setting for recommendation algorithms
	 */
	public void loadSettings(String settingStr){
		String[] attrs = StringUtils.explode(settingStr, ',');
		for(String attr: attrs){
			String[] parts = StringUtils.explode(attr, "[:]");
			params.add(parts[0], parts[1]);
		}
	}
	
	public Parameter getParams() { return params; }
	public void setParams(Parameter params) { this.params = params; }
}
