package eu.etaxonomy.cdm.cache;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class CdmEntityCacheKey {

	private Class<? extends CdmBase> persistenceClass;
	private int persistenceId;
	
	public CdmEntityCacheKey(CdmBase cdmBase) {
		this.persistenceClass = cdmBase.getClass();
		this.persistenceId = cdmBase.getId();
	}
	
	public CdmEntityCacheKey(Class<? extends CdmBase> clazz, int id) {
		this.persistenceClass = clazz;
		this.persistenceId = id;
	}
	

	
	public Class<? extends CdmBase> getPersistenceClass() {
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
		CdmEntityCacheKey that = (CdmEntityCacheKey) obj;
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
