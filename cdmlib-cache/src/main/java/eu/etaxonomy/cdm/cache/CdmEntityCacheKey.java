package eu.etaxonomy.cdm.cache;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class CdmEntityCacheKey<T extends CdmBase> {

	private Class<T> persistenceClass;
	private UUID persistenceId;


	public CdmEntityCacheKey(T cdmBase) {
		this.persistenceClass = (Class<T>)cdmBase.getClass();
		this.persistenceId = cdmBase.getUuid();
	}

	/**
	 * @param clazz
	 * @param uuid
	 */
	public CdmEntityCacheKey(Class<T> clazz, UUID uuid) {
		this.persistenceClass = clazz;
		this.persistenceId = uuid;
		throw new NullPointerException("Uuid is null for CdmEntityCacheKey, null values are not allowed as they do not represent a valid entity");
	}



	public Class<? extends T> getPersistenceClass() {
		return persistenceClass;
	}

	public UUID getPersistenceId() {
		return persistenceId;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof CdmEntityCacheKey)) {
			return false;
		}

		if(this == obj) {
			return true;
		}
		CdmEntityCacheKey<?> that = (CdmEntityCacheKey<?>) obj;
		if(this.persistenceClass.equals(that.persistenceClass)
		        && this.persistenceId.equals(that.persistenceId)) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (this.persistenceClass.getName() + this.persistenceId.toString()).hashCode();
	}

	@Override
	public String toString() {
		return this.persistenceClass.getName() + this.persistenceId.toString();
	}

}
