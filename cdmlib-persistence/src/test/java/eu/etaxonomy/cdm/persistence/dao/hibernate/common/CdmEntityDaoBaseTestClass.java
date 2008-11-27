package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;

@Repository
public class  CdmEntityDaoBaseTestClass extends CdmEntityDaoBase {
	
	public  CdmEntityDaoBaseTestClass() {
		super(TaxonBase.class);
	}


}
