package ai.preferred.cerebro.core.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ai.preferred.cerebro.core.utils.BooleanUtils;
import ai.preferred.cerebro.core.utils.NumberUtils;
import ai.preferred.cerebro.core.utils.ObjectUtils;
import ai.preferred.cerebro.core.utils.StringUtils;
/**
 * Command Options
 * @author ddle.2015
 *
 */
public class CommandOptions {
	private Map<String, Option> options = new HashMap<String, Option>();
	
	private static final Logger LOGGER = LogManager.getLogger(CommandOptions.class.getName());

	public CommandOptions(){}

	public void parse(String[] args){
		if(args == null || args.length == 0){
			printHelp();
			return;
		}
		
		for(int i = 0; i < args.length;){

			String currentToken = args[i];
			if(currentToken.startsWith("-"))
				currentToken = currentToken.substring(1, currentToken.length());
			else if(!currentToken.startsWith("-") || StringUtils.isNullOrEmpty(currentToken)){
				LOGGER.error("Invalid argument " + currentToken);
				printHelp();
				System.exit(0);
			}

			Option opt = options.get(currentToken);
			if(ObjectUtils.isNull(opt)){
				LOGGER.error("No such key " + currentToken);
				printHelp();
				System.exit(0);
			}

			if(i < args.length - 1){
				String nextToken = args[i+1];

				if(!nextToken.startsWith("-")){
					opt.setValue(nextToken);
					i += 2;
				} else {
					opt.setValue(true);
					i++;
				}
			} else {
				opt.setValue(true);
				i++;
			}

			options.put(currentToken, opt);
		}
	}

	public void addOption(String key, String description, Object defaultValue){
		options.put(key, new Option(description, defaultValue));
	}

	public Boolean getBooleanOption(String key) throws Exception{
		Option opt = options.get(key);

		if(ObjectUtils.isNull(opt))
			throw new Exception("No such key " + key + " found");

		Object obj = opt.getValue();
		if(ObjectUtils.isNull(obj)) 
			return BooleanUtils.parseBoolean(opt.getDefaultValue());
		return BooleanUtils.parseBoolean(obj);
	}

	public String getStringOption(String key) throws Exception{
		Option opt = options.get(key);

		if(ObjectUtils.isNull(opt))
			throw new Exception("No such key " + key + " found");

		Object obj = opt.getValue();
		if(ObjectUtils.isNull(obj)) 
			return StringUtils.parseString(opt.getDefaultValue());
		return StringUtils.parseString(obj);
	}


	public Integer getIntegerOption(String key) throws Exception{
		Option opt = options.get(key);
		if(ObjectUtils.isNull(opt))
			throw new Exception("No such key " + key + " found");

		Object obj = opt.getValue();
		if(ObjectUtils.isNull(obj)) 
			return NumberUtils.parseInt(opt.getDefaultValue());
		return NumberUtils.parseInt(obj);
	}

	public Double getDoubleOption(String key) throws Exception{
		Option opt = options.get(key);
		if(ObjectUtils.isNull(opt))
			throw new Exception("No such key " + key + " found");

		Object obj = opt.getValue();
		if(ObjectUtils.isNull(obj)) 
			return NumberUtils.parseDouble(opt.getDefaultValue());
		return NumberUtils.parseDouble(obj);
	}

	public Long getLongOption(String key) throws Exception{
		Option opt = options.get(key);
		if(ObjectUtils.isNull(opt))
			throw new Exception("No such key " + key + " found");

		Object obj = opt.getValue();
		if(ObjectUtils.isNull(obj)) 
			return NumberUtils.parseLong(opt.getDefaultValue());

		return NumberUtils.parseLong(obj);
	}

	public void printHelp(){
		StringBuffer buffer = new StringBuffer();

		buffer.append("\nPlease use the following arguments: ").append("\n");
		for(String key: options.keySet()){
			buffer.append("   ").append("-" + key).append(": ")
			.append(options.get(key).toString()).append("\n");
		}
		LOGGER.info(buffer.toString());
	}

	class Option {
		private Object value;
		private String description;
		private Object defaultValue;

		public Option(){}

		public Option(String description, Object defaultValue) {
			super();
			this.description = description;
			this.defaultValue = defaultValue;
		}

		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
		}

		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}

		public String toString(){
			return description + ". Default value " + defaultValue; 
		}
	}
}
