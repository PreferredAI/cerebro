package ai.preferred.cerebro.core.jpa.entitymanager;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public abstract class AbstractManager<E> {
	protected EntityManager entityManager;

	public AbstractManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void persist(E e) {
		if(!entityManager.getTransaction().isActive())
			entityManager.getTransaction().begin();
		entityManager.persist(e);
		entityManager.flush();
		entityManager.getTransaction().commit();
	}

	@SuppressWarnings("unchecked")
	public void persist(E... objects) {
		if(!entityManager.getTransaction().isActive())
			entityManager.getTransaction().begin();
		for (Object object : objects) {
			entityManager.persist(object);
		}
		entityManager.flush();
		entityManager.getTransaction().commit();
	}

	public void merge(E e) {
		if(!entityManager.getTransaction().isActive())
			entityManager.getTransaction().begin();
//		entityManager.lock(e, LockModeType.OPTIMISTIC);
		entityManager.merge(e);
		entityManager.flush();
//		entityManager.lock(e, LockModeType.NONE);
		entityManager.getTransaction().commit();
	}

	public void refresh(E e) {
		entityManager.refresh(e);

	}

	public void remove(E e) {
		if(!entityManager.getTransaction().isActive())
			entityManager.getTransaction().begin();
		entityManager.remove(e);
		entityManager.flush();
		entityManager.getTransaction().commit();

	}

	public void detach(E e) {
		entityManager.detach(e);
	}

	public void executeQuery(Query query) {
		if(!entityManager.getTransaction().isActive())
			entityManager.getTransaction().begin();
		query.executeUpdate();
		entityManager.flush();
		entityManager.getTransaction().commit();
	}

}