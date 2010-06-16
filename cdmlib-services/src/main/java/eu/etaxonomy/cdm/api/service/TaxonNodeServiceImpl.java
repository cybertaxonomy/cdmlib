// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;

/**
 * @author n.hoffmann
 * @created Apr 9, 2010
 * @version 1.0
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TaxonNodeServiceImpl extends AnnotatableServiceBase<TaxonNode, ITaxonNodeDao> implements ITaxonNodeService{
	private static final Logger logger = Logger
			.getLogger(TaxonNodeServiceImpl.class);

	@Autowired
	private BeanInitializer defaultBeanInitializer;
	
	private Comparator<? super TaxonNode> taxonNodeComparator;
	@Autowired
	public void setTaxonNodeComparator(ITaxonNodeComparator<? super TaxonNode> taxonNodeComparator){
		this.taxonNodeComparator = (Comparator<? super TaxonNode>) taxonNodeComparator;
	}

	public TaxonNode getTaxonNodeByUuid(UUID uuid) {
		return dao.findByUuid(uuid);
	}
	
	public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode,
			List<String> propertyPaths) {
		taxonNode = dao.load(taxonNode.getUuid());
		List<TaxonNode> childNodes = new ArrayList<TaxonNode>(taxonNode.getChildNodes());
		Collections.sort(childNodes, taxonNodeComparator);
		defaultBeanInitializer.initializeAll(childNodes, propertyPaths);
		return childNodes;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ServiceBase#setDao(eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao)
	 */
	@Override
	@Autowired
	protected void setDao(ITaxonNodeDao dao) {
		this.dao = dao;
	}
}
