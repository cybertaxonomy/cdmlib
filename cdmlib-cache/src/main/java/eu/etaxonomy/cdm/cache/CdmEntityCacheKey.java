package eu.etaxonomy.cdm.cache;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class CdmEntityCacheKey<T extends CdmBase> {

	private Class<T> persistenceClass;
	private int persistenceId;


	public CdmEntityCacheKey(T cdmBase) {
		this.persistenceClass = (Class<T>)cdmBase.getClass();
		this.persistenceId = cdmBase.getId();
	}

	public CdmEntityCacheKey(Class<T> clazz, int id) {
		this.persistenceClass = clazz;
		this.persistenceId = id;
	}



	public Class<? extends T> getPersistenceClass() {
		return persistenceClass;
	}

	public int getPersistenceId() {
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
		CdmEntityCacheKey<T> that = (CdmEntityCacheKey<T>) obj;
		if(this.persistenceClass.equals(that.persistenceClass) && this.persistenceId == that.persistenceId) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (this.persistenceClass.getName() + String.valueOf(this.persistenceId)).hashCode();
	}

	@Override
	public String toString() {
		return this.persistenceClass.getName() + String.valueOf(this.persistenceId);
	}

}
