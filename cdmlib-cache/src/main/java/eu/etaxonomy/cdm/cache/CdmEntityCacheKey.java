/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.cache;

import eu.etaxonomy.cdm.model.common.CdmBase;

public class CdmEntityCacheKey<T extends CdmBase> {

	private Class<T> persistenceClass;
	private int persistenceId;   //see #7709 for why we use id, not uuid

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
		CdmEntityCacheKey<?> that = (CdmEntityCacheKey<?>) obj;
		if(this.persistenceClass.equals(that.persistenceClass)
		        && this.persistenceId == that.persistenceId) {
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
		return this.persistenceClass.getName() +":" + String.valueOf(this.persistenceId);
	}

}
