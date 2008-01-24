package eu.etaxonomy.cdm.persistence.dao.common;


import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

public interface IIdentifiableDao <T extends IdentifiableEntity> extends ICdmEntityDao<T>, ITitledDao<T>{
	
}
