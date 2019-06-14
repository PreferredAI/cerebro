package preferred.ai.cerebro.core.jpa.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

public class QueryUtils {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E> E getSingleResult(Query query) {
		E entity = null;
		try {
			List results = query.getResultList();
			if (results != null && !results.isEmpty())
				entity = (E) results.get(0);
		} catch (Exception ex) {
		}
		return entity;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E> List<E> getResultList(Query query, int limit) {
		ArrayList<E> entities = new ArrayList<E>();
		try {
			if (limit > 0) {
				query.setMaxResults(limit);
			}
			
			List results = query.getResultList();
			if (results != null && !results.isEmpty()) {
				for (int i = 0; i < results.size(); i++) {
					try {
						entities.add((E) results.get(i));
					} catch (Exception ex2) {
					}
				}
			}
		} catch (Exception ex) {
		}
		return entities;
	}
	
	public static <E> List<E> getResultList(Query query) {
		return getResultList(query, 0);
	}

	@SuppressWarnings({ "unchecked" })
	public static <E> E[] getResults(Query query, int limit) {
		ArrayList<E> entities = (ArrayList<E>) getResultList(query, limit);
		return entities.toArray((E[]) new Object[0]);
	}

	public static <E> E[] getResults(Query query) {
		return getResults(query, 0);
	}
	
}