package ai.preferred.cerebro.core.jpa.util;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ai.preferred.cerebro.core.util.ConfigUtils;

public class PersistenceUtils {
	private static final Logger LOGGER = LogManager.getLogger(PersistenceUtils.class);
	
	private static EntityManager entityManager = null;
    public static synchronized EntityManager getEntityManager() {
    	if(entityManager == null) {
    		String host = ConfigUtils.getInstance().getStringProperty("preferred.ai.cerebro.cerebrojpa.jdbc.host", "localhost");
    		int port = ConfigUtils.getInstance().getIntProperty("preferred.ai.cerebro.jpa.jdbc.port", 3306);
    		String dbname = ConfigUtils.getInstance().getStringProperty("preferred.ai.cerebro.jpa.jdbc.db");

    		String username = ConfigUtils.getInstance().getStringProperty("preferred.ai.cerebro.jpa.jdbc.user");
    		String password = ConfigUtils.getInstance().getStringProperty("preferred.ai.cerebro.jpa.jdbc.password");
    		entityManager = PersistenceUtils.createEntityManager("cerebrojpa", host, port, dbname, username, password, false);
    	}
    	return entityManager;
    }
	
	private static EntityManager createEntityManager(String name, String host, int port, String dbname, String username, String password, boolean useSSL) {
		try {
			Map<String, String> persistenceMap = new HashMap<String, String>();

			String url = "jdbc:mysql://" + host +":" + port + "/" + dbname 
					+ "?autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8"
					+ "&serverTimezone=Asia/Singapore&verifyServerCertificate=false&useSSL=" + useSSL;
			
			persistenceMap.put("javax.persistence.jdbc.url", url);
			persistenceMap.put("javax.persistence.jdbc.user", username);
			persistenceMap.put("javax.persistence.jdbc.password", password);
			persistenceMap.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
			
			EntityManagerFactory emf =  Persistence.createEntityManagerFactory(name, persistenceMap);
			if (emf != null)
				return emf.createEntityManager();
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
		}
		return null;
	}
}