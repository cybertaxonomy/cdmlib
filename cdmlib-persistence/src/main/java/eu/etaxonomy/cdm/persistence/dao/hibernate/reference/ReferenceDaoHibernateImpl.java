/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.reference;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;

/**
 * @author a.mueller
 *
 */
@Repository
@Qualifier("referenceDaoHibernateImpl")
public class ReferenceDaoHibernateImpl extends IdentifiableDaoBase<ReferenceBase> implements IReferenceDao {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReferenceDaoHibernateImpl.class);

	public ReferenceDaoHibernateImpl() {
		super(ReferenceBase.class);
	}

	
}