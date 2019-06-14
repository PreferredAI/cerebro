package preferred.ai.cerebro.core.entity;

import java.util.Map;
import java.util.TreeMap;

import preferred.ai.cerebro.core.util.NumberUtils;
import preferred.ai.cerebro.core.util.StringUtils;
/**
 * Parameter
 * @author ddle.2015
 *
 */
public class Parameter {
	
	private Map<String, Object> params = new TreeMap<String, Object>();

	public Map<String, Object> getParams() { return params; }
	public void setParams(Map<String, Object> params) { this.params = params; }
	
	public Parameter add(String key, Object value){ 
		params.put(key, value); 
		return this;
	}
	
	public String getValueAsString(String key){
		return StringUtils.parseString(params.get(key));
	}
	public String getValueAsString(String key, String defaultValue){
		return StringUtils.parseString(params.get(key), defaultValue);
	}
	
	public int getValueAsInt(String key){
		return NumberUtils.parseInt(params.get(key));
	}
	public int getValueAsInt(String key, int defaultValue){
		return NumberUtils.parseInt(params.get(key), defaultValue);
	}
	
	public double getValueAsDouble(String key){
		return NumberUtils.parseDouble(params.get(key));
	}
	
	public double getValueAsDouble(String key, double defaultValue){
		return NumberUtils.parseDouble(params.get(key), defaultValue);
	}
	

	public long getValueAsLong(String key){
		return NumberUtils.parseLong(params.get(key));
	}
	
	public long getValueAsLong(String key, long defaultValue){
		return NumberUtils.parseLong(params.get(key), defaultValue);
	}
	

}
