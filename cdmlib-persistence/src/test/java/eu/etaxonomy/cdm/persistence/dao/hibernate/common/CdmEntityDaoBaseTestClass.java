package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;

@Repository
public class  CdmEntityDaoBaseTestClass extends CdmEntityDaoBase {
	
	public  CdmEntityDaoBaseTestClass() {
		super(TaxonBase.class);
	}


}
