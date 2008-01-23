/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.reference;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.common.IdentifiableDaoBase;

/**
 * @author a.mueller
 *
 */
@Repository
public class ReferenceDaoHibernateImpl extends IdentifiableDaoBase<ReferenceBase> implements IReferenceDao {
	static Logger logger = Logger.getLogger(ReferenceDaoHibernateImpl.class);

	public ReferenceDaoHibernateImpl() {
		super(ReferenceBase.class);
	}

	
}