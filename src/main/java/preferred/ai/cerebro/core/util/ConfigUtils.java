package preferred.ai.cerebro.core.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ConfigUtils
 * @author ddle.2015
 *
 */

public class ConfigUtils {
	static public final String PROPERTIES_FILE = "app.cfg";
	private static ConfigUtils instance = null;
	private Properties properties;
	
	private static final Logger LOGGER = LogManager.getLogger(ConfigUtils.class.getName());
	
	public ConfigUtils(){
		loadProperties(PROPERTIES_FILE);
	}
	
	public static ConfigUtils getInstance(){
		if(instance == null) instance = new ConfigUtils();
		return instance;
	}
	
	/**
	 * 
	 * @param fName File name
	 */
	public void loadProperties(String fName) {
		try {
			FileInputStream propsFile = new FileInputStream(fName);
			properties = new Properties();
			properties.load(propsFile);
			propsFile.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param prefix
	 * @return all keys with prefix
	 */
	public List<String> getAllKeyWithPrefix(String prefix) {
		List<String> holder = new ArrayList<String>();
		for(Object objKey: properties.keySet()) {
			String key = StringUtils.parseString(objKey);
			if(!key.startsWith(prefix)) continue;
			holder.add(key);
		}
		return holder;
	}
	
	/**
	 * 
	 * @param propName
	 * @return 
	 */
	public String getStringProperty(String propName) {
		return properties.getProperty(propName);
	}
	
	/**
	 * 
	 * @param propName
	 * @param defaultValue
	 * @return
	 */
	public String getStringProperty(String propName, String defaultValue) {
		return properties.getProperty(propName, defaultValue);
	}
	
	/**
	 * Gets a property and converts it into byte.
	 */
	public byte getByteProperty(String propName, byte defaultValue) {
		return Byte.parseByte(properties.getProperty(propName, Byte.toString(defaultValue)));
	}

	/**
	 * Gets a property and converts it into integer.
	 */
	public int getIntProperty(String propName, int defaultValue) {
		return Integer.parseInt(properties.getProperty(propName, Integer.toString(defaultValue)));
	}
	
	/**
	 * Gets a property and converts it into double.
	 */
	public double getDoubleProperty(String propName, double defaultValue) {
		return Double.parseDouble(properties.getProperty(propName, Double.toString(defaultValue)));
	}
	
	public void debug(){
		LOGGER.info("------------------- CONFIGURATIONS -----------------");
		for(Object key: properties.keySet()){
			LOGGER.info(" " + key + " " + properties.getProperty(key.toString()));
		}
	}
}
